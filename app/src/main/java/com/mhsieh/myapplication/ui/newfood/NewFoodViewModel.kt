package com.mhsieh.myapplication.ui.newfood

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mhsieh.myapplication.data.FoodData
import com.mhsieh.myapplication.data.FoodDatabase
import com.mhsieh.myapplication.data.FoodRepository
import com.mhsieh.myapplication.util.calcShelfLife
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NewFoodViewModel (val app: Application): AndroidViewModel(app){

    private val scope = viewModelScope
    private val repo:FoodRepository
    private lateinit var newFood:FoodData



    init {
        val foodDao = FoodDatabase.getDatabase(app, scope).foodDao()
        repo = FoodRepository(foodDao)
        println("viewModel init called")
    }

    val _foodName = MutableLiveData<String>()

    var preparedDate:Calendar = Calendar.getInstance()

    fun createFood(){
        newFood = FoodData()
        if (_foodName.value.isNullOrEmpty()){
            Toast.makeText(app, "Enter Text", Toast.LENGTH_LONG).show()
        }else{
            newFood.apply {
                newFood.foodName = _foodName.value!!
                newFood.datePrepared = preparedDate
                newFood.shelfLife = preparedDate.calcShelfLife()
            }
            insert(newFood)
            _foodName.value = ""
        }
    }

    fun insert(food: FoodData) = viewModelScope.launch(Dispatchers.IO) {
        repo.insert(food)
        println("insert called")
    }
}