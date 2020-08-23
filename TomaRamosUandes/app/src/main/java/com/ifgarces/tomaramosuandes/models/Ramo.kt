package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey


/* Representa la sección de un ramo que se puede tomar (cada NRC) */
@Entity(tableName=Ramo.TABLE_NAME)
data class Ramo(
    @PrimaryKey(autoGenerate=false) val NRC :Int, // ID
    val nombre       :String, // nombre/título
    val profesor     :String, // profesor
    val créditos     :Int,    // número de créditos
    val materia      :String, // número materia
    val curso        :Int,    // número de curso
    val sección      :String, // número de sección (en raras ocasiones no es un número, e.g. "A")
    val planEstudios :String, // plan de estudios
    val conectLiga   :String, // conector liga
    val listaCruzada :String, // lista cruzada
    var events       :List<RamoEvent> = listOf() // clases, ayudantías, tutoriales, laboratorios, pruebas y exámenes

) {
    companion object { const val TABLE_NAME :String = "ramo" }

//    public fun getShortInfo() : String {
//        var profe :String = this.profesor
//        if (profe == "") { profe = "<No asignado>" }
//        return """NRC: %d
//Nombre: %s
//Sección: %d
//Profesor: %s""".format(this.NRC, this.nombre, this.sección, profe)
//    }
}