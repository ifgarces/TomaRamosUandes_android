package com.ifgarces.tomaramosuandes.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.adapters.RamoEventsAdapter
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.models.RamoEventType
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.IntentKeys
import com.ifgarces.tomaramosuandes.utils.yesNoDialog
import java.util.concurrent.Executors


/**
 * Dialog prompted when clicking a `Ramo`, showing all of its data.
 * @property onDismiss Callback executed when the dialog is dismissed by the user.
 */
class RamoDialogFragment(private val onDismissAction :() -> Unit) : BottomSheetDialogFragment() {

    private class FragmentUI(owner :View) {
        val rootView     :View = owner
        val nombre       :TextView = owner.findViewById(R.id.ramoDialog_nombre)
        val NRC          :TextView = owner.findViewById(R.id.ramoDialog_NRC)
        val profe        :TextView = owner.findViewById(R.id.ramoDialog_profesor)
        val créditos     :TextView = owner.findViewById(R.id.ramoDialog_creditosNum)
        val materia      :TextView = owner.findViewById(R.id.ramoDialog_materia)
        val curso        :TextView = owner.findViewById(R.id.ramoDialog_curso)
        val sección      :TextView = owner.findViewById(R.id.ramoDialog_seccion)
        val PE           :TextView = owner.findViewById(R.id.ramoDialog_PE)
        val conectLiga   :TextView = owner.findViewById(R.id.ramoDialog_liga)
        val listaCruz    :TextView = owner.findViewById(R.id.ramoDialog_lCruz)
        val clases       :RecyclerView = owner.findViewById(R.id.ramoDialog_clasesRecycler)
        val ayuds        :RecyclerView = owner.findViewById(R.id.ramoDialog_ayudsRecycler)
        val labs         :RecyclerView = owner.findViewById(R.id.ramoDialog_labsRecycler)
        val evals        :RecyclerView = owner.findViewById(R.id.ramoDialog_pruebasRecycler)
        val actionButton :MaterialButton = owner.findViewById(R.id.ramoDialog_button)
    }

    private lateinit var UI :FragmentUI

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View? {
        return inflater.inflate(
            if (DataMaster.userStats.nightModeOn) R.layout.night_fragment_ramo_dialog
            else R.layout.fragment_ramo_dialog,
            container, false
        )
    }

    override fun onViewCreated(view :View, savedInstanceState :Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.UI = FragmentUI(owner = view)

        listOf(UI.clases, UI.ayuds, UI.labs, UI.evals).forEach { recycler :RecyclerView ->
            recycler.layoutManager = LinearLayoutManager(
                context, LinearLayoutManager.HORIZONTAL, false
            )
        }

        val ramo_isInscribed :Boolean = this.requireActivity().intent.getBooleanExtra(
            IntentKeys.RAMO_IS_INSCRIBED, false
        )
        val nrc :Int = this.requireActivity().intent.getIntExtra(IntentKeys.RAMO_NRC, -1)
        assert(nrc != -1)
        val ramo :Ramo = DataMaster.findRamo(nrc, searchInUserList = false)!!

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

        Executors.newSingleThreadExecutor().execute {
            val events :List<RamoEvent> = DataMaster.catalogEvents
                .filter { it.ramoNRC == ramo.NRC }

            UI.clases.adapter = RamoEventsAdapter(
                data = events.filter { it.type == RamoEventType.CLAS },
                showEventType = false
            )
            UI.ayuds.adapter = RamoEventsAdapter(
                data = events.filter { it.type == RamoEventType.AYUD },
                showEventType = false
            )
            UI.labs.adapter = RamoEventsAdapter(
                data = events.filter { it.type == RamoEventType.LABT || it.type == RamoEventType.TUTR },
                showEventType = false
            )
            UI.evals.adapter = RamoEventsAdapter(
                data = events.filter { it.isEvaluation() },
                showEventType = true
            )
        }

        if (ramo_isInscribed) { // `ramo` already in user list
            UI.actionButton.icon = ContextCompat.getDrawable(
                this.requireContext(),
                R.drawable.trash_icon
            ) // <==> this.context!!.getDrawable(R.drawable.trash_icon)
            UI.actionButton.text = "Borrar ramo"
            UI.actionButton.setOnClickListener { this.actionUnInscribe(ramo) }
        } else { // `ramo` not yet inscribed by user
            UI.actionButton.icon = ContextCompat.getDrawable(
                this.requireContext(),
                R.drawable.add_icon
            )
            UI.actionButton.text = "Tomar ramo"
            UI.actionButton.setOnClickListener { this.actionInscribe(ramo) }
        }
    }

    override fun onStart() {
        super.onStart()

        // Initializing the `BottomSheetDialog` fully expanded.
        // References: https://medium.com/@mhrpatel12/making-most-out-of-android-bottom-sheet-352c04551fb4
        this.dialog!!.also {
            val bottomSheet :View = dialog!!.findViewById(R.id.design_bottom_sheet)!!
            bottomSheet.layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
            val behavior :BottomSheetBehavior<View> = BottomSheetBehavior.from(bottomSheet)
            val layout :LinearLayout = UI.rootView.findViewById(
                R.id.ramoDialog_linearLayout
            ) as LinearLayout
            layout.viewTreeObserver?.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        behavior.peekHeight = layout.height
                        view?.requestLayout()
                    }
                }
            )
        }
    }

    override fun onDismiss(dialog :DialogInterface) {
        super.onDismiss(dialog)
        this.onDismissAction.invoke()
    }

    /**
     * Attemps to inscribe `ramo`. If conflict, prompts confirmation dialog.
     */
    private fun actionInscribe(ramo :Ramo) {
        DataMaster.inscribeRamo(
            ramo = ramo,
            activity = this.requireActivity(),
            onFinish = {
                this.dismiss()
            }
        )
    }

    /**
     * Removes `ramo` from user's inscribed list.
     */
    private fun actionUnInscribe(ramo :Ramo) {
        this.requireContext().yesNoDialog(
            title = "Borrar ramo",
            message = "¿Está seguro que desea eliminar ${ramo.nombre} de su lista de ramos tomados?",
            onYesClicked = {
                DataMaster.unInscribeRamo(ramo.NRC)
                this.dismiss()
            }
        )
    }
}