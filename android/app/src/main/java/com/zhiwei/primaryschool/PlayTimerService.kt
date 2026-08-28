package com.zhiwei.primaryschool

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat

class PlayTimerService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var screenOn = true
    private var overlay: TextView? = null

    private val screenRecv = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Prefs.pausePlayClock()
                    screenOn = false
                    cancelAlarm()
                    paint()
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    screenOn = true
                    Prefs.resumePlayClock()
                    armAlarm()
                    paint()
                }
            }
        }
    }

    private val loop = object : Runnable {
        override fun run() {
            if (!Prefs.isPlayActive()) {
                expire()
                return
            }
            paint()
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        screenOn = pm.isInteractive
        if (screenOn) Prefs.resumePlayClock() else Prefs.pausePlayClock()
        registerReceiver(
            screenRecv,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
        )
        showOverlay()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ensureChannel()
        startForeground(1, note())
        if (intent?.action == ACTION_EXPIRE || !Prefs.isPlayActive()) {
            expire()
            return START_NOT_STICKY
        }
        handler.removeCallbacks(loop)
        handler.post(loop)
        armAlarm()
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(loop)
        cancelAlarm()
        hideOverlay()
        try {
            unregisterReceiver(screenRecv)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun paint() {
        if (!Prefs.isPlayActive()) return
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, note())
        overlay?.text = "剩余 ${fmt(Prefs.playRemainingMs())}"
    }

    private fun expire() {
        handler.removeCallbacks(loop)
        cancelAlarm()
        hideOverlay()
        Prefs.endPlay()
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
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        )
        Kiosk.bringStudy(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun armAlarm() {
        val left = Prefs.playRemainingMs()
        if (left <= 0L || !screenOn) {
            cancelAlarm()
            return
        }
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        val at = SystemClock.elapsedRealtime() + left
        try {
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, alarmPi())
            } else {
                am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, alarmPi())
            }
        } catch (_: Exception) {
            am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, at, alarmPi())
        }
    }

    private fun cancelAlarm() {
        (getSystemService(ALARM_SERVICE) as AlarmManager).cancel(alarmPi())
    }

    private fun alarmPi(): PendingIntent {
        val i = Intent(this, PlayTimerService::class.java).setAction(ACTION_EXPIRE)
        return PendingIntent.getForegroundService(
            this, 3, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showOverlay() {
        if (overlay != null) return
        if (!Settings.canDrawOverlays(this)) return
        val tv = TextView(this).apply {
            text = "剩余 ${fmt(Prefs.playRemainingMs())}"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 16f
            setPadding(28, 16, 28, 16)
            setBackgroundColor(0xE6E65100.toInt())
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            y = 24
            x = 16
        }
        try {
            (getSystemService(WINDOW_SERVICE) as WindowManager).addView(tv, lp)
            overlay = tv
        } catch (_: Exception) {
        }
    }

    private fun hideOverlay() {
        val tv = overlay ?: return
        overlay = null
        try {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(tv)
        } catch (_: Exception) {
        }
    }

    private fun ensureChannel() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(CH, "自由时间", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    private fun note(): Notification {
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, LauncherActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val ms = Prefs.playRemainingMs()
        val b = NotificationCompat.Builder(this, CH)
            .setSmallIcon(R.drawable.ic_stat)
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
        return if (screenOn) {
            b.setContentTitle("还可以玩 ${fmt(ms)}")
                .setContentText("到点会回到练习")
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(System.currentTimeMillis() + ms)
                .build()
        } else {
            b.setContentTitle("计时已暂停")
                .setContentText("亮屏后继续扣时间")
                .build()
        }
    }

    companion object {
        private const val CH = "play"
        const val ACTION_EXPIRE = "com.zhiwei.primaryschool.EXPIRE"
        fun start(ctx: Context) {
            ctx.startForegroundService(Intent(ctx, PlayTimerService::class.java))
        }
        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, PlayTimerService::class.java))
        }
        fun fmt(ms: Long): String {
            val sec = (ms / 1000).coerceAtLeast(0)
            return "${sec / 60}:${(sec % 60).toString().padStart(2, '0')}"
        }
    }
}
