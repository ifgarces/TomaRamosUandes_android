package com.ifgarces.tomaramosuandes.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.EditText
import android.widget.Toast
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

/* Dialog simple que muestra un texto */
fun Context.infoDialogf(title :String, msg_format :String, vararg msg_args :Any?) {
    val diag_builder :AlertDialog.Builder = AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(sprintf(msg_format, *msg_args))
        .setCancelable(false)
        .setPositiveButton("OK") { dialog :DialogInterface, _ :Int -> dialog.dismiss() }
    diag_builder.create().show()
}

/* Dialog con botónes SÍ/NO */
fun Context.yesNoDialog(
    title :String,
    message :String,
    onYes :() -> Unit, // al presionar SÍ
    onNo :() -> Unit   // al presionar NO
) {
    val diag_builder :AlertDialog.Builder = AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(android.R.string.yes) { dialog :DialogInterface, _ :Int ->
            onYes.invoke()
            dialog.dismiss()
        }
        .setNegativeButton(android.R.string.no) { dialog :DialogInterface, _ :Int ->
            onNo.invoke()
            dialog.dismiss()
        }
    diag_builder.create().show()
}