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
import java.util.Locale


const val DEBUG_MODE :Boolean = true // <- turning to false during tests and release. Needed because somehow the damn tests don't support functions that write to the Log.
const val LOG_TAG    :String = "_DEBUGLOG_" // logging output is not heavy, all of it will be labeled with this string

/**
 * Toast + sprintf
 */
fun Context.toastf(format :String, vararg args :Any?) {
    Toast.makeText(this, format.format(*args), Toast.LENGTH_LONG).show()
}

/**
 * Debug log + sprintf. All log under the same tag stored at `LOG_TAG` constant.
 */
fun Logf(format :String, vararg args :Any?) {
    if (DEBUG_MODE) { Log.d(LOG_TAG, format.format(*args)) }
}

fun String.spanishUpperCase() : String {
    return this.toUpperCase( Locale("es", "ES") )
}

fun String.spanishLowerCase() : String {
    return this.toLowerCase( Locale("es", "ES") )
}

/**
 * For large block literal strings. Makes possible to continue a line with "\\" character, ignoring extra whitespaces.
 */
fun String.multilineTrim() : String { // Note: does not ignore tabs "\t".
    val NEWLINE_MARKER :String = "+++|♣♦♠♥|+++"
    return this
        .replace("\\\n", NEWLINE_MARKER) // avoiding intentioned newlines to be erased on next replace
        .replace(Regex("\\s+"), " ") // removing 'redundant' whitespaces (also newlines). References: https://stackoverflow.com/a/37070443
        .replace(NEWLINE_MARKER, "\n") // recovering intentioned newlines
        .trim()
}

/**
 * Gets the equivalent non-accented spanish character, with the exception of "ñ"/"Ñ".
 */
fun String.spanishNonAccent() : String {
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
 */
fun EditText.onTextChangedListener(action : (text :String) -> Unit) {
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
 * Simple dialog that shows some information.
 * @param title Dialog title.
 * @param message Dialog body.
 * @param onDismiss Executes this when it is dismissed by the user.
 * @param icon Dialog icon, placed to the left of the title. Must be a drawable resource ID.
 */
fun Context.infoDialog(
    title      :String, // note: if `title` is "", somehow the icon is not shown. Should use " " or similar insted.
    message    :String,
    onDismiss  :() -> Unit = {},
    icon       :Int? = null // resource reference
) {
    val diag :AlertDialog.Builder = AlertDialog.Builder(this)
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
 * Dialog with yes/no buttons.
 * @param title Dialog title.
 * @param message Dialog body.
 * @param onYesClicked Action to be performed when the user presses the possitive button.
 * @param onNoClicked Action to be performed when the user presses the negative button.
 * @param icon Dialog icon, placed to the left of the title. Must be a drawable resource ID.
 */
fun Context.yesNoDialog(
    title         :String, // note: if `title` is "", somehow the icon is not shown. Should use " " or similar insted.
    message       :String,
    onYesClicked  :() -> Unit,
    onNoClicked   :() -> Unit = {},
    icon          :Int? = null // resource reference
) {
    val diag :AlertDialog.Builder = AlertDialog.Builder(this)
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
 * Enters 'inmersive mode', hiding system UI elements (navigation and notification bars) and expanding
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