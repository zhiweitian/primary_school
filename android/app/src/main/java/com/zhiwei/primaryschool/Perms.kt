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
import android.os.PowerManager
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

    fun batteryOn(ctx: Context): Boolean {
        val pm = ctx.getSystemService(PowerManager::class.java) ?: return true
        return pm.isIgnoringBatteryOptimizations(ctx.packageName)
    }

    fun readyForPlay(ctx: Context) = notifyOn(ctx)

    fun nextMissing(ctx: Context, skip: Set<String> = emptySet()): String? {
        if ("notify" !in skip && !notifyOn(ctx)) return "notify"
        if ("usage" !in skip && !usageOn(ctx)) return "usage"
        if ("battery" !in skip && !batteryOn(ctx)) return "battery"
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
                "popup" -> {
                    Prefs.setSawPopup()
                    openPopup(activity)
                }
                "usage" -> activity.startActivity(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(NEW_TASK)
                )
                "battery" -> activity.startActivity(
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, pkg)
                        .addFlags(NEW_TASK)
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

    private fun openPopup(activity: Activity) {
        val pkg = activity.packageName
        val tries = listOf(
            Intent("miui.intent.action.APP_PERM_EDITOR")
                .putExtra("extra_pkgname", pkg),
            Intent("huawei.intent.action.NOTIFICATIONMANAGER"),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg"))
        )
        for (i in tries) {
            try {
                activity.startActivity(i.addFlags(NEW_TASK))
                return
            } catch (_: Exception) {
            }
        }
    }

    fun label(kind: String) = when (kind) {
        "notify" -> "通知"
        "overlay" -> "悬浮窗"
        "popup" -> "后台弹出（部分平板需要）"
        "usage" -> "使用情况访问"
        "battery" -> "关闭电池优化"
        "home" -> "默认桌面（可跳过）"
        "alarm" -> "精确闹钟"
        else -> kind
    }

    fun hint(kind: String) = when (kind) {
        "notify" -> "用来在通知栏显示剩余时间。"
        "overlay" -> "打开后，玩游戏时也能看到倒计时。在下一页打开「显示在其他应用上层」。"
        "popup" -> "有的平板过一会会把浮窗收掉。下一页进应用信息，打开「后台弹出界面」「后台显示悬浮窗」（没有这些开关可跳过）。"
        "usage" -> "用来在时间到时把孩子拉回练习。下一页找到「小学练习」并打开。"
        "battery" -> "关掉电池优化，系统才不会在后台把这个桌面杀掉。原系统桌面是系统应用，杀不掉。"
        "home" -> "设成默认桌面后，上划会回到本 App。不想改可以点跳过。"
        "alarm" -> "让到点更准时。下一页允许精确闹钟。"
        else -> ""
    }
}
