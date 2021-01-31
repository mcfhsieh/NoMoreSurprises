package com.mhsieh.myapplication.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "food_table")
data class FoodData(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val foodId: Int = 0,
    @ColumnInfo(name = "food name") var foodName:String = "",
    @ColumnInfo(name = "ingredients") val ingredients: String = "",
    @ColumnInfo(name = "date prepared") var datePrepared: Calendar = Calendar.getInstance(),
    @ColumnInfo(name = "shelf life")var shelfLife: Int = 0,
    @ColumnInfo(name = "was wasted")var spoiled: Boolean = false
)