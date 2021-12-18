package com.ifgarces.tomaramosuandes.local_db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ifgarces.tomaramosuandes.models.RamoEvent


@Dao
interface RamoEventDAO {
    @Query(value="DELETE FROM ${RamoEvent.TABLE_NAME}")
    fun clear()

    @Insert(onConflict=OnConflictStrategy.REPLACE)
    fun insert(ramo :RamoEvent)

    @Update(onConflict=OnConflictStrategy.REPLACE)
    fun update(ramo :RamoEvent)

    @Query(value="SELECT * FROM ${RamoEvent.TABLE_NAME}")
    fun getAllEvents() : List<RamoEvent>

    @Query(value="SELECT * FROM ${RamoEvent.TABLE_NAME} WHERE ID=:id")
    fun getRamoEvent(id :Int) : RamoEvent

    @Query(value="DELETE FROM ${RamoEvent.TABLE_NAME} WHERE ID=:id")
    fun deleteRamoEvent(id :Int)

//    @Query(value="SELECT * FROM ${RamoEvent.TABLE_NAME} WHERE ramoNRC=:nrc")
//    fun getEventsOfRamo(nrc :Int) : List<RamoEvent>
}