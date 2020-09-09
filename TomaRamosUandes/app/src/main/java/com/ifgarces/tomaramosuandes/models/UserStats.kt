package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey


// Note: this class constructor is only called when there's no Room local database and therefore the first run of the app.
@Entity(tableName=UserStats.TABLE_NAME)
data class UserStats(
    @PrimaryKey(autoGenerate=false) val ID :Int = 0,
    var firstRunOfApp :Boolean = true, // indicates if the app is at its first run. TODO: make this useful, for example, in 'Do not show again' options for dialogs.
    var jokeExecuted  :Boolean = false // will turn true when the joke dialog is shown. Will prevent it from being shown ever again.
) {
    companion object {
        const val TABLE_NAME :String = "user_stats"
    }
}