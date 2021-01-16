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

    private object UI {
        lateinit var topBar           :MaterialToolbar
        lateinit var recycler         :RecyclerView
        lateinit var searchBox_layout :TextInputLayout
        lateinit var searchBox        :EditText

        fun init(owner :AppCompatActivity) {
            this.topBar           = owner.findViewById(R.id.catalog_topbar)
            this.recycler         = owner.findViewById(R.id.catalog_recycler)
            this.searchBox_layout = owner.findViewById(R.id.catalog_searchBox_layout)
            this.searchBox        = owner.findViewById(R.id.catalog_searchBox)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_catalog)
        UI.init(owner=this)

        UI.recycler.adapter = CatalogRamosAdapter(
            data = DataMaster.getCatalogRamos().toMutableList(),
            isAllInscribed = false
        )
        UI.recycler.layoutManager = LinearLayoutManager(this)
        UI.searchBox_layout.setStartIconOnClickListener { this.clearSearch() }
        UI.searchBox.onTextChangedListener {
            if (it.length > 2) { this.applySearch(searchText=it) }
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
                son los que ud. ya ha tomado. Puede usar la caja de búsqueda para filtrar ramos por nombre.
            """.multilineTrim()
        )
        // TODO: video or animation showing fast-scroll using scrollbar.
    }

    /**
     * Modifies the catalog's `RecyclerView` to show `Ramo`s whose `nombre` attribute contains `searchText`.
     */
    private fun applySearch(searchText :String) { // TODO: tests.
        val results :MutableList<Ramo> = mutableListOf()
        DataMaster.getCatalogRamos().forEach {
            // considering `nombre` is upper-cased for each `Ramo`
            if (it.nombre.spanishNonAccent().contains(
                    searchText.spanishNonAccent().spanishUpperCase().trim()
                )
            ) {
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