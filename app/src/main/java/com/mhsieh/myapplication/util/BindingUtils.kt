package com.mhsieh.myapplication.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.mhsieh.myapplication.R
import com.mhsieh.myapplication.data.FoodData


@BindingAdapter("foodStatusText")
fun TextView.setFoodStatusText(food: FoodData?) {
    food?.let {
        var shelfLife = food.shelfLife
        text = if (food.spoiled) resources.getString(R.string.food_spoiled, shelfLife)
        else {
            when (shelfLife) {
                0 -> "Fresh"
                in 1..4 -> "$shelfLife days old"
                else -> "Food may be spoiled!"
            }

        }
    }
}
@BindingAdapter("foodName")
fun TextView.setFoodName(food: FoodData?){
    food?.let {
        text = food.foodName
    }
}
