package com.ifgarces.tomaramosuandes.local_db

import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime


object RoomConverter_DayOfWeek {
    @TypeConverter
    @JvmStatic
    fun stringToDay(dateString :String?) : DayOfWeek? {
        if (dateString == null) { return null }
        return DayOfWeek.valueOf(dateString)
    }

    @TypeConverter
    @JvmStatic
    fun dayToString(date :DayOfWeek?) : String? {
        if (date == null) { return null }
        return date.toString()
    }
}

object RoomConverter_LocalDate {
    @TypeConverter
    @JvmStatic
    fun stringToDate(dateString :String?) : LocalDate? {
        if (dateString == null) { return null }
        return LocalDate.parse(dateString)
    }

    @TypeConverter
    @JvmStatic
    fun dateToString(date : LocalDate?) : String? {
        if (date == null) { return null }
        return date.toString()
    }
}

object RoomConverter_LocalTime {
    @TypeConverter
    @JvmStatic
    fun stringToTime(dateString :String?) : LocalTime? {
        if (dateString == null) { return null }
        return LocalTime.parse(dateString)
    }

    @TypeConverter
    @JvmStatic
    fun timeToString(date :LocalTime?) : String? {
        if (date == null) { return null }
        return date.toString()
    }
}