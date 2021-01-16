package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Contains information related to usage of this app by the user.
 * Note: the constructor for this single-instance class is only called when there's no Room local
 * database and therefore the first run of the app.
 * @property ID Primary key.
 * @property firstRunOfApp Indicates whether the app is currently at its first run or not.
 * @property jokeExecuted Will turn true when the joke dialog of `EasterEggs` is shown. That will
 * prevent it from being shown ever again.
 */
@Entity(tableName=UserStats.TABLE_NAME)
data class UserStats(
    @PrimaryKey(autoGenerate=false) val ID :Int = 0,
    var firstRunOfApp :Boolean = true,
    var jokeExecuted  :Boolean = false
) {
    companion object {
        const val TABLE_NAME :String = "user_stats"
    }
}