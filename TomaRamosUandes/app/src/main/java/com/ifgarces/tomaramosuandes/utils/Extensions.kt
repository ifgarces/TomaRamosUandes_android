package com.ifgarces.tomaramosuandes.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.ifgarces.tomaramosuandes.R
import kotlin.reflect.KClass
import java.util.Locale


const val LOGF_TAG :String = "_DEBUGLOG_"

/**
 * Simple long toast with Java-style string formatting.
 * @author Ignacio F. Garcés.
 */
fun Context.toastf(format :String, vararg args :Any?) {
    Toast.makeText(this, format.format(*args), Toast.LENGTH_LONG).show()
}

/**
 * Simplified debug log with Java-style string formatting. All log under the same tag stored at
 * `LOG_TAG` constant, as by now logging is not heavy. This function is not an actual extended
 * method, but I still place it here. We will tell nobody.
 * @author Ignacio F. Garcés.
 * @param scopeClass The class of the fragment or activity (or object) of the scope that desires to
 * log.
 * @param format String format.
 * @param args String format args.
 */
fun Logf(scopeClass :KClass<*>, format :String, vararg args :Any?) {
    Log.d(LOGF_TAG, "[%s] %s".format(scopeClass.simpleName!!, format.format(*args)))
}

fun String.spanishUpperCase() :String {
    return this.toUpperCase( Locale("es", "ES") )
}

fun String.spanishLowerCase() :String {
    return this.toLowerCase( Locale("es", "ES") )
}

/**
 * For large block literal strings. Makes possible to continue a line with "\\" character, ignoring
 * extra whitespaces.
 */
fun String.multilineTrim() :String { // Note: does not ignore tabs "\t".
    return this.replace("\\\n", "").trim()
//    val newline_internal_marker :String = "+++|♣♦♠♥|+++"
//    return this
//        .replace("\\\n", newline_internal_marker) // avoiding intentioned newlines to be erased on next replace
//        .replace(Regex("\\s+"), " ") // removing 'redundant' whitespaces (also newlines). References: https://stackoverflow.com/a/37070443
//        .replace(newline_internal_marker, "\n") // recovering intentioned newlines
//        .trim()
//        .replace("\n ", "\n") // <- TODO: improve this dumb solution. Do testing.
}

/**
 * Gets the equivalent non-accented spanish character, with the exception of "ñ"/"Ñ".
 */
fun String.spanishNonAccent() :String {
    val lowerAccents    :String = "áéíóúü"
    val upperAccents    :String = "ÁÉÍÓÚÜ"
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
    title      :String,
    message    :String,
    onDismiss  :() -> Unit = {},
    icon       :Int? = null
) {
    val diag :AlertDialog.Builder = AlertDialog.Builder(this, R.style.myDialogTheme)
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
    title         :String,
    message       :String,
    onYesClicked  :() -> Unit,
    onNoClicked   :() -> Unit = {},
    icon          :Int? = null
) {
    val diag :AlertDialog.Builder = AlertDialog.Builder(this, R.style.myDialogTheme)
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
fun Activity.enterFullScreen() { // references: https://developer.android.com/training/system-ui/immersive
    this.window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_IMMERSIVE
        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_FULLSCREEN
    )
}

/**
 * Undoes `Activity.enterFullScreen()`.
 */
fun Activity.exitFullScreen() { // references: https://developer.android.com/training/system-ui/immersive
    this.window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    )
}
