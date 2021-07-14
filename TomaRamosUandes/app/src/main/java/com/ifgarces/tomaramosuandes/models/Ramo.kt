package com.ifgarces.tomaramosuandes.models

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Represents a section or a subject that can be registered by a student (i.e. each NRC).
 * @property NRC Primary key.
 * @property nombre Subject title.
 * @property profesor Subject teacher(s).
 * @property créditos Amount of credits.
 * @property materia Represents the carreer related to the subject (e.g. "ING", "ICC", ...).
 * @property curso The course number.
 * @property sección Section number for the subject.
 * @property planEstudios The curriculum it belongs to (2011 or 2016).
 * @property conectLiga ??
 * @property listaCruzada ??
 */
@Entity(tableName=Ramo.TABLE_NAME)
data class Ramo(
    @PrimaryKey(autoGenerate=false) val NRC :Int, // ~ ID
    val nombre       :String, // nombre/título
    val profesor     :String, // profesor
    val créditos     :Int,    // número de créditos
    val materia      :String, // número materia
    val curso        :Int,    // número de curso
    val sección      :String, // número de sección (en raras ocasiones no es un número, e.g. "A")
    val planEstudios :String, // plan de estudios
    val conectLiga   :String, // conector liga
    val listaCruzada :String, // lista cruzada
) {
    companion object {
        const val TABLE_NAME :String = "ramo"
    }
}
