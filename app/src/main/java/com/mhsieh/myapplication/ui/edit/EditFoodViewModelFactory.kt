package com.mhsieh.myapplication.ui.edit

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EditFoodViewModelFactory (val application: Application, val foodId:Int):
    ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        println("viewnmodelfactory used")
        if(modelClass.isAssignableFrom(EditFoodViewModel::class.java)){
            return EditFoodViewModel(application, foodId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class!")
    }
}