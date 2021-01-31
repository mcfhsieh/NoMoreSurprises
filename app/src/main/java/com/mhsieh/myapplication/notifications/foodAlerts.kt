package com.mhsieh.myapplication.notifications

import android.app.Notification.DEFAULT_ALL
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.getActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color.RED
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.media.RingtoneManager.getDefaultUri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.work.ListenableWorker.Result.success
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mhsieh.myapplication.MainActivity
import com.mhsieh.myapplication.R
import com.mhsieh.myapplication.util.vectorToBitmap
import timber.log.Timber

class FoodAlerts(appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {


    lateinit var foodName:String

    companion object{
        const val NOTIFICATION_ID = "com.mhsieh.myapplication.notifications"
        const val NOTIFICATION_NAME = "NoSurprises"
        const val NOTIFICATION_CHANNEL = "NoSuprises_channel_01"
        const val NOTIFICATION_WORK = "NoSurprises_notification_work"
        const val REQUEST_CODE = 10
    }


    override fun doWork(): Result {
        val id =  inputData.getLong(NOTIFICATION_ID, 0).toInt()
       // foodName = inputData.getString("Food Name").toString()
        sendFoodAlert(id)
        return success()
    }

    fun sendFoodAlert(id:Int){
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(NOTIFICATION_ID, id)
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as
                    NotificationManager
        val bitmap = applicationContext.vectorToBitmap(R.drawable.ic_new_food_24)
        val titleNotification = "$foodName is about to expire"
        val subtitleNotification = applicationContext.getString(R.string.notification_subtitle)
        val pendingIntent = getActivity(applicationContext, 0, intent, 0)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setLargeIcon(bitmap).setSmallIcon(R.drawable.calendar_icon)
            .setContentTitle(titleNotification).setContentText(subtitleNotification)
            .setDefaults(DEFAULT_ALL).setContentIntent(pendingIntent).setAutoCancel(true)
        notification.priority = PRIORITY_MAX
        if (SDK_INT >= O) {
            notification.setChannelId(NOTIFICATION_CHANNEL)

            val ringtoneManager = getDefaultUri(TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder().setUsage(USAGE_NOTIFICATION_RINGTONE)
                .setContentType(CONTENT_TYPE_SONIFICATION).build()

            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, IMPORTANCE_HIGH)

            channel.enableLights(true)
            channel.lightColor = RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channel.setSound(ringtoneManager, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(id, notification.build())
    }
}
