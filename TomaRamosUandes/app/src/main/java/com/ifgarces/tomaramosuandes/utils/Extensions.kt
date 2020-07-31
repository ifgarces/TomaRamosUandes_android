package com.ifgarces.tomaramosuandes.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.EditText
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

/* Olvidarse del `SpannableStringBuilder` en todos lados */
fun EditText.setTextf(format :String, vararg args :Any?) {
    this.text = SpannableStringBuilder( sprintf(format, *args) )
}

fun String.spanishUpperCase() : String {
    return this.toUpperCase( Locale("ES", "ES") )
}

fun String.spanishLowerCase() : String {
    return this.toLowerCase( Locale("ES", "ES") )
}

fun String.spanishNonAccent() : String {
    val accents    :String = "áéíóúü"
    val nonAccents :String = "aeiouu"
    val result = this.spanishLowerCase()
    // TODO: finish this
    return "{TODO}"
}

/* Dialog simple que muestra un texto */
fun Context.infoDialog(title :String, message :String, onDismiss :() -> Unit = {}) {
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
    onNoClicked :() -> Unit
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