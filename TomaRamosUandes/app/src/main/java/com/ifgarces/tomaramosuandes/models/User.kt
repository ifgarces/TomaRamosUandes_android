package com.ifgarces.tomaramosuandes.models


data class User (
    var data :MutableList<Curso>
) {
    companion object {
        const val TABLE_NAME :String = "user"
    }
}