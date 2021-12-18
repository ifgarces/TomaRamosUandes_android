package com.ifgarces.tomaramosuandes.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.gson.Gson
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.fragments.EvaluationsFragment
import com.ifgarces.tomaramosuandes.fragments.SchedulePortraitFragment
import com.ifgarces.tomaramosuandes.fragments.UserRamosFragment
import com.ifgarces.tomaramosuandes.models.RamoEvent
import com.ifgarces.tomaramosuandes.navigators.HomeNavigator
import com.ifgarces.tomaramosuandes.networking.FirebaseMaster
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.WebManager
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.jsonToRamoEvent
import com.ifgarces.tomaramosuandes.utils.toJson
import kotlin.reflect.KClass


class HomeActivity : AppCompatActivity() {

    private class ActivityUI(owner :AppCompatActivity) {
        val topToolbar        :MaterialToolbar = owner.findViewById(R.id.homeActivity_topbar)
        val bottomNavbar      :BottomNavigationView = owner.findViewById(R.id.homeActivity_bottomNavView)
        val loadScreenOverlay :View = owner.findViewById(R.id.homeActivity_loadScreen)
    }

    private lateinit var UI :ActivityUI
    private lateinit var navigator :HomeNavigator
    public fun getNavigator() = this.navigator

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_home)
        this.UI = ActivityUI(owner = this)
        this.navigator = HomeNavigator(homeActivity = this)

        // -----------------------------------------------------------------------------------------

        val gson = Gson()
        var got :RamoEvent
        DataMaster.getCatalogEvents().forEach { expected :RamoEvent ->
            got = expected.toJson().jsonToRamoEvent()
            if (got != expected)
                Logf(this::class, "Values do not match: %s != %s", got, expected)
        }

//        var failed :Boolean = false
//        FirebaseMaster.uploadEvents {
//            if (!failed) {
//                failed = true
//                this.infoDialog(
//                    title = "Events upload failed",
//                    message = "An error ocurred when uploading the Events to Firebase"
//                )
//            }
//        }




        // -----------------------------------------------------------------------------------------

        // Checking for updates here and blocking navigation until this async task is completed
        this.showLoadingScreen()
        WebManager.init(
            activity = this,
            onFinish = { success :Boolean ->
                this.runOnUiThread {
                    this.hideLoadingScreen()
                    if (!success) {
                        this.infoDialog(
                            title = "Error de conexión",
                            message = "No se pudo verificar la actualización de la app",
                            onDismiss = {},
                            icon = R.drawable.alert_icon
                        )
                    }
                }
            }
        )

        UI.bottomNavbar.setOnItemSelectedListener { item :MenuItem ->
            when (item.itemId) {
                R.id.bottom_nav_ramos -> {
                    this.navigator.toUserRamos()
                    return@setOnItemSelectedListener true
                }
                R.id.bottom_nav_schedule -> {
                    this.navigator.toSchedulePortrait()
                    return@setOnItemSelectedListener true
                }
                R.id.bottom_nav_evaluations -> {
                    this.navigator.toEvaluations()
                    return@setOnItemSelectedListener true
                }
                else -> {
                    Logf(this::class, "Warning: unknown bottom navbar element pressed (id=%d)", item.itemId)
                    return@setOnItemSelectedListener false
                }
            }
        }
    }

    override fun onDestroy() { //! this will destroy everything is the user rotates the screen, should change for setting this behaviour for onBackPressed?
        super.onDestroy()
        this.finishAffinity()
    }

    /**
     * Displays a traslucid large loading screen in front of all other views (except top toolbar).
     */
    public fun showLoadingScreen() {
        UI.loadScreenOverlay.visibility = View.VISIBLE
    }

    /**
     * Hides the traslucid loading screen.
     */
    public fun hideLoadingScreen() {
        UI.loadScreenOverlay.visibility = View.GONE
    }

    /**
     * Method for modifying the title and subtitle for the top toolbar, and the onClick behavior for
     * its "help" button located. Intended to be called when fragments are initialized, so as for
     * the toolbar to be loaded just once (on the activity) and its behaviour is updated on the fly
     * depending on the currently active fragment.
     * @author Ignacio F. Garcés.
     * @param onClick Callback to run when the "help" button is clicked.
     */
    public fun setTopToolbarValues(title :String, subtitle :String, onClick :() -> Unit) {
        UI.topToolbar.title = title
        UI.topToolbar.subtitle = subtitle
        UI.topToolbar.setOnMenuItemClickListener { item :MenuItem ->
            when (item.itemId) {
                R.id.menu_help -> {
                    onClick.invoke()
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    Logf(this::class, "Warning: unknown top toolbar element pressed (id=%d)", item.itemId)
                    return@setOnMenuItemClickListener false
                }
            }
        }
    }

    /**
     * Sets the item in the bottom nav bar to display as selected, given a fragment class. Used to
     * reflect navigations from one fragment to another in that bar.
     * References: https://www.py4u.net/discuss/603053
     * @author Ignacio F. Garcés.
     * @param fragment The kotlin class for the fragment desired to be displayed as selected in the
     * nav bar.
     */
    public fun setBottomNavItemSelected(fragment :KClass<*>) {
        val index :Int? = when(fragment) {
            UserRamosFragment::class -> 0
            SchedulePortraitFragment::class -> 1
            EvaluationsFragment::class -> 2
            else -> null
        }
        UI.bottomNavbar.menu.getItem(index!!).isChecked = true
    }
}