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
    private const val NEW_TASK = Intent.FLAG_ACTIVITY_NEW_TASK

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

    fun nextMissing(ctx: Context, skip: Set<String> = emptySet()): String? {
        if ("notify" !in skip && !notifyOn(ctx)) return "notify"
        if ("overlay" !in skip && !overlayOn(ctx)) return "overlay"
        if ("usage" !in skip && !usageOn(ctx)) return "usage"
        if ("home" !in skip && !homeOn(ctx)) return "home"
        if ("alarm" !in skip && !alarmOn(ctx)) return "alarm"
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
                "overlay" -> activity.startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, pkg).addFlags(NEW_TASK)
                )
                "usage" -> activity.startActivity(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(NEW_TASK)
                )
                "home" -> activity.startActivity(
                    Intent(Settings.ACTION_HOME_SETTINGS).addFlags(NEW_TASK)
                )
                "alarm" -> {
                    if (Build.VERSION.SDK_INT >= 31) {
                        activity.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, pkg)
                                .addFlags(NEW_TASK)
                        )
                    }
                }
            }
        } catch (_: Exception) {
            Toast.makeText(activity, "打不开系统页，请到设置里手动打开", Toast.LENGTH_LONG).show()
        }
    }

    fun label(kind: String) = when (kind) {
        "notify" -> "通知"
        "overlay" -> "悬浮窗"
        "usage" -> "使用情况访问"
        "home" -> "默认桌面（可跳过）"
        "alarm" -> "精确闹钟"
        else -> kind
    }

    fun hint(kind: String) = when (kind) {
        "notify" -> "用来在通知栏显示剩余时间。"
        "overlay" -> "打开后，玩游戏时也能看到倒计时。在下一页打开「显示在其他应用上层」。"
        "usage" -> "用来在时间到时把孩子拉回练习。下一页找到「小学练习」并打开。"
        "home" -> "设成默认桌面后，上划会回到本 App。不想改可以点跳过。"
        "alarm" -> "让到点更准时。下一页允许精确闹钟。"
        else -> ""
    }
}
