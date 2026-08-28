package com.zhiwei.primaryschool

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WatchAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Prefs.init(context)
        Watch.log(context, "alarm recv play=${Prefs.isPlayActive()}")
        KeepAliveService.start(context)
        KeepAliveService.armWatch(context)
    }
}
