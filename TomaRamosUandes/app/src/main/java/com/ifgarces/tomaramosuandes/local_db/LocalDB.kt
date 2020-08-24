package com.ifgarces.tomaramosuandes.local_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent


@Database(entities=[Ramo::class, RamoEvent::class], version=1, exportSchema=false)
@TypeConverters(RoomConverter_DayOfWeek::class, RoomConverter_LocalDate::class, RoomConverter_LocalTime::class)
abstract class LocalDB : RoomDatabase() {
    abstract fun ramoDAO() : RamoDAO
    abstract fun ramoEventDAO() : RamoEventDAO
}