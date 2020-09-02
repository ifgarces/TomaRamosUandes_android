package com.ifgarces.tomaramosuandes.models


// TODO: integrate in Room local database.

data class UserStats(
    var firsTimeOpened :Boolean, // TODO: make this useful.
    var jokeExecuted :Boolean // TODO: set a very low probability to randomly execute joke, maximum once per device.
) {
    companion object {
        const val TABLE_NAME :String = "user_stats"
    }
}