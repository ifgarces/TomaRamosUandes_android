package com.ifgarces.tomaramosuandes.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.widget.Toast
import java.util.Locale
import java.lang.String.format as sprintf


public const val LOG_TAG :String = "_DEBUGLOG_"

/* Toast + sprintf */
fun Context.toastf(format :String, vararg args :Any?) {
    Toast.makeText(this, sprintf(format, *args), Toast.LENGTH_LONG).show()
}

/* Debug log + sprintf */
fun Logf(format :String, vararg args :Any?) {
    Log.d(LOG_TAG, sprintf(format, *args))
}

fun String.spanishUpperCase() : String {
    return this.toUpperCase( Locale("ES", "ES") )
}

fun String.spanishLowerCase() : String {
    return this.toLowerCase( Locale("ES", "ES") )
}

/* For large block literal strings. Makes possible to continue a line with "\\" character, ignoring extra whitespaces. */
fun String.multilineFormat() : String { // Note: does not ignore tabs "\t".
    return this
        .replace("\\\n", "") // continuing line
        .replace(Regex("\\s+"), " ") // removing 'redundant' whitespaces. References: https://stackoverflow.com/a/37070443
}

/* Gets the equivalent non-accented spanish character, with the exception of "ñ"/"Ñ" */
fun String.spanishNonAccent() : String {
    val lowerAccents    :String = "áéíóúü"
    val upperAccents    :String = "ÁÉÍÓÚü"
    val lowerNonAccents :String = "aeiouu"
    val upperNonAccents :String = "AEIOUU"

    var result :String = ""
    for (char :Char in this) {
        for ( k :Int in (0 until lowerAccents.count()) ) {
            when (char) {
                lowerAccents[k] -> { result += lowerNonAccents[k] }
                upperAccents[k] -> { result += upperNonAccents[k] }
                else -> { result += char }
            }
        }
    }
    return result
}

/* Dialog simple que muestra un texto */
fun Context.infoDialog(
    title :String,
    message :String,
    onDismiss :() -> Unit = {}
) {
    val diag_builder :AlertDialog.Builder = AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok) { dialog :DialogInterface, _ :Int ->
            onDismiss.invoke()
            dialog.dismiss()
        }
    diag_builder.create().show()
}

/* Dialog con botónes SÍ/NO */
fun Context.yesNoDialog(
    title :String,
    message :String,
    onYesClicked :() -> Unit,
    onNoClicked :() -> Unit = {}
) {
    val diag_builder :AlertDialog.Builder = AlertDialog.Builder(this)
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
    diag_builder.create().show()
}