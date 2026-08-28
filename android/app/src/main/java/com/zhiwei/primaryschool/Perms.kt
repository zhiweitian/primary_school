package com.zhiwei.primaryschool

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.role.RoleManager
import android.content.ComponentName
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

    fun missingKinds(ctx: Context): List<String> {
        val out = mutableListOf<String>()
        if (!notifyOn(ctx)) out += "notify"
        if (!usageOn(ctx)) out += "usage"
        if (!batteryOn(ctx)) out += "battery"
        if (!Prefs.sawOem()) out += "oem"
        if (!homeOn(ctx)) out += "home"
        if (!alarmOn(ctx)) out += "alarm"
        return out
    }

    fun nextMissing(ctx: Context, skip: Set<String> = emptySet()): String? {
        return missingKinds(ctx).firstOrNull { it !in skip }
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
                "usage" -> activity.startActivity(
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(NEW_TASK)
                )
                "battery" -> openBattery(activity)
                "oem" -> openOem(activity)
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

    private fun openBattery(activity: Activity) {
        val name = activity.packageName
        val pkg = Uri.parse("package:$name")
        val tries = listOf(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(pkg),
            Intent().setComponent(
                ComponentName(
                    "com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                )
            ).putExtra("package_name", name).putExtra("package_label", "小学练习"),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(pkg)
        )
        for (i in tries) {
            try {
                activity.startActivity(i)
                if (i.action == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
                    Toast.makeText(
                        activity,
                        "打开「电池 / 耗电管理」，设为不限制或不优化",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            } catch (_: Exception) {
            }
        }
        Toast.makeText(activity, "请到设置 → 应用 → 小学练习 → 电池，关掉优化", Toast.LENGTH_LONG).show()
    }

    private fun openOem(activity: Activity) {
        val pkg = Uri.parse("package:" + activity.packageName)
        try {
            activity.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(pkg)
            )
        } catch (_: Exception) {
            Toast.makeText(activity, "请到设置 → 应用 → 小学练习", Toast.LENGTH_LONG).show()
        }
    }

    fun oemGuide() = """
        很多平板没有「自启动」这个名字，请手动找：

        1. 下一页是「小学练习」应用信息。点「电池 / 耗电 / 用电管理」，选「不限制」或「允许后台活动」。

        2. 上划打开多任务，长按「小学练习」，点锁定（小锁图标）。

        3. 若在系统设置里搜到「自启动」「后台运行」，也把小学练习打开。

        找不到可以先跳过。专用平板仍建议用电脑设设备所有者。
    """.trimIndent()

    fun label(kind: String) = when (kind) {
        "notify" -> "通知"
        "usage" -> "使用情况访问"
        "battery" -> "关闭电池优化"
        "oem" -> "后台保活（手动）"
        "home" -> "默认桌面（可跳过）"
        "alarm" -> "精确闹钟"
        else -> kind
    }

    fun hint(kind: String) = when (kind) {
        "notify" -> "用来在通知栏显示剩余时间。"
        "usage" -> "用来在时间到时把孩子拉回练习。下一页找到「小学练习」并打开。"
        "battery" -> "系统会问是否允许忽略电池优化，选允许。如果进了应用列表，点右上角「全部」，搜「小学练习」。找不到就到应用信息里的电池，设为不优化。"
        "oem" -> oemGuide()
        "home" -> "设成默认桌面后，上划会回到本 App。不想改可以点跳过。"
        "alarm" -> "让到点更准时。下一页允许精确闹钟。"
        else -> ""
    }
}
