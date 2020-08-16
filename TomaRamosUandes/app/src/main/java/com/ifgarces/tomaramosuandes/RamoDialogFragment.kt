package com.ifgarces.tomaramosuandes

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.ifgarces.tomaramosuandes.adapters.RamoEventsAdapter
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEventType
import com.ifgarces.tomaramosuandes.utils.IntentKeys
import com.ifgarces.tomaramosuandes.utils.toastf
import com.ifgarces.tomaramosuandes.utils.yesNoDialog


class RamoDialogFragment : BottomSheetDialogFragment() {

    companion object {
        /**
         * Shows the dialog fragment.
         * @param manager Needs the caller's `FragmentManager`.
         * @param onDismiss This will be executed when dialog is dismissed by the user.
         */
        lateinit var dismissAction :() -> Unit
        public fun summon(manager :FragmentManager, onDismiss :() -> Unit = {}) {
            this.dismissAction = onDismiss
            this.newInstance().show(manager, this::class.simpleName)
        }
        private fun newInstance() = RamoDialogFragment()
    }

    private object UI {
        lateinit var rootView     :View
        lateinit var nombre       :TextView
        lateinit var NRC          :TextView
        lateinit var profe        :TextView
        lateinit var créditos     :TextView
        lateinit var materia      :TextView
        lateinit var curso        :TextView
        lateinit var sección      :TextView
        lateinit var PE           :TextView
        lateinit var conectLiga   :TextView
        lateinit var listaCruz    :TextView
        lateinit var clases       :RecyclerView
        lateinit var ayuds        :RecyclerView
        lateinit var labs         :RecyclerView
        lateinit var pruebas      :RecyclerView
        lateinit var actionButton :MaterialButton

        fun init(owner :View) {
            this.rootView     = owner
            this.nombre       = owner.findViewById(R.id.ramoDialog_nombre)
            this.NRC          = owner.findViewById(R.id.ramoDialog_NRC)
            this.profe        = owner.findViewById(R.id.ramoDialog_profesor)
            this.créditos     = owner.findViewById(R.id.ramoDialog_creditosNum)
            this.materia      = owner.findViewById(R.id.ramoDialog_materia)
            this.curso        = owner.findViewById(R.id.ramoDialog_curso)
            this.sección      = owner.findViewById(R.id.ramoDialog_seccion)
            this.PE           = owner.findViewById(R.id.ramoDialog_PE)
            this.conectLiga   = owner.findViewById(R.id.ramoDialog_liga)
            this.listaCruz    = owner.findViewById(R.id.ramoDialog_lCruz)
            this.clases       = owner.findViewById(R.id.ramoDialog_clasesRecycler)
            this.ayuds        = owner.findViewById(R.id.ramoDialog_ayudsRecycler)
            this.labs         = owner.findViewById(R.id.ramoDialog_labsRecycler)
            this.pruebas      = owner.findViewById(R.id.ramoDialog_pruebasRecycler)
            this.actionButton = owner.findViewById(R.id.ramoDialog_button)
        }
    }

    override fun onViewCreated(view :View, savedInstanceState :Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        UI.init(owner=view)

        for ( recycler :RecyclerView in listOf(UI.clases, UI.ayuds, UI.labs, UI.pruebas) ) {
            recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        val ramo_isTaken :Boolean = this.activity!!.intent.getBooleanExtra(IntentKeys.RAMO_IS_TAKEN, false)
        val nrc  :Int = this.activity!!.intent.getIntExtra(IntentKeys.RAMO_NRC, -99999)
        val ramo :Ramo = DataMaster.findRamo(nrc)!!

        UI.nombre.text = ramo.nombre
        UI.NRC.text = ramo.NRC.toString()
        UI.profe.text = ramo.profesor
        UI.créditos.text = ramo.créditos.toString()
        UI.materia.text = ramo.materia
        UI.curso.text = ramo.curso.toString()
        UI.sección.text = ramo.sección
        UI.PE.text = ramo.planEstudios
        UI.conectLiga.text = if (ramo.conectLiga == "") "No" else ramo.conectLiga
        UI.listaCruz.text = if (ramo.listaCruzada == "") "No" else ramo.listaCruzada

        UI.clases.adapter = RamoEventsAdapter(
            data = ramo.events.filter{ it.type == RamoEventType.CLAS }
        )
        UI.ayuds.adapter = RamoEventsAdapter(
            data = ramo.events.filter{ it.type == RamoEventType.AYUD }
        )
        UI.labs.adapter = RamoEventsAdapter(
            data = ramo.events.filter{ it.type == RamoEventType.LABT || it.type == RamoEventType.TUTR }
        )
        UI.pruebas.adapter = RamoEventsAdapter(
            data = ramo.events.filter{ it.isEvaluation() }
        )

        if (ramo_isTaken) { // `ramo` already in user list
            UI.actionButton.icon = context!!.getDrawable(R.drawable.trash_icon)
            UI.actionButton.text = "Borrar ramo"
            UI.actionButton.setOnClickListener { this.actionUntake(ramo) }
        }
        else { // `ramo` not yet taken by user
            UI.actionButton.icon = context!!.getDrawable(R.drawable.add_icon)
            UI.actionButton.text = "Tomar ramo"
            UI.actionButton.setOnClickListener { this.actionTake(ramo) }
        }
    }

    override fun onStart() { // references: https://medium.com/@mhrpatel12/making-most-out-of-android-bottom-sheet-352c04551fb4
        super.onStart()

        /* initializing the `BottomSheetDialog` fully expanded */
        dialog?.also {
            val bottomSheet :View = dialog?.findViewById(R.id.design_bottom_sheet)!!
            bottomSheet.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
            val behavior :BottomSheetBehavior<View> = BottomSheetBehavior.from<View>(bottomSheet)
            val layout :LinearLayout = UI.rootView.findViewById(R.id.ramoDialog_linearLayout) as LinearLayout //rootLayout is root of your fragment layout.
            layout.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    try {
                        layout.viewTreeObserver.removeGlobalOnLayoutListener(this)
                        behavior.peekHeight = layout.height
                        view?.requestLayout()
                    } catch (e: Exception) {}
                }
            })
        }
    }

    override fun onCreateView(inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?) : View? {
        return inflater.inflate(R.layout.fragment_ramo_dialog, container, false)
    }

    override fun onDismiss(dialog :DialogInterface) {
        super.onDismiss(dialog)
        Companion.dismissAction.invoke()
    }

    private fun actionTake(ramo :Ramo) {
        DataMaster.addUserRamo(ramo)
        this.context!!.toastf("%s tomado", ramo.nombre)
        HomeActivity.RecyclerSync.requestUpdate()
        this.dismiss()
    }

    private fun actionUntake(ramo :Ramo) {
        this.context!!.yesNoDialog(
            title = "Borrar ramo",
            message = "¿Está seguro que desea eliminar ${ramo.nombre} de su lista de ramos tomados?",
            onYesClicked = {
                DataMaster.deleteUserRamo(ramo.NRC)
                this.context!!.toastf("%s eliminado", ramo.nombre)
                HomeActivity.RecyclerSync.requestUpdate() // <- necessary because this dialog can be called from `HomeActivity`
                this.dismiss()
            }
        )
    }
}