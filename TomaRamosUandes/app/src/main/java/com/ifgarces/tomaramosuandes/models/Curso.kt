package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*


/* Representa la sección de un ramo que se puede tomar */
@Entity(tableName=Curso.TABLE_NAME)
data class Curso(
    @PrimaryKey(autoGenerate=false) val NRC :Int, // ID
    val nombre       :String, // nombre/título
    val profesor     :String, // profesor
    val créditos     :Int,    // N° de créditos
    val materia      :String, // N° materia
    val cursoNum     :Int,    // N° de curso ramo
    val secciónNum   :Int,    // N° de sección ramo
    val planEstudios :String, // plan de estudios
    val connectLiga  :String, // conector liga
    val listaCruz    :String,  // lista cruzada

    var clases       :MutableList<CursoEvent> = mutableListOf(),
    var ayudantías   :MutableList<CursoEvent> = mutableListOf(),
    var evaluaciones :MutableList<CursoEvent> = mutableListOf(),
    var laboratorios :MutableList<CursoEvent> = mutableListOf()

) {
    companion object { const val TABLE_NAME :String = "curso" }

    public fun getShortInfo() : String {
        var profe :String = this.profesor
        if (profe == "") { profe = "<No asignado>" }
        return """
            NRC: %d
            Nombre: %s
            Sección: %d
            Profesor: %s
        """.trimIndent().format(this.NRC, this.nombre, this.secciónNum, profe)
    }
}