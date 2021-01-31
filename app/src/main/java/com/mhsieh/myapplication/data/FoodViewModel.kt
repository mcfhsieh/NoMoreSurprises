package com.mhsieh.myapplication.data

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.mhsieh.myapplication.notifications.AlarmReceiver
import com.mhsieh.myapplication.notifications.FoodAlerts
import com.mhsieh.myapplication.notifications.FoodAlerts.Companion.REQUEST_CODE
import com.mhsieh.myapplication.util.calcShelfLife
import com.mhsieh.myapplication.util.dinerTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class FoodViewModel(app: Application) : AndroidViewModel(app) {

    private var parentJob = Job()
    val allDishes: LiveData<List<FoodData>>
    private lateinit var allFoods: List<FoodData>
    private var repo: FoodRepository
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private var scope = CoroutineScope(coroutineContext)
    val context: Context = getApplication<Application>().applicationContext
    var notificationsOn = false

    init {
        val foodDao = FoodDatabase.getDatabase(app, scope).foodDao()
        repo = FoodRepository(foodDao)
        allDishes = repo.allDishes
    }

    fun updateShelfLife() {
        scope.launch(Dispatchers.IO) {
            allFoods = repo.getAllFoods()
            allFoods.let {
                for (food in it) {
                    if (food.spoiled) {
                        continue
                    } else {
                        food.shelfLife = food.datePrepared.calcShelfLife()
                        update(food)
                    }
                }
            }
            if (!notificationsOn){
                println("notifications disabled: Alarm not set")
                cancelFoodAlarm()
            }
            else scheduleFoodAlarm()
        }
    }


    fun update(food: FoodData) = scope.launch(Dispatchers.IO) {
        repo.updateFood(food)
        Timber.d("updateFood called")
    }

    fun scheduleFoodAlarm() {
            scope.launch(Dispatchers.IO){
                allFoods = repo.getAllFoods()
                val lastFood = allFoods.last()
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                }
                val delay =calendar.dinerTime()

                val intent = Intent(
                    context,
                    AlarmReceiver::class.java
                )
                intent.action = "ALARM_ACTION"
                intent.putExtra("Food Name", lastFood.foodName)
                intent.putExtra("Shelf Life", lastFood.shelfLife)
                val pIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                val alarm = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                alarm?.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    delay,
                    AlarmManager.INTERVAL_DAY,
                    pIntent
                )
            }
        }

    fun cancelFoodAlarm(){
        val intent = Intent(
            context,
            AlarmReceiver::class.java
        )
        intent.action = "ALARM_ACTION"

        val sender = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.cancel(sender)
    }

    fun setNotificationTime(food: FoodData) {
        val expirationDate = food.datePrepared.timeInMillis + 86400000 * 3
        val currentTime = System.currentTimeMillis()
        if (expirationDate > currentTime) {
            val data = Data.Builder()
                .putInt(FoodAlerts.NOTIFICATION_ID, 0)
                .putString("Food Name", food.foodName)
                .putInt("Food Id", food.foodId)
                .build()
            val delay = expirationDate - currentTime
            scheduleNotification(delay, data)
        }
    }

    private fun scheduleNotification(delay: Long, data: Data) {
        val notificationWork = OneTimeWorkRequest.Builder(FoodAlerts::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()

        val instanceWorkManager =
            WorkManager.getInstance(getApplication<Application>().applicationContext)
        instanceWorkManager.beginUniqueWork(
            FoodAlerts.NOTIFICATION_WORK,
            ExistingWorkPolicy.APPEND,
            notificationWork
        ).enqueue()
    }

    fun restoreFood(food: FoodData) {
        viewModelScope.launch {
            repo.insert(food)
            Timber.d("food inserted!")
        }
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
        Timber.d("onCleared called")
    }


}