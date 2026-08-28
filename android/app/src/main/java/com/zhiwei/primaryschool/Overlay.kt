package com.zhiwei.primaryschool

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView

object Overlay {
    private var tv: TextView? = null

    fun tick(ctx: Context) {
        val app = ctx.applicationContext
        if (!Prefs.isPlayActive()) {
            tv?.visibility = View.GONE
            return
        }
        ensure(app)
        val v = tv ?: return
        v.visibility = View.VISIBLE
        v.text = "剩余 ${PlayTimerService.fmt(Prefs.playRemainingMs())}"
    }

    fun ensure(ctx: Context) {
        val app = ctx.applicationContext
        if (!Settings.canDrawOverlays(app)) return
        val cur = tv
        if (cur != null && cur.isAttachedToWindow) return
        if (cur != null) {
            try {
                wm(app).removeView(cur)
            } catch (_: Exception) {
            }
            tv = null
        }
        val v = TextView(app).apply {
            text = "剩余 ${PlayTimerService.fmt(Prefs.playRemainingMs())}"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 16f
            setPadding(28, 16, 28, 16)
            setBackgroundColor(0xE6E65100.toInt())
            setOnClickListener { Kiosk.bringPlayHome(app) }
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            y = 24
            x = 16
            if (Build.VERSION.SDK_INT >= 28) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        try {
            wm(app).addView(v, lp)
            tv = v
        } catch (_: Exception) {
        }
    }

    fun hide() {
        tv?.visibility = View.GONE
    }

    private fun wm(ctx: Context) =
        ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
}
