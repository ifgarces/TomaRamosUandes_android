package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName=Curso.TABLE_NAME)
data class Curso (
    @PrimaryKey(autoGenerate=false)
    val NRC :String, // ID

    val nombre       :String, // nombre/título
    val profesor     :String, // profesor
    val créditos     :Int,    // N° de créditos
    val materia      :String, // N° materia
    val cursoNum     :Int,    // N° de curso ramo
    val secciónNum   :Int,    // N° de sección ramo
    val planEstudios :String, // plan de estudios
    val connectLiga  :String, // conector liga
    val listaCruz    :String  // lista cruzada
) {
    companion object {
        public const val TABLE_NAME :String = "Curso"
    }
}