package com.ifgarces.tomaramosuandes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputLayout
import com.ifgarces.tomaramosuandes.adapters.CatalogAdapter
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.spanishNonAccent
import com.ifgarces.tomaramosuandes.utils.spanishUpperCase
import com.ifgarces.tomaramosuandes.utils.toastf


class CatalogActivity : AppCompatActivity() {

    private object UI {
        lateinit var topBar           :MaterialToolbar
        lateinit var recycler         :RecyclerView
        lateinit var searchBox_layout :TextInputLayout
        lateinit var searchBox        :EditText
        lateinit var searchButton     :ImageButton

        fun init(owner :AppCompatActivity) {
            this.topBar           = owner.findViewById(R.id.catalog_topbar)
            this.recycler         = owner.findViewById(R.id.catalog_recycler)
            this.searchBox_layout = owner.findViewById(R.id.catalog_searchBox_layout)
            this.searchBox        = owner.findViewById(R.id.catalog_searchBox)
            this.searchButton     = owner.findViewById(R.id.catalog_searchButton)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_catalog)
        UI.init(owner=this)

        UI.recycler.adapter =
            CatalogAdapter(data = DataMaster.getCatalog())
        UI.recycler.layoutManager = LinearLayoutManager(this)
        UI.searchButton.setOnClickListener { this.searchFilterCatalog() }
        UI.searchBox_layout.setStartIconOnClickListener {
            this.clearSearch()
        }
        UI.topBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_help -> {
                    this.showCatalogHelp()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    Logf("[CatalogActivity] Warning: unknown toolbar element pressed (id=%d).", it.itemId)
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    private fun showCatalogHelp() {
        // TODO: show catalog help. Maybe show BottomDialogFragment instead of regular dialog.
    }

    /* Searches `Ramo`s in the catalog by `nombre` */
    private fun searchFilterCatalog() { // TODO: tests.
        // TODO: [BUG] nonAccent not working: "algebra" has no results, unlike "álgebra"
        val results :MutableList<Ramo> = mutableListOf()
        val keywords :List<String> = UI.searchBox.text.toString()
            .spanishNonAccent().spanishUpperCase().trim().split(" ")
        for (cc :Ramo in DataMaster.getCatalog()) {
            for (word :String in keywords) {
                if (word.length <= 1) { continue }
                if (cc.nombre.spanishNonAccent().contains(word) && cc !in results) {
                    results.add(cc)
                }
            }
        }
        (UI.recycler.adapter as CatalogAdapter).updateData(data=results)
        Logf("[CatalogActivity] Got %d search results.", results.count())
        if (results.count() == 0) {
            this.toastf("Sin resultados")
        }
    }

    /* Undoes the search, showing again the full catalog */
    private fun clearSearch() {
        UI.searchBox.setText("")
        (UI.recycler.adapter as CatalogAdapter).updateData(data=DataMaster.getCatalog())
    }
}