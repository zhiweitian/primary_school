package com.zhiwei.primaryschool

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class KeepAliveService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var homeWasOn = true
    private val loop = object : Runnable {
        override fun run() {
            paint()
            handler.postDelayed(this, 15_000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        startForeground(NOTE_ID, note())
        handler.post(loop)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTE_ID, note())
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(loop)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun paint() {
        val on = Perms.homeOn(this) || Kiosk.isOwner(this)
        if (on != homeWasOn) {
            homeWasOn = on
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTE_ID, note())
        }
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
        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, KeepAliveService::class.java))
        }
    }
}
