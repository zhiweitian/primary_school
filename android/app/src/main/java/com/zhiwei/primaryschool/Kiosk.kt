package com.zhiwei.primaryschool

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager

object Kiosk {
    fun admin(ctx: Context) = ComponentName(ctx, KioskAdminReceiver::class.java)

    fun dpm(ctx: Context) =
        ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    fun isOwner(ctx: Context) = dpm(ctx).isDeviceOwnerApp(ctx.packageName)

    fun setupOwner(ctx: Context) {
        if (!isOwner(ctx)) return
        val dpm = dpm(ctx)
        val admin = admin(ctx)
        dpm.setLockTaskPackages(admin, arrayOf(ctx.packageName))
        dpm.setUninstallBlocked(admin, ctx.packageName, true)
        val filter = IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        dpm.addPersistentPreferredActivity(
            admin,
            filter,
            ComponentName(ctx, LauncherActivity::class.java)
        )
        try {
            dpm.setKeyguardDisabled(admin, true)
        } catch (_: Exception) {
        }
        grantPerms(ctx)
        setOthersSuspended(ctx, !Prefs.isPlayActive())
    }

    fun grantPerms(ctx: Context) {
        if (!isOwner(ctx)) return
        val dpm = dpm(ctx)
        val admin = admin(ctx)
        val perms = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.SCHEDULE_EXACT_ALARM
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

    fun otherLaunchers(ctx: Context): Array<String> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map { it.activityInfo.packageName }
            .filter { it != ctx.packageName }
            .distinct()
            .toTypedArray()
    }

    fun setOthersSuspended(ctx: Context, suspended: Boolean) {
        if (!isOwner(ctx)) return
        try {
            dpm(ctx).setPackagesSuspended(admin(ctx), otherLaunchers(ctx), suspended)
        } catch (_: Exception) {
        }
    }

    fun bringStudy(ctx: Context) {
        setOthersSuspended(ctx, true)
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

    fun setPlayMode(activity: Activity, play: Boolean) {
        setupOwner(activity)
        if (isOwner(activity)) {
            try {
                dpm(activity).setStatusBarDisabled(admin(activity), !play)
            } catch (_: Exception) {
            }
            setOthersSuspended(activity, !play)
        }
        try {
            if (play) activity.stopLockTask() else activity.startLockTask()
        } catch (_: Exception) {
        }
    }
}
