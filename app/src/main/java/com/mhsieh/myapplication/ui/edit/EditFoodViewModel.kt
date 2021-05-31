package com.mhsieh.myapplication.ui.edit

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.DialogFragmentNavigatorDestinationBuilder
import com.mhsieh.myapplication.data.FoodData
import com.mhsieh.myapplication.data.FoodDatabase
import com.mhsieh.myapplication.data.FoodRepository
import com.mhsieh.myapplication.notifications.AlarmReceiver
import com.mhsieh.myapplication.notifications.FoodAlerts
import com.mhsieh.myapplication.util.calcShelfLife
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

class EditFoodViewModel(
    private val app: Application,
    private val foodId: Int
) : AndroidViewModel(app) {

    val TAG = "EditFoodViewModel"
    private val repo: FoodRepository
    lateinit var editableFood: FoodData
    var newCalendarDate: Calendar
    private var alarmMgr: AlarmManager? = null

    val _editFoodNameText = MutableLiveData<String>()
    val _editFoodShelfLife = MutableLiveData<String>()

    init {
        newCalendarDate = Calendar.getInstance()
        val foodDao = FoodDatabase.getDatabase(app, viewModelScope).foodDao()
        repo = FoodRepository(foodDao)
        initializeNote()
    }

    private fun initializeNote() {
        viewModelScope.launch {
            editableFood = if (foodId != null) {
                repo.getFood(foodId) ?: FoodData()
            } else {
                FoodData()
            }
            newCalendarDate = editableFood.datePrepared
            updateUI()
        }
    }

    private fun updateUI(){
        _editFoodNameText.value = editableFood.foodName
        _editFoodShelfLife.value = editableFood.datePrepared.calcShelfLife().toString() + " days old"
        Timber.d(_editFoodNameText.value)
    }

    fun updateNote() {
        editableFood.apply {
            foodName = _editFoodNameText.value!!
            datePrepared = newCalendarDate
            shelfLife = editableFood.datePrepared.calcShelfLife()
        }
        update(editableFood)
    }

    fun deleteFood(){
        viewModelScope.launch {
            delete(editableFood.foodId)
        }
    }

    fun insert(food: FoodData) = viewModelScope.launch(Dispatchers.IO) {
        repo.insert(food)

    }

    private fun update(food: FoodData) = viewModelScope.launch(Dispatchers.IO) {
        repo.updateFood(food)
        Timber.d("update called")
    }

    private fun delete(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repo.delete(id)
        Timber.d("delete called")
    }

    fun cancelFoodAlarm(context: Context,id: Int) {
        val intent = Intent(
            context,
            AlarmReceiver::class.java
        )
        intent.action = "ALARM_ACTION"
        alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val sender = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmMgr?.cancel(sender)
    }


}