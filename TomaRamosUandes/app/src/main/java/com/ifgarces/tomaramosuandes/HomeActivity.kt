package com.ifgarces.tomaramosuandes

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ifgarces.tomaramosuandes.navigators.HomeNavigator
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.WebManager
import java.util.concurrent.Executors


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

        // Checking for updates here instead of MainAcivity, for simplicity
        Executors.newSingleThreadExecutor().execute {
            WebManager.init(activity = this)
        }

        UI.bottomNavbar.setOnItemSelectedListener { item :MenuItem ->
            when (item.itemId) {
                R.id.bottom_nav_ramos -> {
                    throw NotImplementedError() //TODO: navigate to ramos fragment
                    return@setOnItemSelectedListener true
                }
                R.id.bottom_nav_schedule -> {
                    throw NotImplementedError() //TODO: navigate to schedule fragment
                    return@setOnItemSelectedListener true
                }
                R.id.bottom_nav_evaluations -> {
                    throw NotImplementedError() //TODO: navigate to evaluations fragment
                    return@setOnItemSelectedListener true
                }
                else -> {
                    Logf("[HomeActivity] Warning: unknown bottom navbar element pressed (id=%d)", item.itemId)
                    return@setOnItemSelectedListener false
                }
            }
        }
    }

//    override fun onDestroy() { //! this will destroy everything is the user rotates the screen
//        super.onDestroy()
//        this.finishAffinity()
//    }

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
                    Logf("[HomeActivity] Warning: unknown top toolbar element pressed (id=%d)", item.itemId)
                    return@setOnMenuItemClickListener false
                }
            }
        }
    }

    /**
     * Sets the item with a given `index` in the bottom nav bar to display as selected. Used to
     * reflect navigations from one fragment to another in that bar.
     * References: https://www.py4u.net/discuss/603053
     */
    public fun setBottomNavItemSelected(index :Int) {
        UI.bottomNavbar.menu.getItem(index).isChecked = true
    }
}