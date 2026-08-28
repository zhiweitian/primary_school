package com.zhiwei.primaryschool

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.widget.Toast

object Perms {
    const val REQ_NOTIFY = 71

    fun overlayOn(ctx: Context) = Settings.canDrawOverlays(ctx)

    fun notifyOn(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < 33) return true
        return ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun usageOn(ctx: Context): Boolean {
        val ops = ctx.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = ops.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            ctx.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun homeOn(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 29) {
            val rm = ctx.getSystemService(RoleManager::class.java)
            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) && rm.isRoleHeld(RoleManager.ROLE_HOME)) {
                return true
            }
        }
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val r = ctx.packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
        return r?.activityInfo?.packageName == ctx.packageName
    }

    fun alarmOn(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        return (ctx.getSystemService(AlarmManager::class.java))?.canScheduleExactAlarms() == true
    }

    fun readyForPlay(ctx: Context) = overlayOn(ctx) && notifyOn(ctx)

    fun nextMissing(ctx: Context): String? {
        if (!notifyOn(ctx)) return "notify"
        if (!overlayOn(ctx)) return "overlay"
        if (!usageOn(ctx)) return "usage"
        if (!homeOn(ctx)) return "home"
        if (!alarmOn(ctx)) return "alarm"
        return null
    }

    fun open(activity: Activity, kind: String) {
        val pkg = Uri.parse("package:" + activity.packageName)
        try {
            when (kind) {
                "notify" -> {
                    if (Build.VERSION.SDK_INT >= 33) {
                        activity.requestPermissions(
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            REQ_NOTIFY
                        )
                    }
                }
                "overlay" -> {
                    Toast.makeText(activity, "打开「显示在其他应用上层」", Toast.LENGTH_LONG).show()
                    activity.startActivity(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, pkg)
                    )
                }
                "usage" -> {
                    Toast.makeText(activity, "找到「小学练习」，打开使用情况访问", Toast.LENGTH_LONG).show()
                    activity.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
                "home" -> {
                    Toast.makeText(activity, "选「小学练习」作为默认桌面", Toast.LENGTH_LONG).show()
                    if (Build.VERSION.SDK_INT >= 29) {
                        val rm = activity.getSystemService(RoleManager::class.java)
                        if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME)) {
                            activity.startActivity(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
                            return
                        }
                    }
                    activity.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                }
                "alarm" -> {
                    if (Build.VERSION.SDK_INT >= 31) {
                        Toast.makeText(activity, "允许精确闹钟", Toast.LENGTH_LONG).show()
                        activity.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, pkg)
                        )
                    }
                }
            }
        } catch (_: Exception) {
            Toast.makeText(activity, "打不开系统页，请到设置里手动打开", Toast.LENGTH_LONG).show()
        }
    }

    fun label(kind: String) = when (kind) {
        "notify" -> "通知（倒计时）"
        "overlay" -> "显示在其他应用上层（游戏上看到剩余时间）"
        "usage" -> "使用情况访问（到点拉回练习）"
        "home" -> "设为默认桌面（上划回到本 App）"
        "alarm" -> "精确计时"
        else -> kind
    }
}
