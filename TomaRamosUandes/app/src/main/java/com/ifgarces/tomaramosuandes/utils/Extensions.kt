package com.ifgarces.tomaramosuandes.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.AppMetadata
import com.ifgarces.tomaramosuandes.models.Ramo
import java.util.Locale


/**
 * Simple long toast with Java-style string formatting.
 * @author Ignacio F. Garcés.
 */
fun Context.toastf(format :String, vararg args :Any?) {
    Toast.makeText(this, format.format(*args), Toast.LENGTH_LONG).show()
}

fun String.spanishUpperCase() :String {
    return this.uppercase(Locale("es", "ES"))
}

fun String.spanishLowerCase() :String {
    return this.lowercase(Locale("es", "ES"))
}

/**
 * For large block literal strings. Makes possible to continue a line with "\\" character, ignoring
 * extra whitespaces.
 */
fun String.multilineTrim() :String { // Note: does not ignore tabs "\t".
    return this.replace("\\\n", "").trim() //TODO: unit tests
    //? Is this really needed? try with a standarized alternative for this.
}

/**
 * Gets the equivalent non-accented spanish character, with the exception of "ñ"/"Ñ".
 */
fun String.spanishNonAccent() :String {
    val lowerAccents :String = "áéíóúü"
    val upperAccents :String = "ÁÉÍÓÚÜ"
    val lowerNonAccents :String = "aeiouu"
    val upperNonAccents :String = "AEIOUU"

    var result :String = this
    for (k :Int in (lowerAccents.indices)) {
        result = result
            .replace(lowerAccents[k], lowerNonAccents[k])
            .replace(upperAccents[k], upperNonAccents[k])
    }
    return result
}

/**
 * Executes `action` when the widget `text` is changed.
 * @param action Callback to be executed when the text is changed. The current text will be passed
 * as a parameter.
 */
fun EditText.onTextChangedListener(action :(text :String) -> Unit) {
    this.addTextChangedListener(
        object : TextWatcher {
            override fun afterTextChanged(s :Editable) {}
            override fun beforeTextChanged(s :CharSequence, start :Int, count :Int, after :Int) {}
            override fun onTextChanged(s :CharSequence, start :Int, before :Int, count :Int) {
                action.invoke(s.toString())
            }
        }
    )
}

/**
 * Simple `AlertDialog` that shows text information.
 * @author Ignacio F. Garcés.
 * @param title Dialog title. If it is "", somehow the icon is not shown. Should use a blank string
 * like " " insted, if desired to show a dialog without title.
 * @param message Dialog body.
 * @param onDismiss Callback executed when the dialog is dismissed by the user.
 * @param icon Dialog icon, placed to the left of the title. Must be a drawable resource ID.
 */
fun Context.infoDialog(
    title :String,
    message :String,
    onDismiss :() -> Unit = {},
    icon :Int? = null
) {
    val diag :AlertDialog.Builder = AlertDialog.Builder(
        this,
        if (DataMaster.getUserStats().nightModeOn) R.style.myNightDialogTheme
            else R.style.myDialogTheme
    )
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok) { dialog :DialogInterface, _ :Int ->
            onDismiss.invoke()
            dialog.dismiss()
        }
    if (icon != null) {
        diag.setIcon(icon)
    }
    diag.create().show()
}

/**
 * `AlertDialog` with yes/no buttons.
 * @author Ignacio F. Garcés.
 * @param title Dialog title. If it is "", somehow the icon is not shown. Should use a blank string
 * like " " insted, if desired to show a dialog without title.
 * @param message Dialog body.
 * @param onYesClicked Callback executed when the user presses the possitive button.
 * @param onNoClicked Callback executed when the user presses the negative button.
 * @param icon Dialog icon, placed to the left of the title. Must be a drawable resource ID.
 */
fun Context.yesNoDialog(
    title :String,
    message :String,
    onYesClicked :() -> Unit,
    onNoClicked :() -> Unit = {},
    icon :Int? = null
) {
    val diag :AlertDialog.Builder = AlertDialog.Builder(
        this,
        if (DataMaster.getUserStats().nightModeOn) R.style.myNightDialogTheme
            else R.style.myDialogTheme
    )
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton("Sí") { dialog :DialogInterface, _ :Int ->
            onYesClicked.invoke()
            dialog.dismiss()
        }
        .setNegativeButton("No") { dialog :DialogInterface, _ :Int ->
            onNoClicked.invoke()
            dialog.dismiss()
        }
    if (icon != null) {
        diag.setIcon(icon)
    }
    diag.create().show()
}

/**
 * Enters "inmersive mode", hiding system UI elements (navigation and notification bars) and expanding
 * the app to fill all of the screen.
 */
fun Activity.enterFullScreen() {
    if (Build.VERSION.SDK_INT >= 30) { // ref: https://stackoverflow.com/a/68055924/12684271
        with(
            WindowInsetsControllerCompat(
                window,
                window.decorView
            )
        ) {
            this.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
            this.hide(systemBars())
        }
    } else { // ref: https://stackoverflow.com/a/66526368/12684271
        if (this.actionBar != null) this.actionBar!!.hide()
        this.window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }
}

/**
 * Undoes `Activity.enterFullScreen()`.
 */
fun Activity.exitFullScreen() {
    if (Build.VERSION.SDK_INT >= 30) { // ref: https://stackoverflow.com/a/68055924/12684271
        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).show(systemBars())
    } else { // ref: https://developer.android.com/training/system-ui/immersive
        this.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }
}

/**
 * Casting a Firebase doc to `Ramo`. Don't want to use `.toObject`, as the model would have to be
 * modified (allowing empty constructor, which is bad actually, I prefer this).
 */
fun QueryDocumentSnapshot.toRamo() :Ramo {
    return Ramo(
        NRC = this.data.getValue("nrc")!!.toString().toInt(),
        nombre = this.data.getValue("nombre")!!.toString(),
        profesor = this.data.getValue("profesor")!!.toString(),
        créditos = this.data.getValue("créditos")!!.toString().toInt(),
        materia = this.data.getValue("materia")!!.toString(),
        curso = this.data.getValue("curso")!!.toString().toInt(),
        sección = this.data.getValue("sección")!!.toString(),
        planEstudios = this.data.getValue("planEstudios")!!.toString(),
        conectLiga = this.data.getValue("conectLiga")!!.toString(),
        listaCruzada = this.data.getValue("listaCruzada")!!.toString()
    )
}

/**
 * Similarly to `QueryDocumentSnapshot.toRamo()`, casts a `QueryDocumentSnapshot` to `AppMetadata`,
 * avoiding `.toObject` constraints for the target class.
 */
fun QueryDocumentSnapshot.toAppMetadata() :AppMetadata {
    return AppMetadata(
        latestVersionName = this.data.getValue("latestVersionName")!!.toString(),
        catalogCurrentPeriod = this.data.getValue("catalogCurrentPeriod")!!.toString(),
        catalogLastUpdated = this.data.getValue("catalogLastUpdated")!!.toString(),
    )
}

/**
 * On-click behaviour for a button `toggleButton` that should collapse/expand (hide/unhide) a
 * given target view `targetContainer` (likely to be a Layout).
 * @param isCollapsed Wether the current section is collapsed or not right when the user clicks
 * the collapse/expand toggle button.
 * @param toggleButton Button for collapsing/expanding.
 * @param targetContainer Target view for the elements that are hidden/shown.
 */
fun Context.toggleCollapseViewButton(
    isCollapsed :Boolean, toggleButton :MaterialButton, targetContainer :View
) {
    targetContainer.visibility = if (isCollapsed) View.VISIBLE else View.GONE
    //TODO: add basic appear/dissapear animation
    toggleButton.icon = ContextCompat.getDrawable(
        this, if (isCollapsed) R.drawable.arrow_tip_down else R.drawable.arrow_tip_right
    )
}
