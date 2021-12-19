package com.ifgarces.tomaramosuandes.networking

import android.app.Activity
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.AppMetadata
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import com.ifgarces.tomaramosuandes.utils.toAppMetadata
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
     * Not sure this is the best place for this, but this method may be called on `MainActivity` and
     * `HomeActivity`. Displays a dialog noticing an internet connection failure. Intended for
     * easily calling for the case in which connection with Firebase failed.
     */
    public fun showInternetConnectionErrorDialog(targetActivity :Activity) {
        targetActivity.infoDialog(
            title = "Error de conexión",
            message = """\
No se pudo obtener los ramos más actuales, verifique su conexión a internet. La app cuenta con un \
catálogo offline, por lo que puede continuar, pero se recomeienda tener conexión para obtener \
siempre el catálogo más reciente.""".multilineTrim(),
            onDismiss = {},
            icon = R.drawable.no_internet_icon
        )
    }

    /**
     * Namespace-like object for encapsulating methods intended for the developer to update the
     * catalog to a new period, update application metadata values in Firebase, etc. Don't know if
     * this is the best place for this. All the write operations are here.
     */
    object Developer {
        /**
         * Inserting/updating a `Ramo` into Firebase DB.
         */
        public fun createRamo(
            ramo :Ramo,
            onSuccess :() -> Unit,
            onFailure :(exception :Exception) -> Unit
        ) {
            val methodName :String = this::createRamo.name
            FirebaseMaster.db.collection(Ramo.TABLE_NAME)
                .document(ramo.NRC.toString()) // we give a fixed ID: the Ramo's NRC
                .set(ramo)
                .addOnSuccessListener {
                    FirebaseMaster.logCallOk(methodName)
                    onSuccess.invoke()
                }
                .addOnFailureListener { e :Exception ->
                    FirebaseMaster.logCallFail(methodName, e)
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
            FirebaseMaster.db.collection(RamoEvent.TABLE_NAME)
                .document(ramoEvent.ID.toString())
                .set( // cannot add directly as there's trouble serializing some attributes
                    RamoEvent.toRawMap(ramoEvent)
                )
                .addOnSuccessListener {
                    FirebaseMaster.logCallOk(methodName)
                    onSuccess.invoke()
                }
                .addOnFailureListener { e :Exception ->
                    FirebaseMaster.logCallFail(methodName, e)
                    onFailure.invoke(e)
                }
        }

        /**
         * For easily creating/updating a list of `Ramo`s.
         */
        public fun uploadRamoCollection(
            ramos :List<Ramo>,
            onFirstFailureCallback :(e :Exception) -> Unit
        ) {
            ramos.forEach { ramo :Ramo ->
                this.createRamo(
                    ramo = ramo,
                    onSuccess = {},
                    onFailure = {
                        onFirstFailureCallback.invoke(it)
                    }
                )
            }
        }

        /**
         * For easily creating/updating a list of `RamoEvent`s.
         */
        public fun uploadEventCollection(
            events :List<RamoEvent>,
            onFirstFailureCallback :(e :Exception) -> Unit
        ) {
            events.forEach { ramoEvent :RamoEvent ->
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
         * Inserting/updating the [single] `AppMetadata` object into Firebase DB.
         */
        public fun updateAppMetadata(
            metadata :AppMetadata,
            onSuccess :() -> Unit,
            onFailure :(exception :Exception) -> Unit
        ) {
            val methodName :String = this::updateAppMetadata.name
            FirebaseMaster.db.collection(AppMetadata.TABLE_NAME)
                .document("0") // this will be a one-row table
                .set(metadata)
                .addOnSuccessListener {
                    FirebaseMaster.logCallOk(methodName)
                    onSuccess.invoke()
                }
                .addOnFailureListener { e :Exception ->
                    FirebaseMaster.logCallFail(methodName, e)
                    onFailure.invoke(e)
                }
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
                val gotEventsRawMaps :List<Map<String, Any?>> = docs.documents.map { it.data!! }
                val gotEvents :List<RamoEvent> = gotEventsRawMaps.map { RamoEvent.fromRawMap(it) }
                this.logCallOk(methodName, "Got %d events".format(gotEvents.count()))
                onSuccess.invoke(gotEvents)
            }
            .addOnFailureListener { e :Exception ->
                this.logCallFail(methodName, e)
                onFailure.invoke(e)
            }
    }

    public fun getAppMetadata(
        onSuccess :(gotMetadata :AppMetadata) -> Unit,
        onFailure :(exception :Exception) -> Unit
    ) {
        val methodName :String = this::getAppMetadata.name
        this.db.collection(AppMetadata.TABLE_NAME)
            .get()
            .addOnSuccessListener { docs :QuerySnapshot ->
                if (docs.count() == 0) { // this should be handled on the OnFailure callback, but for some reason this check is needed
                    val e = Exception("Internet connection error")
                    this.logCallFail(methodName, e, "Couldn't get first document of QuerySnapshot")
                    onFailure(e)
                    return@addOnSuccessListener
                }
                val meta :AppMetadata = docs.first().toAppMetadata()
                this.logCallOk(methodName, "Got: %s".format(meta))
                onSuccess.invoke(meta)
            }
            .addOnFailureListener { e :Exception ->
                this.logCallFail(methodName, e)
                onFailure.invoke(e)
            }
    }
}
