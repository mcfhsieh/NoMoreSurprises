package com.mhsieh.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mhsieh.myapplication.util.calcShelfLife
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [FoodData::class], version = 4 )
@TypeConverters(Converters::class)
abstract class FoodDatabase: RoomDatabase(){

    abstract fun foodDao(): FoodDao

    companion object{
        @Volatile
        private var INSTANCE: FoodDatabase? = null

        fun getDatabase(context: Context,
                        scope: CoroutineScope
        )
                :FoodDatabase{
            return INSTANCE?: synchronized(this){
                val instance: FoodDatabase = Room.databaseBuilder(
                    context.applicationContext,
                    FoodDatabase::class.java,
                    "Meal_Database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(MealDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class MealDatabaseCallback(
            private val scope: CoroutineScope
        ) :
            RoomDatabase.Callback() {

            override fun onOpen(db: SupportSQLiteDatabase){
                super.onOpen(db)
                INSTANCE?.let{database ->
                    scope.launch(Dispatchers.IO){
                        //populate database
                        populateDatabase(database.foodDao())
                    }
                }

            }

            suspend fun populateDatabase(foodDao: FoodDao) {
                if (foodDao.getAllFoods().isEmpty()){
                    println("Database is empty")
                }else {
                    val foods = foodDao.getAllFoods()
//                    for (food in foods){
//                        food.shelfLife = food.datePrepared.calcShelfLife()
////                        foodDao.updateTask(food)
//                        println("$food.id  updated")
//                    }
                }

            }
        }

    }


}