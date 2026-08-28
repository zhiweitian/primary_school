package com.zhiwei.primaryschool

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build

object Kiosk {
    fun admin(ctx: Context) = ComponentName(ctx, KioskAdminReceiver::class.java)

    fun dpm(ctx: Context) =
        ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    fun isOwner(ctx: Context) = dpm(ctx).isDeviceOwnerApp(ctx.packageName)

    fun setupOwner(ctx: Context) {
        if (!isOwner(ctx)) return
        val dpm = dpm(ctx)
        val admin = admin(ctx)
        dpm.setUninstallBlocked(admin, ctx.packageName, true)
        val filter = IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        for (pkg in homePackages(ctx)) {
            try {
                dpm.clearPackagePersistentPreferredActivities(admin, pkg)
            } catch (_: Exception) {
            }
        }
        dpm.addPersistentPreferredActivity(
            admin,
            filter,
            ComponentName(ctx, LauncherActivity::class.java)
        )
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
            } catch (_: Exception) {
            }
        }
        try {
            dpm.setKeyguardDisabled(admin, true)
        } catch (_: Exception) {
        }
        grantPerms(ctx)
        applyRestrictions(ctx, Prefs.isPlayActive())
    }

    fun grantPerms(ctx: Context) {
        if (!isOwner(ctx)) return
        val dpm = dpm(ctx)
        val admin = admin(ctx)
        val perms = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.SCHEDULE_EXACT_ALARM,
            Manifest.permission.PACKAGE_USAGE_STATS
        )
        for (perm in perms) {
            try {
                dpm.setPermissionGrantState(
                    admin,
                    ctx.packageName,
                    perm,
                    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                )
            } catch (_: Exception) {
            }
        }
    }

    fun homePackages(ctx: Context): Set<String> {
        val pm = ctx.packageManager
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val listed = pm.queryIntentActivities(home, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }
        val resolved = pm.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
        return (listed + listOfNotNull(resolved)).filter { it != ctx.packageName }.toSet()
    }

    fun otherLaunchers(ctx: Context): Array<String> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }
            .filter { it != ctx.packageName }
            .distinct()
            .toTypedArray()
    }

    fun playablePackages(ctx: Context): Array<String> {
        val homes = homePackages(ctx)
        return otherLaunchers(ctx).filter { it !in homes }.toTypedArray()
    }

    fun applyRestrictions(ctx: Context, play: Boolean) {
        if (!isOwner(ctx)) return
        val dpm = dpm(ctx)
        val admin = admin(ctx)
        val homes = homePackages(ctx).toTypedArray()
        val games = playablePackages(ctx)
        try {
            dpm.setPackagesSuspended(admin, homes, true)
        } catch (_: Exception) {
        }
        for (pkg in homes) {
            try {
                dpm.setApplicationHidden(admin, pkg, true)
            } catch (_: Exception) {
            }
        }
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)
            } catch (_: Exception) {
            }
        }
        try {
            dpm.setPackagesSuspended(admin, games, !play)
        } catch (_: Exception) {
        }
        val lock = if (play) arrayOf(ctx.packageName) + games else arrayOf(ctx.packageName)
        try {
            dpm.setLockTaskPackages(admin, lock)
        } catch (_: Exception) {
        }
    }

    fun allowExtra(activity: Activity, pkg: String) {
        if (!isOwner(activity)) {
            try {
                activity.stopLockTask()
            } catch (_: Exception) {
            }
            return
        }
        val dpm = dpm(activity)
        val admin = admin(activity)
        val cur = try {
            dpm.getLockTaskPackages(admin)
        } catch (_: Exception) {
            arrayOf(activity.packageName)
        }
        try {
            dpm.setLockTaskPackages(admin, (cur.toList() + pkg).distinct().toTypedArray())
        } catch (_: Exception) {
        }
        try {
            dpm.setPackagesSuspended(admin, arrayOf(pkg), false)
        } catch (_: Exception) {
        }
    }

    fun bringStudy(ctx: Context) {
        applyRestrictions(ctx, false)
        val launch = Intent(ctx, LauncherActivity::class.java)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            .putExtra("expired", true)
        try {
            ctx.startActivity(launch)
        } catch (_: Exception) {
        }
        try {
            ctx.startActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
        }
    }

    fun bringPlayHome(ctx: Context) {
        try {
            ctx.startActivity(
                Intent(ctx, LauncherActivity::class.java)
                    .addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
            )
        } catch (_: Exception) {
        }
    }

    fun setPlayMode(activity: Activity, play: Boolean) {
        setupOwner(activity)
        applyRestrictions(activity, play)
        if (isOwner(activity)) {
            try {
                dpm(activity).setStatusBarDisabled(admin(activity), !play)
            } catch (_: Exception) {
            }
        }
        try {
            if (!isOwner(activity) && play) activity.stopLockTask()
            else activity.startLockTask()
        } catch (_: Exception) {
        }
    }
}
