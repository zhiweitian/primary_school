package com.zhiwei.primaryschool

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat

class PlayTimerService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var lastRt = 0L
    private var screenOn = true

    private val screenRecv = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    tick(force = true)
                    screenOn = false
                    lastRt = 0L
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    screenOn = true
                    lastRt = SystemClock.elapsedRealtime()
                }
            }
        }
    }

    private val loop = object : Runnable {
        override fun run() {
            tick()
            if (!Prefs.isPlayActive()) {
                expire()
                return
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        screenOn = pm.isInteractive
        lastRt = if (screenOn) SystemClock.elapsedRealtime() else 0L
        val f = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenRecv, f)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureChannel()
        startForeground(1, note())
        handler.removeCallbacks(loop)
        handler.post(loop)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(loop)
        try {
            unregisterReceiver(screenRecv)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun tick(force: Boolean = false) {
        val now = SystemClock.elapsedRealtime()
        if (screenOn && lastRt > 0) {
            Prefs.consumePlay(now - lastRt)
        }
        lastRt = if (screenOn) now else 0L
        if (!force && Prefs.isPlayActive()) {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(1, note())
        }
    }

    private fun expire() {
        Prefs.endPlay()
        Kiosk.lockNow(this)
        val launch = Intent(this, LauncherActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
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
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        )
        startActivity(launch)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun ensureChannel() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CH, "自由时间", NotificationManager.IMPORTANCE_LOW)
        )
    }

    private fun note(): Notification {
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, LauncherActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val ms = Prefs.playRemainingMs()
        val m = (ms / 1000) / 60
        val s = (ms / 1000) % 60
        return NotificationCompat.Builder(this, CH)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentTitle("还可以玩 ${m}:${s.toString().padStart(2, '0')}")
            .setContentText("亮屏时间到点后会回到练习")
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    companion object {
        private const val CH = "play"
        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, PlayTimerService::class.java))
        }
        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, PlayTimerService::class.java))
        }
    }
}
