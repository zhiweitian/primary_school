package com.zhiwei.primaryschool

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class KioskAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        Kiosk.setupOwner(context)
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        Kiosk.setupOwner(context)
    }
}
