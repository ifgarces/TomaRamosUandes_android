package com.ifgarces.tomaramosuandes.networking

import android.content.Context
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.toastf


/**
 * Interface for external database calls (Firebase Firestore DB).
 */
object FirebaseMaster {
    private val db :FirebaseFirestore = FirebaseFirestore.getInstance()

    public fun testCall(context :Context) {
        val testRamo :Ramo = DataMaster.getCatalogRamos().first()
        this.createRamo(
            ramo = testRamo,
            onSuccess = {
                context.toastf("Bad news: call failed")
            },
            onFailure = {
                context.toastf("Call succeded!")
            }
        )
    }

    public fun createRamo(
        ramo :Ramo,
        onSuccess :() -> Unit,
        onFailure :(exception :Exception) -> Unit
    ) {
        val funcName :String = this::createRamo.name
        this.db.collection(Ramo.TABLE_NAME)
            .add(ramo)
            .addOnSuccessListener { doc :DocumentReference ->
                Logf(this::class, "%s - success", funcName)
            }
            .addOnFailureListener { e :Exception ->
                Logf(this::class, "%s - failure: %s", funcName, e)
            }
    }

    public fun createRamoEvents(
        ramoEvent :RamoEvent,
        onSuccess :() -> Unit,
        onFailure :(exception :Exception) -> Unit
    ) {
        throw NotImplementedError()
    }

    public fun getRamo(
        ramoId :Int,
        onSuccess :() -> Unit,
        onFailure :(exception :Exception) -> Unit
    ) {
        throw NotImplementedError()
    }

    public fun getEventsOfRamo(
        ramoId :Int,
        onSuccess :() -> Unit,
        onFailure :(exception :Exception) -> Unit
    ) {
        throw NotImplementedError()
    }
}
