package com.mhsieh.myapplication.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.mhsieh.myapplication.MainActivity
import com.mhsieh.myapplication.MainFragment
import com.mhsieh.myapplication.R
import com.mhsieh.myapplication.util.vectorToBitmap
import timber.log.Timber

class NotificationManager : JobIntentService() {

    lateinit var foodName: String
    var foodId = 0

    companion object {
        const val NOTIFICATION_ID = "com.mhsieh.myapplication.notifications"
        const val NOTIFICATION_NAME = "NoSurprises"
        const val NOTIFICATION_CHANNEL = "NoSurprises_channel_01"
        const val REQUEST_CODE = 10
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                com.mhsieh.myapplication.notifications.NotificationManager::class.java,
                1,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        foodName = intent.getStringExtra("Food Name").toString()
        foodId = intent.getIntExtra("Food Id", 0)
//        if(shelfLife > 2){
//            sendFoodAlert(1)
//        }
        sendFoodAlert(1)
    }

    private fun sendFoodAlert(id: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(NOTIFICATION_ID, id)
        intent.putExtra("FoodId", foodId)
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as
                    NotificationManager
        val bitmap = applicationContext.vectorToBitmap(R.drawable.ic_new_food_24)
        val titleNotification = "Consider eating $foodName for dinner"
        val subtitleNotification = applicationContext.getString(R.string.notification_subtitle)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            0)

        val notification = NotificationCompat.Builder(
            applicationContext,
            NOTIFICATION_CHANNEL
        )   .setLargeIcon(bitmap)
            .setSmallIcon(R.drawable.calendar_icon)
            .setContentTitle(titleNotification)
            .setContentText(subtitleNotification)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setChannelId(NOTIFICATION_CHANNEL)
            val ringtoneManager =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes =
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL,
                    NOTIFICATION_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channel.setSound(ringtoneManager, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(id, notification.build())
    }


}