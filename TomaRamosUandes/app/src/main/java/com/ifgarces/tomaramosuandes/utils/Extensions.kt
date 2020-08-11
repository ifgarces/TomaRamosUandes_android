package com.ifgarces.tomaramosuandes.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import java.util.Locale
import java.lang.String.format as sprintf


public const val LOG_TAG :String = "_DEBUGLOG_" // logging is not heavy, all of it will be labeled with this string

/* Toast + sprintf */
fun Context.toastf(format :String, vararg args :Any?) {
    Toast.makeText(this, sprintf(format, *args), Toast.LENGTH_LONG).show()
}

/* Debug log + sprintf */
fun Logf(format :String, vararg args :Any?) {
    Log.d(LOG_TAG, sprintf(format, *args))
}

fun String.spanishUpperCase() : String {
    return this.toUpperCase( Locale("es", "ES") )
}

fun String.spanishLowerCase() : String {
    return this.toLowerCase( Locale("es", "ES") )
}

/* For large block literal strings. Makes possible to continue a line with "\\" character, ignoring extra whitespaces. */
fun String.multilineFormat() : String { // Note: does not ignore tabs "\t".
    val NEWLINE_MARKER :String = "+++|♣♦♠♥|+++"
    return this
        .replace("\\\n", "") // continuing line
        .replace("\n", NEWLINE_MARKER) // avoiding intentioned newlines to be erased next
        .replace(Regex("\\s+"), " ") // removing 'redundant' whitespaces (also newlines). References: https://stackoverflow.com/a/37070443
        .replace(NEWLINE_MARKER, "\n") // recovering intentioned newlines
        .trim()
}

/* Gets the equivalent non-accented spanish character, with the exception of "ñ"/"Ñ" */
fun String.spanishNonAccent() : String {
    val lowerAccents    :String = "áéíóúü"
    val upperAccents    :String = "ÁÉÍÓÚÜ"
    val lowerNonAccents :String = "aeiouu"
    val upperNonAccents :String = "AEIOUU"

    var result :String = this
    for (k :Int in (0 until lowerAccents.length)) {
        result = result
            .replace(lowerAccents[k], lowerNonAccents[k])
            .replace(upperAccents[k], upperNonAccents[k])
    }
    return result
}

/* Executes `action` when the widget `text` is changed */
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

/* Dialog simple que muestra un texto */
fun Context.infoDialog(
    title     :String,
    message   :String,
    onDismiss :() -> Unit = {}
) {
    val diag :AlertDialog.Builder = AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok) { dialog :DialogInterface, _ :Int ->
            onDismiss.invoke()
            dialog.dismiss()
        }
    diag.create().show()
}

/* Dialog con botónes SÍ/NO */
fun Context.yesNoDialog(
    title        :String,
    message      :String,
    onYesClicked :() -> Unit,
    onNoClicked  :() -> Unit = {}
) {
    val diag :AlertDialog.Builder = AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(android.R.string.yes) { dialog :DialogInterface, _ :Int ->
            onYesClicked.invoke()
            dialog.dismiss()
        }
        .setNegativeButton(android.R.string.no) { dialog :DialogInterface, _ :Int ->
            onNoClicked.invoke()
            dialog.dismiss()
        }
    diag.create().show()
}