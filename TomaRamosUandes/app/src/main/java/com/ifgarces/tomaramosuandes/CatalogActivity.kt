package com.ifgarces.tomaramosuandes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.ifgarces.tomaramosuandes.utils.Logf


class CatalogActivity : AppCompatActivity() {

    private object UI {
        lateinit var topBar   :MaterialToolbar
        lateinit var recycler :RecyclerView

        fun init(owner :AppCompatActivity) {
            this.topBar   = owner.findViewById(R.id.catalog_topbar)
            this.recycler = owner.findViewById(R.id.catalog_recycler)
        }
    }

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_catalog)
        UI.init(owner=this)

        UI.recycler.adapter = CatalogAdapter(data=DataMaster.getCatalog())
        UI.recycler.layoutManager = LinearLayoutManager(this)
        UI.topBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_help -> {
                    this.showHelpDialog()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    Logf("[CatalogActivity] Warning: unknown toolbar element pressed (id=%d).", it.itemId)
                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    private fun showHelpDialog() {
        // TODO: show catalog help. Maybe show BottomDialogFragment instead of regular dialog.
    }
}