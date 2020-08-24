package com.ifgarces.tomaramosuandes.local_db

import androidx.room.Embedded
import androidx.room.Relation
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent


data class RamoAndEventLinker (
    @Embedded val ramo :Ramo,
    @Relation(
        parentColumn = "NRC",
        entityColumn = "ramoNRC"
    )
    val event :RamoEvent
)