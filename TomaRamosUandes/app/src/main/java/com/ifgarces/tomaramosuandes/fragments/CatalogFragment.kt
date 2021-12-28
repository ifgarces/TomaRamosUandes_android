package com.ifgarces.tomaramosuandes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.activities.HomeActivity
import com.ifgarces.tomaramosuandes.adapters.CatalogRamosAdapter
import com.ifgarces.tomaramosuandes.models.AppMetadata
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import com.ifgarces.tomaramosuandes.utils.onTextChangedListener
import com.ifgarces.tomaramosuandes.utils.spanishNonAccent
import com.ifgarces.tomaramosuandes.utils.spanishUpperCase


/**
 * Displays the available `Ramo`s catalog for the current period.
 */
class CatalogFragment : Fragment() {

    private class FragmentUI(owner :View) {
        val recycler        :RecyclerView = owner.findViewById(R.id.catalog_recycler)
        val searchboxLayout :TextInputLayout = owner.findViewById(R.id.catalog_searchBox_layout)
        val searchBox       :EditText = owner.findViewById(R.id.catalog_searchBox)
    }

    private lateinit var UI :FragmentUI
    private lateinit var catalogRamosAdapter :CatalogRamosAdapter

    override fun onCreateView(
        inflater :LayoutInflater, container :ViewGroup?, savedInstanceState :Bundle?
    ) :View {
        val fragView :View = inflater.inflate(
            if (DataMaster.getUserStats().nightModeOn) R.layout.night_fragment_catalog
            else R.layout.fragment_catalog,
            container, false
        )
        this.UI = FragmentUI(owner = fragView)

        (this.requireActivity() as HomeActivity).let { homeActivity :HomeActivity ->
            val catalogPeriod :String
            val catalogLastUpdateDate :String
            homeActivity.getLatestAppMetadat().let { meta :AppMetadata? ->
                if (meta != null) {
                    catalogPeriod = meta.catalogCurrentPeriod
                    catalogLastUpdateDate = meta.catalogLastUpdated
                } else {
                    catalogPeriod = homeActivity.getString(R.string.catalog_period)
                    catalogLastUpdateDate = "[offline]"
                }
            }

            homeActivity.setTopToolbarValues(
                title = "Catálogo de Ramos",
                subtitle = "${catalogPeriod}, actualizado el ${catalogLastUpdateDate}",
                onClick = {
                    this.showHelp()
                }
            )

            this.catalogRamosAdapter = CatalogRamosAdapter(
                data = DataMaster.getCatalogRamos().toMutableList(),
                colorizeInscribed = false
            )
            UI.recycler.adapter = this.catalogRamosAdapter
            UI.recycler.layoutManager = LinearLayoutManager(homeActivity)
            UI.searchboxLayout.setStartIconOnClickListener {
                this.clearSearch()
            }
            UI.searchBox.onTextChangedListener {
                if (it.length > 1) {
                    this.applySearch(searchText = it)
                }
            }
        }

        return fragView
    }

    /**
     * Prompts a dialog with information/help about this view.
     */
    private fun showHelp() {
        this.requireContext().infoDialog(
            title = "Ayuda - Catálogo de ramos",
            message = """\
Presione un ramo para ver detalles y eventualmente tomarlo. Los ramos con fondo azul \
son los que ud. ya ha tomado. Puede usar la caja de búsqueda para filtrar ramos por \
nombre o NRC.""".multilineTrim()
        )
    }

    /**
     * Modifies the catalog's `RecyclerView` to show `Ramo`s whose `nombre` attribute contains
     * `searchText`.
     */
    private fun applySearch(searchText :String) { // TODO: tests.
        val results :MutableList<Ramo> = mutableListOf()
        val sanitizedSearchText :String = searchText.spanishNonAccent().spanishUpperCase().trim()
        DataMaster.getCatalogRamos().forEach {
            // Considering `nombre` is upper-cased for each `Ramo`
            if (
                it.nombre.spanishNonAccent().contains(sanitizedSearchText) ||
                it.NRC.toString().contains(sanitizedSearchText)
            ) {
                if (it !in results) {
                    results.add(it)
                }
            }
        }
        this.catalogRamosAdapter.updateData(data = results)
        if (results.count() == 0) {
            UI.recycler.visibility = View.INVISIBLE
        } else {
            UI.recycler.visibility = View.VISIBLE
        }
    }

    /**
     * Undoes the search, i.e. shows again the full catalog.
     */
    private fun clearSearch() {
        UI.searchBox.setText("")
        this.catalogRamosAdapter.updateData(data = DataMaster.getCatalogRamos().toMutableList())
        UI.recycler.visibility = View.VISIBLE
    }
}