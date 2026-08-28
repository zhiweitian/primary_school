package com.zhiwei.primaryschool

import android.content.Context
import android.os.Process
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Watch {
    private val fmt = SimpleDateFormat("MM-dd HH:mm:ss", Locale.US)

    fun log(ctx: Context, msg: String) {
        Prefs.touchBeat()
        val line = "${fmt.format(Date())} p${Process.myPid()} $msg"
        try {
            val f = File(ctx.applicationContext.filesDir, "watch.log")
            f.appendText(line + "\n")
            val lines = f.readLines()
            if (lines.size > 80) f.writeText(lines.takeLast(60).joinToString("\n") + "\n")
        } catch (_: Exception) {
        }
    }

    fun dump(ctx: Context): String {
        val last = Prefs.lastBeat()
        val gap = if (last == 0L) "无" else "${(System.currentTimeMillis() - last) / 1000} 秒前"
        val log = try {
            File(ctx.applicationContext.filesDir, "watch.log").readText()
        } catch (_: Exception) {
            "（还没有日志）"
        }
        return "默认桌面=${Perms.homeOn(ctx)}\n无障碍=${Perms.accessOn(ctx)}\n设备所有者=${Kiosk.isOwner(ctx)}\n自由时间=${Prefs.isPlayActive()}\n上次心跳 $gap\n\n$log"
    }
}
