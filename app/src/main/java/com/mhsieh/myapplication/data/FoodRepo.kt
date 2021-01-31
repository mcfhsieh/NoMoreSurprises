package com.mhsieh.myapplication.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FoodRepository(private val dao: FoodDao){

    val allDishes: LiveData<List<FoodData>> = dao.getAllFood()

    suspend fun insert(food:FoodData){
        dao.insert(food)
    }

    suspend fun deleteAll(){
        dao.deleteAll()
    }

    suspend fun getFood(id: Int): FoodData? {
        return withContext(Dispatchers.IO){
            dao.getFood(id)
        }
    }

    suspend fun delete(id:Int){
        dao.delete(id)
    }
    suspend fun updateFood(food: FoodData){
        dao.updateFood(food)
    }

    suspend fun getAllFoods(): List<FoodData> {
        return dao.getAllFoods()
    }



}
