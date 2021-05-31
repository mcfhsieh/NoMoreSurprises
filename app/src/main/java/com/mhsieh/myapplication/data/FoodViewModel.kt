package com.mhsieh.myapplication.data

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mhsieh.myapplication.notifications.AlarmReceiver
import com.mhsieh.myapplication.notifications.FoodAlerts.Companion.REQUEST_CODE
import com.mhsieh.myapplication.util.calcShelfLife
import com.mhsieh.myapplication.util.dinerTime
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import kotlin.coroutines.CoroutineContext

class FoodViewModel(app: Application) : AndroidViewModel(app) {
    private lateinit var allFoods: List<FoodData>
    private lateinit var lastFood: FoodData
    private var parentJob = Job()
    private var scope = CoroutineScope(coroutineContext)
    private var alarmMgr: AlarmManager? = null
    private var repo: FoodRepository
    val allDishes: LiveData<List<FoodData>>
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val _notificationsOn = MutableLiveData<Boolean?>()
    val notificationsOn: LiveData<Boolean?>
        get() = _notificationsOn
    private val _navigateToEditFood = MutableLiveData<Int>()
    val navigateToEditFood: LiveData<Int>
        get() = _navigateToEditFood
    private val _navigateToNewFood = MutableLiveData<Boolean>()
    val navigateToNewFood: LiveData<Boolean>
        get() = _navigateToNewFood


    init {
        val foodDao = FoodDatabase.getDatabase(app, scope).foodDao()
        repo = FoodRepository(foodDao)
        allDishes = repo.allDishes
        updateShelfLife()
    }

    fun showNewFoodDialog(){
        _navigateToNewFood.value = true
    }

    fun  onNewFoodDialogNavigated(){
        _navigateToNewFood.value = false
    }
    fun onFoodItemClicked(foodId: Int){
        _navigateToEditFood.value = foodId
    }

    fun onEditFoodNavigated(){
        _navigateToEditFood.value = null
    }

    fun turnOnNotifications() {
        _notificationsOn.value = true
    }

    fun turnOffNotifications() {
        _notificationsOn.value = false
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
        }
    }

    fun update(food: FoodData) = viewModelScope.launch(Dispatchers.IO) {
        repo.updateFood(food)
        Timber.d("updateFood called")
    }

    fun restoreFood(food: FoodData) {
        viewModelScope.launch {
            repo.insert(food)
            Timber.d("food inserted!")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("onCleared called")
    }

    fun scheduleFoodAlarm(context: Context) {
        viewModelScope.launch {
            repo.getAllFoods()
            lastFood = allFoods.last()
            val calendar = Calendar.getInstance()
            val delay = calendar.dinerTime()
            val alarmIntent = makeAlarmIntent(context)
            alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmMgr?.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + delay,
                AlarmManager.INTERVAL_DAY,
                alarmIntent
            )
        }
    }

    private fun makeAlarmIntent(context: Context): PendingIntent {
        val intent = Intent(
            context,
            AlarmReceiver::class.java
        )
        intent.action = "ALARM_ACTION"
        intent.putExtra("Food Name", lastFood.foodName)
        intent.putExtra("Food Id", lastFood.foodId)
        return PendingIntent.getBroadcast(
            context,
            lastFood.foodId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    fun cancelFoodAlarm(context: Context) {
        val intent = Intent(
            context,
            AlarmReceiver::class.java
        )
        intent.action = "ALARM_ACTION"
        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val sender = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmMgr?.cancel(sender)
    }
}