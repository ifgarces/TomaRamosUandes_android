package com.ifgarces.tomaramosuandes.local_db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.UserStats


@Database(
    entities = [Ramo::class, RamoEvent::class, UserStats::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    RoomConverter_DayOfWeek::class,
    RoomConverter_LocalDate::class,
    RoomConverter_LocalTime::class
)
abstract class LocalRoomDB : RoomDatabase() {
    abstract fun ramosDAO() :RamoDAO
    abstract fun eventsDAO() :RamoEventDAO
    abstract fun userStatsDAO() :UserStatsDAO
}