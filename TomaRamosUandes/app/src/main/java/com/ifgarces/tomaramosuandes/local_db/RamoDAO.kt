package com.ifgarces.tomaramosuandes.local_db

import androidx.room.*
import com.ifgarces.tomaramosuandes.models.Ramo


@Dao
interface RamoDAO {
    @Query(value="DELETE FROM ${Ramo.TABLE_NAME}")
    fun clear()

    @Insert(onConflict=OnConflictStrategy.REPLACE)
    fun insert(ramo :Ramo)

    @Update(onConflict=OnConflictStrategy.REPLACE)
    fun update(ramo :Ramo)

    @Query(value="SELECT * FROM ${Ramo.TABLE_NAME}")
    fun getAllRamos() :List<Ramo>

    @Query(value="SELECT * FROM ${Ramo.TABLE_NAME} WHERE NRC=:nrc")
    fun getRamo(nrc :Int) :Ramo

    @Query(value="DELETE FROM ${Ramo.TABLE_NAME} WHERE NRC=:nrc")
    fun deleteRamo(nrc :Int)
}