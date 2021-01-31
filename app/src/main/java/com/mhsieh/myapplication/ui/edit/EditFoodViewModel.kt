package com.mhsieh.myapplication.ui.edit

import android.app.Application
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

    val _editFoodNameText = MutableLiveData<String>()
    val _editFoodShelfLife = MutableLiveData<String>()

    init {
        newCalendarDate = Calendar.getInstance()
        val foodDao = FoodDatabase.getDatabase(app, viewModelScope).foodDao()
        repo = FoodRepository(foodDao)
        initializeNote()
    }

    fun initializeNote() {
        viewModelScope.launch {
            if (foodId != null) {
                editableFood = repo.getFood(foodId)?: FoodData()

            } else {
                editableFood = FoodData()
            }
            newCalendarDate = editableFood.datePrepared
            updateUI()
        }
    }

    fun updateUI(){
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

    fun update(food: FoodData) = viewModelScope.launch(Dispatchers.IO) {
        repo.updateFood(food)
        Timber.d("update called")
    }

    fun delete(id: Int) = viewModelScope.launch(Dispatchers.IO) {
        repo.delete(id)
        Timber.d("delete called")
    }


}