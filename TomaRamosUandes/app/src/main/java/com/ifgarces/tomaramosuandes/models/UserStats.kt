package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Contains information related to usage of this app by the user.
 * Note: the constructor for this single-instance class is only called when there's no Room local
 * database and therefore the first run of the app. This is suppossed to be a one-row database table.
 * @property ID Primary key.
 * @property firstRunOfApp Ehether the app is currently at its first run or not.
 * @property nightModeOn `true` if the user decided to turn on the night theme in the app settings.
 */
@Entity(tableName = UserStats.TABLE_NAME)
data class UserStats(
    @PrimaryKey(autoGenerate = false) val ID :Int = 0,
    var firstRunOfApp :Boolean = true,
    var nightModeOn :Boolean = false
) {
    companion object {
        const val TABLE_NAME :String = "user_stats"
    }
}
