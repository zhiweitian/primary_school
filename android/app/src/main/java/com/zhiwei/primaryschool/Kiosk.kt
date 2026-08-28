package com.zhiwei.primaryschool

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

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
        try {
            dpm.setStatusBarDisabled(admin, true)
        } catch (_: Exception) {
        }
    }

    fun setPlayMode(activity: Activity, play: Boolean) {
        setupOwner(activity)
        val dpm = dpm(activity)
        if (isOwner(activity)) {
            try {
                dpm.setStatusBarDisabled(admin(activity), !play)
            } catch (_: Exception) {
            }
        }
        try {
            if (play) activity.stopLockTask() else activity.startLockTask()
        } catch (_: Exception) {
        }
    }

    fun lockNow(ctx: Context) {
        if (!isOwner(ctx)) return
        try {
            dpm(ctx).lockNow()
        } catch (_: Exception) {
        }
    }
}
