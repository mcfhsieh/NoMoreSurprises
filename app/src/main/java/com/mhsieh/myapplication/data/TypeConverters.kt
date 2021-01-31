package com.mhsieh.myapplication.data

import androidx.room.TypeConverter
import java.util.*

class Converters {

    @TypeConverter
    fun toCalendar(long: Long): Calendar?{
        var calendar = Calendar.getInstance()
        calendar.timeInMillis = long
        long.let { return calendar }
    }

    @TypeConverter
    fun longFromCalendar(cal:Calendar):Long?{
        cal.let { return cal.timeInMillis }
    }
}
