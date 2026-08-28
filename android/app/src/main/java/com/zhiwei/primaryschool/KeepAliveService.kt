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

    private val screenRecv = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Prefs.pausePlayClock()
                    screenOn = false
                    if (Prefs.isPlayActive()) armExpire() else cancelExpire()
                    show()
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    screenOn = true
                    Prefs.resumePlayClock()
                    if (Prefs.isPlayActive()) armExpire()
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
        if (Prefs.lastBeat() > 0L && idle > 20_000L) {
            Watch.log(this, "killed idle=${idle / 1000}s (no destroy)")
        }
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        screenOn = pm.isInteractive
        homeWasOn = Perms.homeOn(this)
        Watch.log(this, "KeepAlive create idle=${idle / 1000}s home=$homeWasOn play=${Prefs.isPlayActive()} bat=${Perms.batteryOn(this)}")
        registerReceiver(
            screenRecv,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
        )
        startForeground(NOTE_ID, note())
        handler.post(loop)
        armWatch()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTE_ID, note())
        when (intent?.action) {
            ACTION_EXPIRE -> expire()
            ACTION_WATCH -> {
                Watch.log(this, "alarm")
                armWatch()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Watch.log(this, "KeepAlive destroy")
        handler.removeCallbacks(loop)
        cancelExpire()
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
        if (Prefs.isPlayActive()) {
            val sec = Prefs.playRemainingMs() / 1000
            if (sec != lastSec) {
                lastSec = sec
                show()
            }
            police()
            armExpire()
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
            armWatch()
        }
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
        nm.createNotificationChannel(
            NotificationChannel("play_end", "时间到", NotificationManager.IMPORTANCE_HIGH)
        )
        nm.notify(
            2,
            NotificationCompat.Builder(this, "play_end")
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle("时间到了，继续做题")
                .setContentIntent(full)
                .setFullScreenIntent(full, true)
                .build()
        )
        Kiosk.bringStudy(this)
        startForeground(NOTE_ID, note())
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

    private fun armWatch() {
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        val at = SystemClock.elapsedRealtime() + 45_000L
        try {
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, watchPi())
            } else {
                am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, watchPi())
            }
        } catch (_: Exception) {
            am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, watchPi())
        }
    }

    private fun watchPi(): PendingIntent {
        val i = Intent(this, KeepAliveService::class.java).setAction(ACTION_WATCH)
        return PendingIntent.getForegroundService(
            this, 9, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun note(): Notification {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CH, "小学练习", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val home = Perms.homeOn(this) || Kiosk.isOwner(this)
        val play = Prefs.isPlayActive()
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, LauncherActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val b = NotificationCompat.Builder(this, CH)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        return when {
            play && screenOn -> {
                val ms = Prefs.playRemainingMs()
                val sec = ms / 1000
                if (sec != lastSec) lastSec = sec
                b.setContentTitle("还可以玩 ${fmt(ms)}")
                    .setContentText("到点会回到练习")
                    .setUsesChronometer(true)
                    .setChronometerCountDown(true)
                    .setWhen(System.currentTimeMillis() + ms)
                    .build()
            }
            play -> b.setContentTitle("计时已暂停").setContentText("亮屏后继续扣时间").build()
            !home -> b.setContentTitle("默认桌面被改走了").setContentText("点这里，再设成默认桌面").build()
            else -> b.setContentTitle("小学练习桌面运行中").setContentText("下拉可回到练习").build()
        }
    }

    companion object {
        private const val CH = "desk2"
        private const val NOTE_ID = 8
        const val ACTION_WATCH = "com.zhiwei.primaryschool.WATCH"
        const val ACTION_EXPIRE = "com.zhiwei.primaryschool.EXPIRE"

        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, KeepAliveService::class.java))
        }

        fun fmt(ms: Long): String {
            val sec = (ms / 1000).coerceAtLeast(0)
            return "${sec / 60}:${(sec % 60).toString().padStart(2, '0')}"
        }
    }
}
