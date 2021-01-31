package com.mhsieh.myapplication.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import timber.log.Timber

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ALARM_ACTION") {
            val foodName = intent.getStringExtra("Food Name")
            NotificationManager.enqueueWork(context, intent)
        }

    }
}