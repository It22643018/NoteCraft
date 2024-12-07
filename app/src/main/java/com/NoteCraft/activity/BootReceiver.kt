package com.kikunote.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.d("BootReceiver", "Device rebooted, rescheduling alarms")

        }
    }
}
