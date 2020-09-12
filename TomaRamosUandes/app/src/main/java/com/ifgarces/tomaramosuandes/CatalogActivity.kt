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
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.onTextChangedListener
import com.ifgarces.tomaramosuandes.utils.spanishNonAccent
import com.ifgarces.tomaramosuandes.utils.spanishUpperCase


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
        //UI.searchButton.setOnClickListener { this.searchFilterCatalog() }
        UI.searchBox_layout.setStartIconOnClickListener { this.clearSearch() }
        UI.searchBox.onTextChangedListener {
            if (it.length > 2) this.searchFilterCatalog(searchText=it)
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

    private fun showHelp() {
        // TODO: show catalog help. Maybe show BottomDialogFragment instead of regular dialog.
    }

    /* Searches `Ramo`s in the catalog by `nombre` */
    private fun searchFilterCatalog(searchText :String) { // TODO: tests.
        val results :MutableList<Ramo> = mutableListOf()
        DataMaster.getCatalogRamos().forEach {
            // considering `nombre` is upper cased for each `Ramo`
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

    /* Shows again the full catalog */
    private fun clearSearch() {
        UI.searchBox.setText("")
        (UI.recycler.adapter as CatalogRamosAdapter).updateData(data=DataMaster.getCatalogRamos().toMutableList())
        UI.recycler.visibility = View.VISIBLE
    }
}