package com.mhsieh.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FoodDao {

    @Query("SELECT * from food_table ORDER BY `shelf life`")
    fun getAllFood(): LiveData<List<FoodData>>

    @Query("SELECT * from food_table ORDER BY `shelf life`")
    suspend fun getAllFoods(): List<FoodData>

    @Query("SELECT * from food_table WHERE id = :key")
    suspend fun getFood(key: Int): FoodData?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: FoodData)

    @Query("DELETE FROM food_table")
    suspend fun deleteAll()

    @Query("DELETE FROM food_table WHERE id = :key")
    suspend fun delete(key: Int)

    @Update
    suspend fun updateFood(food: FoodData)
}
