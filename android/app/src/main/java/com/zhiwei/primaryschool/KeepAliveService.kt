package com.zhiwei.primaryschool

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.NotificationCompat

class KeepAliveService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var homeWasOn = true
    private val loop = object : Runnable {
        override fun run() {
            guard()
            handler.postDelayed(this, 15_000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        val gap = System.currentTimeMillis() - Prefs.lastBeat()
        Watch.log(
            this,
            "KeepAlive create gap=${gap / 1000}s home=${Perms.homeOn(this)} play=${Prefs.isPlayActive()}"
        )
        if (Prefs.lastBeat() > 0L && gap > 30_000L) {
            Watch.log(this, "likely killed (no onDestroy)")
        }
        startForeground(NOTE_ID, note())
        handler.post(loop)
        armWatch()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTE_ID, note())
        if (intent?.action == ACTION_WATCH) {
            Watch.log(this, "alarm")
            armWatch()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Watch.log(this, "KeepAlive destroy")
        handler.removeCallbacks(loop)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun guard() {
        Watch.log(
            this,
            "beat home=${Perms.homeOn(this)} play=${Prefs.isPlayActive()}"
        )
        if (Prefs.isPlayActive()) PlayTimerService.start(this)
        val on = Perms.homeOn(this) || Kiosk.isOwner(this)
        if (on != homeWasOn) {
            homeWasOn = on
            Watch.log(this, "home changed -> $on")
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTE_ID, note())
        }
        armWatch()
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
            NotificationChannel(CH, "桌面保活", NotificationManager.IMPORTANCE_LOW)
        )
        val home = Perms.homeOn(this) || Kiosk.isOwner(this)
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, LauncherActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CH)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentTitle(if (home) "小学练习桌面运行中" else "默认桌面被改走了")
            .setContentText(if (home) "避免系统把桌面杀掉" else "点这里，再设成默认桌面")
            .build()
    }

    companion object {
        private const val CH = "desk"
        private const val NOTE_ID = 8
        const val ACTION_WATCH = "com.zhiwei.primaryschool.WATCH"
        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, KeepAliveService::class.java))
        }
    }
}
