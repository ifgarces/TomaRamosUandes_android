package com.ifgarces.tomaramosuandes

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import com.ifgarces.tomaramosuandes.adapters.CatalogRamosAdapter
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.utils.*


class CatalogActivity : AppCompatActivity() {

    private class ActivityUI(owner :AppCompatActivity) {
        val topBar           :MaterialToolbar = owner.findViewById(R.id.catalog_topbar)
        val recycler         :RecyclerView = owner.findViewById(R.id.catalog_recycler)
        val searchBox_layout :TextInputLayout = owner.findViewById(R.id.catalog_searchBox_layout)
        val searchBox        :EditText = owner.findViewById(R.id.catalog_searchBox)
    }
    private lateinit var UI :ActivityUI

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_catalog)
        this.UI = ActivityUI(owner=this)

        UI.recycler.adapter = CatalogRamosAdapter(
            data = DataMaster.getCatalogRamos().toMutableList(),
            isAllInscribed = false
        )
        UI.recycler.layoutManager = LinearLayoutManager(this)
        UI.searchBox_layout.setStartIconOnClickListener { this.clearSearch() }
        UI.searchBox.onTextChangedListener {
            if (it.length > 1) { this.applySearch(searchText=it) }
        }
        try {
            UI.topBar.subtitle = "%s, actualizado el %s".format(WebManager.getCatalogLastPeriodName(), WebManager.getCatalogLastUpdateDate())
        }
        catch (e :Exception) {
            // Catching error when navigating to `CatalogActivity` before the async tasks of `WebManager` are finished.
            this.toastf("Aún no se cargan los ramos, por favor espere")
            this.finish()
        }
        UI.topBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_help -> {
                    this.showHelp()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    Logf("[CatalogActivity] Warning: unknown toolbar element pressed (id=%d).", it.itemId)
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    /**
     * Prompts a dialog with information/help about this view.
     */
    private fun showHelp() {
        this.infoDialog(
            title = "Ayuda - Catálogo de ramos",
            message = """
                Presione un ramo para ver detalles y eventualmente tomarlo. Los ramos con fondo azul
                son los que ud. ya ha tomado. Puede usar la caja de búsqueda para filtrar ramos por
                nombre o NRC.
            """.multilineTrim()
        )
        // TODO: video or animation showing fast-scroll using scrollbar.
    }

    /**
     * Modifies the catalog's `RecyclerView` to show `Ramo`s whose `nombre` attribute contains `searchText`.
     */
    private fun applySearch(searchText :String) { // TODO: tests.
        val results :MutableList<Ramo> = mutableListOf()
        val sanitizedSearchText :String = searchText.spanishNonAccent().spanishUpperCase().trim()
        DataMaster.getCatalogRamos().forEach {
            // considering `nombre` is upper-cased for each `Ramo`
            if (it.nombre.spanishNonAccent().contains(sanitizedSearchText) || (it.NRC.toString().contains(sanitizedSearchText))) {
                if (it !in results) { results.add(it) }
            }
        }
        (UI.recycler.adapter as CatalogRamosAdapter).updateData(data=results)
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
        (UI.recycler.adapter as CatalogRamosAdapter).updateData(data=DataMaster.getCatalogRamos().toMutableList())
        UI.recycler.visibility = View.VISIBLE
    }
}