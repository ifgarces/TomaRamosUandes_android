package com.ifgarces.tomaramosuandes.local_db

import androidx.room.*
import com.ifgarces.tomaramosuandes.models.UserStats


// Note: it is supossed to be just one instance of the `UserStats` class in the Room local database, i.e. its table should contain one row.
@Dao
interface UserStatsDAO {
    @Query(value="DELETE FROM ${UserStats.TABLE_NAME}")
    fun clear()

    @Insert(onConflict=OnConflictStrategy.REPLACE)
    fun insert(value :UserStats)

    @Update(onConflict=OnConflictStrategy.REPLACE)
    fun update(value :UserStats)

    @Query(value="SELECT * FROM ${UserStats.TABLE_NAME}")
    fun getStats() : List<UserStats> // this list should always contain one element, if not, throw error or somethin'
}