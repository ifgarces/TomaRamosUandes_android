package com.ifgarces.tomaramosuandes.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.fragments.EvaluationsFragment
import com.ifgarces.tomaramosuandes.fragments.SchedulePortraitFragment
import com.ifgarces.tomaramosuandes.fragments.UserRamosFragment
import com.ifgarces.tomaramosuandes.models.AppMetadata
import com.ifgarces.tomaramosuandes.navigators.HomeNavigator
import com.ifgarces.tomaramosuandes.networking.FirebaseMaster
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import com.ifgarces.tomaramosuandes.utils.yesNoDialog
import kotlin.reflect.KClass


class HomeActivity : AppCompatActivity() {

    private class ActivityUI(owner :AppCompatActivity) {
        val topToolbar        :MaterialToolbar = owner.findViewById(R.id.homeActivity_topbar)
        val bottomNavbar      :BottomNavigationView = owner.findViewById(R.id.homeActivity_bottomNavView)
        val loadScreenOverlay :View = owner.findViewById(R.id.homeActivity_loadScreen)
    }

    private lateinit var UI :ActivityUI
    private lateinit var navigator :HomeNavigator
    private var latestMetadata :AppMetadata? = null // will remain null internet is not available

    // Getters
    public fun getNavigator() = this.navigator
    public fun getLatestAppMetadat() = this.latestMetadata

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_home)
        this.UI = ActivityUI(owner = this)
        this.navigator = HomeNavigator(homeActivity = this)

        // -----------------------------------------------------------------------------------------
        /* For updating app metadata when the catalog or app version changes */
//        FirebaseMaster.Developer.updateAppMetadata(
//            metadata = AppMetadata(
//                latestVersionName = this.getString(R.string.APP_VERSION),
//                catalogCurrentPeriod = this.getString(R.string.CATALOG_PERIOD),
//                catalogLastUpdated = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
//            ),
//            onSuccess = {
//                this.toastf("AppMetadata uploaded successfully")
//            },
//            onFailure = {
//
//            }
//        )

        /* For updating the catalog itself */
//        FirebaseMaster.Developer.uploadRamoCollection(
//            ramos = DataMaster.getCatalogRamos(),
//            onFirstFailureCallback = {
//                this.infoDialog("Error", "Couldn't upload ramos")
//            }
//        )
//        FirebaseMaster.Developer.uploadEventCollection(
//            events = DataMaster.getCatalogEvents(),
//            onFirstFailureCallback = {
//                this.infoDialog("Error", "Couldn't upload events")
//            }
//        )
        // -----------------------------------------------------------------------------------------

        // Showing progress bar until we get app metadata for the latest catalog version, app
        // version itself (check for updates), etc.
        this.showLoadingScreen()
        FirebaseMaster.getAppMetadata(
            onSuccess = { gotMetadata :AppMetadata ->
                this.hideLoadingScreen()
                this.latestMetadata = gotMetadata // storing latest metadata in memory
                if (gotMetadata.latestVersionName != this.getString(R.string.APP_VERSION)) {
                    this.runOnUiThread { // update available
                        this.showUpdateAvailableDialog(gotMetadata)
                    }
                }
            },
            onFailure = {
                this.hideLoadingScreen()
                FirebaseMaster.showInternetConnectionErrorDialog(this)
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

    override fun onDestroy() { //! this will destroy everything is the user rotates the screen, should change for setting this behaviour for onBackPressed? anyway, the solution is just set the activity as portrait-mode only.
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

    public fun showUpdateAvailableDialog(mostRecientMetadata :AppMetadata) {
        this.yesNoDialog(
            title = "Actualización disponible",
            message = """\
Hay una nueva versión de esta app: ${mostRecientMetadata.latestVersionName}.
¿Ir al link de descarga ahora?

* Si tiene problemas para actualizar, desinstale la app manualmente e instale la versión más \
reciente.""".multilineTrim(),
            onYesClicked = { // opens direct APK download link in default browser
                this.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(AppMetadata.APK_DOWNLOAD_URL)
                    )
                )
            },
            icon = R.drawable.exclamation_icon
        )
    }
}