package com.zhiwei.primaryschool

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView

class DeskAccess : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private var tv: TextView? = null
    private val loop = object : Runnable {
        override fun run() {
            tick()
            handler.postDelayed(this, 400)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Prefs.init(this)
        Watch.log(this, "access on")
        KeepAliveService.start(this)
        handler.post(loop)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        Watch.log(this, "access off")
        handler.removeCallbacks(loop)
        drop()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        handler.removeCallbacks(loop)
        drop()
        super.onDestroy()
    }

    private fun tick() {
        if (!Prefs.isPlayActive()) {
            tv?.visibility = View.GONE
            return
        }
        ensure()
        val v = tv ?: return
        v.visibility = View.VISIBLE
        v.text = "剩余 ${PlayTimerService.fmt(Prefs.playRemainingMs())}"
    }

    private fun ensure() {
        val cur = tv
        if (cur != null && cur.isAttachedToWindow) return
        drop()
        val v = TextView(this).apply {
            text = "剩余 ${PlayTimerService.fmt(Prefs.playRemainingMs())}"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 16f
            setPadding(28, 16, 28, 16)
            setBackgroundColor(0xE6E65100.toInt())
            setOnClickListener { Kiosk.bringPlayHome(this@DeskAccess) }
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
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
            (getSystemService(WINDOW_SERVICE) as WindowManager).addView(v, lp)
            tv = v
            Watch.log(this, "access overlay on")
        } catch (e: Exception) {
            Watch.log(this, "access overlay fail ${e.javaClass.simpleName}")
        }
    }

    private fun drop() {
        val v = tv ?: return
        tv = null
        try {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(v)
        } catch (_: Exception) {
        }
    }
}
