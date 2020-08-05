package com.ifgarces.tomaramosuandes.models


data class User (
    var data :MutableList<Curso>,
    var firstTime :Boolean // TODO: aplicar algo así para dar un mensaje de bienvenida sólo a primerizos o al que quiera (opción de no mostrar de nuevo)
) {
    companion object {
        const val TABLE_NAME :String = "user"
    }
}