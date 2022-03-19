package com.ifgarces.tomaramosuandes.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ifgarces.tomaramosuandes.BuildConfig
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.fragments.DashboardFragment
import com.ifgarces.tomaramosuandes.fragments.EvaluationsFragment
import com.ifgarces.tomaramosuandes.fragments.SchedulePortraitFragment
import com.ifgarces.tomaramosuandes.fragments.UserRamosFragment
import com.ifgarces.tomaramosuandes.models.AppMetadata
import com.ifgarces.tomaramosuandes.navigators.HomeNavigator
import com.ifgarces.tomaramosuandes.networking.FirebaseMaster
import com.ifgarces.tomaramosuandes.utils.DataMaster
import com.ifgarces.tomaramosuandes.utils.Logf
import com.ifgarces.tomaramosuandes.utils.infoDialog
import com.ifgarces.tomaramosuandes.utils.multilineTrim
import com.ifgarces.tomaramosuandes.utils.yesNoDialog
import kotlin.reflect.KClass


/**
 * Base activity for the app itself, when initialization is done in `MainActivity`.
 * @property navigator Navigator for this activity.
 * @property latestMetadata Latest got `AppMetadata` object from Firebase (if reachable), used to
 * checking for updates, etc.
 */
class HomeActivity : AppCompatActivity() {

    private class ActivityUI(owner :AppCompatActivity) {
        val topToolbar        :MaterialToolbar = owner.findViewById(R.id.home_topbar)
        val bottomNavbar      :BottomNavigationView = owner.findViewById(R.id.home_bottomNavView)
        val loadScreenOverlay :View = owner.findViewById(R.id.home_loadScreen)
    }

    private lateinit var UI :ActivityUI
    private lateinit var navigator :HomeNavigator
    private var latestMetadata :AppMetadata? = null // will remain null when Firebase Firestore is not reachable

    // Getters
    public fun getNavigator() = this.navigator
    public fun getLatestAppMetadata() = this.latestMetadata

    override fun onCreate(savedInstanceState :Bundle?) {
        super.onCreate(savedInstanceState)

        this.setContentView(
            if (DataMaster.getUserStats().nightModeOn) R.layout.night_activity_home
            else R.layout.activity_home
        )
        this.UI = ActivityUI(owner = this)
        this.navigator = HomeNavigator(homeActivity = this)

        // -----------------------------------------------------------------------------------------
        /* For easily updating the hole online catalog itself from the offline catalog, when the faculty performs changes */
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
        // version itself, etc.
        this.showLoadingScreen()
        FirebaseMaster.getAppMetadata(
            onSuccess = { gotMetadata :AppMetadata ->
                // Noticing the user in case an update is available
                this.hideLoadingScreen()
                this.latestMetadata = gotMetadata
                if (gotMetadata.latestVersionName != BuildConfig.VERSION_NAME) {
                    this.runOnUiThread {
                        this.yesNoDialog(
                            title = "Actualización disponible",
                            message = """\
Hay una nueva versión de esta app: ${gotMetadata.latestVersionName}.
¿Ir al link de descarga ahora?

* Si tiene problemas para actualizar, desisntale la app antes de instalar la nueva versión (revise \
el documento 'LÉEME' disponible en
${AppMetadata.USER_APP_URL}""".multilineTrim(),
                            onYesClicked = {
                                // Opens direct APK download link in web browser
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
            },
            onFailure = {
                this.hideLoadingScreen()
                FirebaseMaster.showInternetConnectionErrorDialog(this)
            }
        )

        // Setting click listener for bottom navbar
        UI.bottomNavbar.setOnItemSelectedListener { item :MenuItem ->
            when (item.itemId) {
                R.id.bottom_nav_dashboard -> {
                    this.navigator.toDashboard()
                    return@setOnItemSelectedListener true
                }
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
                    Logf.warn(
                        this::class, "Unknown bottom navbar element pressed (id=%d)", item.itemId
                    )
                    return@setOnItemSelectedListener false
                }
            }
        }
    }

    override fun onDestroy() { //! this will destroy everything is the user rotates the screen, should change for setting this behaviour for onBackPressed? anyway, the solution is just set the activity as portrait-mode only. That will do for now.
        super.onDestroy()
        this.finishAffinity()
    }

    /**
     * Displays a half-transparent large loading screen in front of all other views, except the top
     * toolbar, avoiding the user to click UI elements of the current fragment, etc.
     */
    public fun showLoadingScreen() {
        UI.loadScreenOverlay.visibility = View.VISIBLE
    }

    /**
     * Hides the overlay-like loading screen.
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
     * @param onHelpClick Callback to run when the "help" toolbar menu item is clicked.
     */
    public fun setTopToolbarValues(title :String, subtitle :String, onHelpClick :() -> Unit) {
        UI.topToolbar.title = title
        UI.topToolbar.subtitle = subtitle
        UI.topToolbar.setOnMenuItemClickListener { item :MenuItem ->
            when (item.itemId) {
                R.id.menu_help -> {
                    onHelpClick.invoke()
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_night_mode -> {
                    // Toggling current night mode setting and restarting current activity for
                    // applying changes
                    DataMaster.toggleNightMode(
                        onFinish = {
                            Logf.debug(
                                this::class,
                                "Switching night mode to %s (restarting this activity)".format(
                                    if (DataMaster.getUserStats().nightModeOn) "ON" else "OFF"
                                )
                            )
                            // Restarting this activity (will avoid needing to wait for
                            // `MainActivity` to initialize again, good for both performance and UX)
                            this.startActivity(
                                Intent.makeRestartActivityTask(this.intent.component)
                            )
                        }
                    )
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_feedback -> {
                    this.infoDialog(
                        title = "Feedback",
                        message = """\
Inicie sesión con su cuenta @miuandes para mandar un formulario de feedback. Gracias!
""".multilineTrim(),
                        onDismiss = {
                            // Open the URL for the user to give feedback about the application
                            this.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(AppMetadata.FEEDBACK_URL)
                                )
                            )
                        },
                        icon = R.drawable.coffee_icon
                    )
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    Logf.warn(
                        this::class, "Unknown top toolbar element pressed (id=%d)", item.itemId
                    )
                    return@setOnMenuItemClickListener false
                }
            }
        }
    }

    /**
     * Sets the item in the bottom nav bar to display as selected, given a valid fragment class.
     * Used to reflect navigations from one fragment to another in that bar.
     * References: https://www.py4u.net/discuss/603053
     * @author Ignacio F. Garcés.
     * @exception Exception When the `fragment` parameter is an unexpected class (not corresponding
     * to the Fragments attached to the bottom navbar items, which would be a dumb code mistake).
     * @param fragment The kotlin class for the Fragment desired to be displayed as selected in the
     * nav bar.
     */
    public fun setBottomNavItemSelected(fragment :KClass<*>) {
        UI.bottomNavbar.menu.getItem(
            when (fragment) {
                DashboardFragment::class -> 0
                UserRamosFragment::class -> 1
                SchedulePortraitFragment::class -> 2
                EvaluationsFragment::class -> 3
                else -> throw Exception(
                    "Invalid target fragment class '%s' for HomeActivity.setBottomNavItemSelected".format(
                        fragment.simpleName
                    )
                )
            }
        ).isChecked = true
    }
}
