package com.mhsieh.myapplication.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.mhsieh.myapplication.notifications.FoodAlerts.Companion.REQUEST_CODE
import timber.log.Timber

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ALARM_ACTION") {
            NotificationManager.enqueueWork(context, intent)
        }
    }
}