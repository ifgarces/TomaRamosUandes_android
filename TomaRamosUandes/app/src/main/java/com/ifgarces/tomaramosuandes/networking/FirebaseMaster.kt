package com.ifgarces.tomaramosuandes.networking

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.toRamo


/**
 * Public object that encapsulates Firebase remote database calls, becoming an interface between
 * frontent and backend in this matter. This allows RecyclerView adapters and Fragments to easily
 * consume Firebase calls. For all call methods, there are two parameters `onSuccess` and
 * `onFailure`, which are callbacks for the success and failure scenarios for the call, respectively.
 * @author Ignacio F. Garcés.
 */
object FirebaseMaster {
    private val db :FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * TEST
     */
    public fun uploadRamos(
        onFirstFailureCallback :(e :Exception) -> Unit
    ) {
        DataMaster.getCatalogRamos().forEach { ramo :Ramo ->
            this.createRamo(
                ramo = ramo,
                onSuccess = {},
                onFailure = {
                    onFirstFailureCallback.invoke(it)
                }
            )
        }
    }
    public fun uploadEvents(
        onFirstFailureCallback :(e :Exception) -> Unit
    ) {
        DataMaster.getCatalogEvents().forEach { ramoEvent :RamoEvent ->
            this.createRamoEvent(
                ramoEvent = ramoEvent,
                onSuccess = {},
                onFailure = {
                    onFirstFailureCallback.invoke(it)
                }
            )
        }
    }

    /**
     * Utility method for easily writing to log in a standard format on a successful call. We won't
     * be using `Logf` here, to distinguish logging from firebase, as it will get heavy.
     * @author Ignacio F. Garcés.
     * @param methodName The name of the call method of this object, for tracing.
     * @param aditionalMessage Optional, custom message appended.
     */
    private fun logCallOk(methodName :String, aditionalMessage :String? = null) {
        if (aditionalMessage == null)
            Log.d(this::class.simpleName, "%s - success".format(methodName))
        else
            Log.d(this::class.simpleName, "%s - success: %s".format(methodName, aditionalMessage))
    }

    /**
     * Utility method, similar to `logCallOk`, but for the case in which the call did not succeed.
     * @author Ignacio F. Garcés.
     * @param methodName The name of the call method of this object, for tracing.
     * @param e Exception that caused the call to fail.
     * @param aditionalMessage Optional, custom message appended.
     */
    private fun logCallFail(methodName :String, e :Exception, aditionalMessage :String? = null) {
        if (aditionalMessage == null)
            Log.d(this::class.simpleName, "%s - failure [%s]".format(methodName, e))
        else
            Log.d(this::class.simpleName, "%s - failure [%s]: %s".format(
                methodName, e, aditionalMessage
            ))
    }

    /**
     * Inserting/updating a `Ramo` into Firebase DB.
     */
    public fun createRamo(
        ramo :Ramo,
        onSuccess :() -> Unit,
        onFailure :(exception :Exception) -> Unit
    ) {
        val methodName :String = this::createRamo.name
        this.db.collection(Ramo.TABLE_NAME)
            .document(ramo.NRC.toString()) // we give a fixed ID: the Ramo's NRC
            .set(ramo)
            .addOnSuccessListener {
                this.logCallOk(methodName)
                onSuccess.invoke()
            }
            .addOnFailureListener { e :Exception ->
                this.logCallFail(methodName, e)
                onFailure.invoke(e)
            }
    }

    /**
     * Inserting/updating a `RamoEvent` into Firebase DB.
     */
    public fun createRamoEvent(
        ramoEvent :RamoEvent,
        onSuccess :() -> Unit,
        onFailure :(exception :Exception) -> Unit
    ) {
        val methodName :String = this::createRamoEvent.name
        this.db.collection(RamoEvent.TABLE_NAME)
            .add(ramoEvent)
            .addOnSuccessListener {
                this.logCallOk(methodName)
                onSuccess.invoke()
            }
            .addOnFailureListener { e :Exception ->
                this.logCallFail(methodName, e)
                onFailure.invoke(e)
            }
    }

    /**
     * Getting a `Ramo` by ID from Firebase DB.
     */
    public fun getRamo(
        ramoId :Int,
        onSuccess :(gotRamo :Ramo) -> Unit,
        onFailure :(exception :Exception) -> Unit
    ) {
        val methodName :String = this::getRamo.name
        this.db.collection(Ramo.TABLE_NAME)
            .document(ramoId.toString())
            .get()
            .addOnSuccessListener { doc :DocumentSnapshot ->
                val gotRamo :Ramo = doc.toObject(Ramo::class.java)!!
                this.logCallOk(methodName)
                onSuccess.invoke(gotRamo)
            }
            .addOnFailureListener { e :Exception ->
                this.logCallFail(methodName, e)
                onFailure.invoke(e)
            }
    }

    /**
     * Get all the `Ramo`s stored in Firebase DB.
     */
    public fun getAllRamos(
        onSuccess :(gotRamos :List<Ramo>) -> Unit,
        onFailure :(exception :Exception) -> Unit
    ) {
        val methodName :String = this::getAllRamos.name
        this.db.collection(Ramo.TABLE_NAME)
            .get()
            .addOnSuccessListener { docs :QuerySnapshot ->
                val gotRamos :List<Ramo> = docs.map { it.toRamo() }
                this.logCallOk(methodName, "Got %d ramos".format(gotRamos.count()))
                onSuccess.invoke(gotRamos)
            }
            .addOnFailureListener { e :Exception ->
                this.logCallFail(methodName, e)
                onFailure.invoke(e)
            }
    }

    /**
     * Get all the `RamoEvent`s stored in Firebase DB.
     */
    public fun getAllRamoEvents(
        onSuccess :(gotRamoEvents :List<RamoEvent>) -> Unit,
        onFailure :(exception :Exception) -> Unit
    ) {
        val methodName :String = this::getAllRamoEvents.name
        this.db.collection(RamoEvent.TABLE_NAME)
            .get()
            .addOnSuccessListener { docs :QuerySnapshot ->
                val gotEvents :List<RamoEvent> = docs.map { it.toObject(RamoEvent::class.java) }
                this.logCallOk(methodName, "Got %d events".format(gotEvents.count()))
                onSuccess.invoke(gotEvents)
            }
            .addOnFailureListener { e :Exception ->
                this.logCallFail(methodName, e)
                onFailure.invoke(e)
            }
    }
}
