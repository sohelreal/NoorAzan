package com.noorazan.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.noorazan.app.util.AlarmScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmScheduler.rescheduleAllAlarms(context)
        }
    }
}
