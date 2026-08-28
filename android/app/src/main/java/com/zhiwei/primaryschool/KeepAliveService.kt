package com.zhiwei.primaryschool

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat

class KeepAliveService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var screenOn = true
    private var lastSec = -1L
    private var homeWasOn = false
    private var lastBeatLog = 0L
    private var wake: PowerManager.WakeLock? = null

    private val screenRecv = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Prefs.pausePlayClock()
                    screenOn = false
                    if (Prefs.isPlayActive()) armExpire() else cancelExpire()
                    syncWake()
                    show()
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    screenOn = true
                    Prefs.resumePlayClock()
                    if (Prefs.isPlayActive()) armExpire()
                    syncWake()
                    show()
                }
            }
        }
    }

    private val loop = object : Runnable {
        override fun run() {
            tick()
            handler.postDelayed(this, if (Prefs.isPlayActive()) 400L else 15_000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        val idle = System.currentTimeMillis() - Prefs.lastBeat()
        val play = Prefs.isPlayActive()
        if (Prefs.lastBeat() > 0L && idle > 20_000L) {
            Watch.log(this, if (play) "killed during play idle=${idle / 1000}s" else "killed idle=${idle / 1000}s")
        }
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        screenOn = pm.isInteractive
        homeWasOn = Perms.homeOn(this)
        Watch.log(this, "KeepAlive create idle=${idle / 1000}s home=$homeWasOn play=$play bat=${Perms.batteryOn(this)}")
        registerReceiver(
            screenRecv,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
        )
        ensureChannels()
        startForeground(NOTE_ID, note())
        syncWake()
        handler.post(loop)
        armWatch(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTE_ID, note())
        when (intent?.action) {
            ACTION_EXPIRE -> expire()
            ACTION_WATCH -> tick()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Watch.log(this, "KeepAlive destroy play=${Prefs.isPlayActive()}")
        handler.removeCallbacks(loop)
        cancelExpire()
        releaseWake()
        if (Prefs.isPlayActive()) armWatch(applicationContext, 5_000L)
        try {
            unregisterReceiver(screenRecv)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun tick() {
        if (Prefs.isPlayActive() && Prefs.playRemainingMs() <= 0L) {
            expire()
            return
        }
        syncWake()
        if (Prefs.isPlayActive()) {
            val sec = Prefs.playRemainingMs() / 1000
            if (sec != lastSec) {
                lastSec = sec
                show()
            }
            police()
            armExpire()
            armWatch(this, 12_000L)
        }
        val now = SystemClock.elapsedRealtime()
        if (now - lastBeatLog > 14_000L) {
            lastBeatLog = now
            if (!Prefs.isPlayActive()) show()
            Watch.log(
                this,
                "beat home=${Perms.homeOn(this)} play=${Prefs.isPlayActive()} bat=${Perms.batteryOn(this)}"
            )
            val on = Perms.homeOn(this)
            if (on != homeWasOn) {
                homeWasOn = on
                Watch.log(this, "home changed -> $on")
            }
            if (!Prefs.isPlayActive()) armWatch(this)
        }
    }

    private fun syncWake() {
        if (Prefs.isPlayActive() && screenOn) holdWake() else releaseWake()
    }

    private fun holdWake() {
        if (wake?.isHeld == true) return
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wake = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "primaryschool:play").apply {
            setReferenceCounted(false)
            acquire(10 * 60 * 60 * 1000L)
        }
    }

    private fun releaseWake() {
        wake?.let { if (it.isHeld) it.release() }
        wake = null
    }

    private fun show() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTE_ID, note())
    }

    private fun police() {
        if (Prefs.holdKiosk() || !Prefs.isPlayActive() || !screenOn) return
        val top = topPackage() ?: return
        if (top in Kiosk.homePackages(this)) Kiosk.bringPlayHome(this)
    }

    private fun topPackage(): String? {
        val usm = getSystemService(UsageStatsManager::class.java) ?: return null
        val now = System.currentTimeMillis()
        return try {
            val ev = usm.queryEvents(now - 8000, now)
            val e = UsageEvents.Event()
            var last: String? = null
            while (ev.hasNextEvent()) {
                ev.getNextEvent(e)
                @Suppress("DEPRECATION")
                if (e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) last = e.packageName
            }
            last
        } catch (_: Exception) {
            null
        }
    }

    private fun expire() {
        if (!Prefs.isPlayActive()) {
            startForeground(NOTE_ID, note())
            return
        }
        Prefs.endPlay()
        releaseWake()
        Watch.log(this, "play end")
        cancelExpire()
        val launch = Intent(this, LauncherActivity::class.java)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            .putExtra("expired", true)
        val full = PendingIntent.getActivity(
            this, 2, launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(
            2,
            NotificationCompat.Builder(this, CH_END)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle("时间到了，继续做题")
                .setContentIntent(full)
                .setFullScreenIntent(full, true)
                .build()
        )
        Kiosk.bringStudy(this)
        startForeground(NOTE_ID, note())
        armWatch(this)
    }

    private fun armExpire() {
        val left = Prefs.playRemainingMs()
        if (left <= 0L || !screenOn) {
            cancelExpire()
            return
        }
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        val at = SystemClock.elapsedRealtime() + left
        try {
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, expirePi())
            } else {
                am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, expirePi())
            }
        } catch (_: Exception) {
            am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, expirePi())
        }
    }

    private fun cancelExpire() {
        (getSystemService(ALARM_SERVICE) as AlarmManager).cancel(expirePi())
    }

    private fun expirePi(): PendingIntent {
        val i = Intent(this, KeepAliveService::class.java).setAction(ACTION_EXPIRE)
        return PendingIntent.getForegroundService(
            this, 3, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannels() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CH, "小学练习", NotificationManager.IMPORTANCE_DEFAULT)
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_PLAY, "自由时间", NotificationManager.IMPORTANCE_HIGH)
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_END, "时间到", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    private fun note(): Notification {
        val home = Perms.homeOn(this) || Kiosk.isOwner(this)
        val play = Prefs.isPlayActive()
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, LauncherActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val ch = if (play) CH_PLAY else CH
        val b = NotificationCompat.Builder(this, ch)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(if (play) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_SERVICE)
        return when {
            play && screenOn -> {
                val ms = Prefs.playRemainingMs()
                b.setContentTitle("还可以玩 ${fmt(ms)}")
                    .setContentText("到点会回到练习")
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setWhen(System.currentTimeMillis() + ms)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()
            }
            play -> b.setContentTitle("计时已暂停").setContentText("亮屏后继续扣时间").build()
            !home -> b.setContentTitle("默认桌面被改走了").setContentText("点这里，再设成默认桌面").build()
            else -> b.setContentTitle("小学练习桌面运行中").setContentText("下拉可回到练习").build()
        }
    }

    companion object {
        private const val CH = "desk2"
        private const val CH_PLAY = "play_live"
        private const val CH_END = "play_end"
        private const val NOTE_ID = 8
        const val ACTION_WATCH = "com.zhiwei.primaryschool.WATCH"
        const val ACTION_EXPIRE = "com.zhiwei.primaryschool.EXPIRE"

        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, KeepAliveService::class.java))
        }

        fun armWatch(ctx: Context, delayMs: Long = if (Prefs.isPlayActive()) 12_000L else 45_000L) {
            Prefs.init(ctx)
            val am = ctx.getSystemService(ALARM_SERVICE) as AlarmManager
            val at = SystemClock.elapsedRealtime() + delayMs
            val pi = PendingIntent.getBroadcast(
                ctx, 9,
                Intent(ctx, WatchAlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, pi)
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, pi)
                }
            } catch (_: Exception) {
                am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, pi)
            }
        }

        fun fmt(ms: Long): String {
            val sec = (ms / 1000).coerceAtLeast(0)
            return "${sec / 60}:${(sec % 60).toString().padStart(2, '0')}"
        }
    }
}
