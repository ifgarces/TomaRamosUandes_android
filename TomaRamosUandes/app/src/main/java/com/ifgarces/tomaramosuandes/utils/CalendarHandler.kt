package com.ifgarces.tomaramosuandes.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.CalendarContract
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import com.ifgarces.tomaramosuandes.DataMaster
import com.ifgarces.tomaramosuandes.R
import com.ifgarces.tomaramosuandes.models.Ramo
import com.ifgarces.tomaramosuandes.models.RamoEvent
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


/**
 * Worries about exporting the `RamoEvent` as actual events to the user's default calendar.
 */
object CalendarHandler {

    /**
     * Represents a calendar of the user's device.
     * @property ID Calendar ID or index, i.e. the value for `CalendarContract.Events.CALENDAR_ID`
     * @property name Calendar name.
     */
    private data class UserCalendar(
        val ID   :Int,
        val name :String
    )

    /**
     * Returns the list of all calendars of the user's device.
     */
    @SuppressLint("MissingPermission")
    private fun getUserCalendars(activity :Activity) : List<UserCalendar> { // references: https://stackoverflow.com/a/49878686/12684271
        val results :MutableList<UserCalendar> = mutableListOf()

        val cur :Cursor = activity.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.NAME),
            null,
            null,
            null
        )!!

        while (cur.moveToNext()) {
            try {
                results.add(
                    UserCalendar(
                        ID = cur.getInt(cur.getColumnIndex(CalendarContract.Calendars._ID)),
                        name = cur.getString(cur.getColumnIndex(CalendarContract.Calendars.NAME))
                    )
                )
            }
            catch (e :NullPointerException) {
                Logf("[CalendarHandles] Error catched: null pointer exception while trying to get next calendar on cursor position %d. Calendar ignored. Details: %s", cur.position ,e)
            }
        }
        cur.close()
        return results
    }

    /**
     * Asks the user for which calendar they want to add the `RamoEvent`s.
     * @param activity The caller activity for context actions.
     * @param onItemSelected The action that will be executed once we have the calendar ID and name
     * the user clicked on.
     * @param onError Will be invoked when the clicked calendar could not be resolved, probably due
     * privacy reasons of the linked account, I think.
     */
    private fun promptSelectCalendarDialog(
        activity       :Activity,
        onItemSelected :(calendarID :Int, calendarName :String) -> Unit,
        onError        :() -> Unit
    ) {
        val userCalendars :List<UserCalendar> = this.getUserCalendars(activity)
        Logf("[CalendarHandler] Calendars in device: %s", userCalendars)
        val selectables :MutableList<String> = mutableListOf()
        userCalendars.forEach {
            selectables.add(it.name)
        }

        val diag :AlertDialog.Builder = AlertDialog.Builder(activity) // references: https://stackoverflow.com/a/43532478/12684271
            .setTitle("Seleccione calendario")
            .setCancelable(true)
            .setItems(selectables.toTypedArray()) { dialog :DialogInterface, which :Int ->
                val cal :UserCalendar? = userCalendars[which]
                Logf("[CalendarHandler] User has selected %s (index: %d)", cal, which)
                if (cal == null) {
                    onError.invoke()
                    return@setItems
                }
                onItemSelected.invoke(cal.ID, cal.name)
                dialog.dismiss()
            }
        diag.create().show()
    }

    /**
     * Exports all of the `RamoEvents` for the inscribed `Ramo` collection to the user's chosen calendar.
     * Each calendar event will have the following attributes:
     *  - Title: the name of the linked `Ramo`.
     *  - Description: the event details given by `RamoEvent.toLargeString()`.
     *  - Start time-date: given by `RamoEvent.startDate` and `RamoEvent.startTime`.
     *  - End time-date: same but with the end.
     */
    public fun exportEventsToCalendar(activity :Activity) {
        this.ensurePermissions(activity)

        this.promptSelectCalendarDialog(
            activity = activity,
            onError = {
                activity.infoDialog(
                    title = "Error",
                    icon = R.drawable.alert_icon,
                    message = """
                        No se pudo acceder al calendario escogido, probablemente por configuraciones
                        de seguridad estrictas de la cuenta asociada. Por favor intente con otro.
                    """.multilineTrim()
                )
            },
            onItemSelected = { calendarID :Int, calendarName :String ->
                val evaluations :List<RamoEvent> = DataMaster.getUserEvaluations()
                Logf("[CalendarHandler] Starting to export %d events...", evaluations.count())
                var e :ContentValues
                var result :Uri? // will hold the got internal feedback for each event insertion in calendar
                var zoneAux :ZonedDateTime
                val baseUri :Uri = Uri.parse("content://com.android.calendar/events")
                val errors :MutableList<RamoEvent> = mutableListOf() // will contain events that somehow couldn't be exported

                evaluations.forEach { event :RamoEvent ->
                    Logf("[CalendarHandler] Exporting %s", event)
                    e = ContentValues()
                    e.put(CalendarContract.Events.CALENDAR_ID, calendarID)
                    e.put(CalendarContract.Events.TITLE, DataMaster.findRamo(NRC=event.ramoNRC, searchInUserList=true)!!.nombre)
                    e.put(CalendarContract.Events.DESCRIPTION, event.toLargeString().replace("\n", ";"))

                    zoneAux = LocalDateTime.of(event.date!!, event.startTime) // we are exporting evaluations, whose date are always assigned, do not worry about NullPointerException
                        .atZone(ZoneId.of("America/Santiago"))
                    e.put(CalendarContract.Events.DTSTART, zoneAux.toInstant().toEpochMilli())
                    zoneAux = LocalDateTime.of(event.date!!, event.endTime)
                        .atZone(ZoneId.of("America/Santiago"))
                    e.put(CalendarContract.Events.DTEND, zoneAux.toInstant().toEpochMilli())

                    e.put(CalendarContract.Events.ALL_DAY, 0) // 0: false, 1: true
                    e.put(CalendarContract.Events.HAS_ALARM, 0)
                    e.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)

                    result = activity.contentResolver.insert(baseUri, e)
                    if (result == null) {
                        Logf("[CalendarHandler] Error: got null URI response when inserting the event at the calendar with ID=%d", calendarID)
                        errors.add(event)
                    }
                }

                if (errors.count() > 0) {
                    Logf("[CalendarHandler] Finished event export with %d errors", errors.count())
                    // showing error report-like
                    var aux :String = ""
                    errors.forEach {
                        aux += "\n• %s".format(it.toShortString())
                    }
                    activity.infoDialog(
                        title = "Error",
                        message = """
                            No se pudo exportar al calendario de nombre ""%s"" los siguientes eventos:\
                            %s\
                            Intente de nuevo con otro calendario.
                        """.multilineTrim().format(calendarName, aux),
                        icon = R.drawable.alert_icon
                    )
                }
                else {
                    Logf("[CalendarHandler] Events successfully exported to calendar named \"%s\"", calendarName)
                    activity.toastf("%d eventos exportados a \"%s\"", evaluations.count(), calendarName)
                }
            }
        )
    }

    private fun ensurePermissions(activity :Activity) {
        Logf("[CalendarHandler] Checking permissions...")
        while (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
        ) {
            this.askPermissions(activity)
            // TODO: add a small wait here, because if user hasn't set permisions yet, it seems to be a busy cycle until they decide to grant the permission
        }
        Logf("[CalendarHandler] Calendar permissios granted.")
    }

    private fun askPermissions(activity :Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
            1
        )
    }

    /**
     * Creates an ICS file (AKA iCalendar file) which holds each `RamoEvent` of user inscribed `Ramo`s,
     * as calendar events. This file could later be used on any device (e.g. PC) to import the events there.
     */
    public fun exportAsICSFile(context :Context) {
        val icsFile : File

        /* creating and saving temporal ICS (iCalendar) file */
        val saveFolder :String = "Download"
        val fileContent :String = this.buildIcsContent(DataMaster.getUserRamos())

        fun streamWriteFile(content :String, stream :OutputStream) {
            try {
                stream.write(content.toByteArray())
                stream.close()
                Logf("[CalendarHandler] ICS file successfully exported inside folder '%s'.", saveFolder)
            }
            catch (e :IOException) {
                Logf("[CalendarHandler] Failed to export ICS file. %s", e)
            }
        }

        val fileMetadata :ContentValues = ContentValues() // TODO: make sure nothing breaks for using metadata intented for image files
        fileMetadata.put(MediaStore.Images.Media.MIME_TYPE, "text/calendar")
        fileMetadata.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            fileMetadata.put(MediaStore.Images.Media.RELATIVE_PATH, saveFolder)
            fileMetadata.put(MediaStore.Images.Media.IS_PENDING, true)

            val uri :Uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, fileMetadata)!!
            icsFile = File(uri.path!!)
            streamWriteFile(content=fileContent, stream=context.contentResolver.openOutputStream(uri)!!)
            fileMetadata.put(MediaStore.Images.Media.IS_PENDING, false)
            context.contentResolver.update(uri, fileMetadata, null, null)
        }
        else {
            val directory = File( "%s/%s".format(Environment.getExternalStorageDirectory().toString(), saveFolder) )
            if (! directory.exists()) { directory.mkdirs() }

            val fileName :String = "%s.ics".format(System.currentTimeMillis().toString())
            icsFile = File(directory, fileName)
            streamWriteFile(content=fileContent, stream=FileOutputStream(icsFile))
            fileMetadata.put(MediaStore.Images.Media.DATA, icsFile.absolutePath)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileMetadata)
        }
        Logf("[CalendarHandler] iCalendar/ICS file created at %s", icsFile.path)
    }

    /**
     * Generates the ICS file contents and returns it.
     */
    private fun buildIcsContent(ramos :List<Ramo>) : String {

        // TODO: make sure this function generates a 100% valid iCalendar file

        val fileHeader :String = """BEGIN:VCALENDAR
PRODID:-//Google Inc//Google Calendar 70.9054//EN
VERSION:2.0
CALSCALE:GREGORIAN
METHOD:PUBLISH
X-WR-CALNAME:CalendarioEvaluaciones
X-WR-TIMEZONE:America/Santiago"""
        var fileBody :String = ""
        val fileFooter :String = "END:VCALENDAR"

        var _year      :String
        var _month     :String
        var _day       :String
        var _startTime :String
        var _endTime   :String

        /* processing evaluations... */
        ramos.forEach { ramo :Ramo ->
            DataMaster.getEventsOfRamo(ramo=ramo, searchInUserList=true).forEach { event :RamoEvent ->
                if (event.isEvaluation()) {
                    _year = event.date!!.year.toString()
                    _month = event.date!!.month.toString()
                    _day = event.date!!.dayOfMonth.toString()
                    _startTime = event.startTime.toString().replace(":", "")
                    _endTime = event.endTime.toString().replace(":", "")

                    fileBody += """
BEGIN:VEVENT
DTSTART;TZID=America/Santiago:${_year}${_month}${_day}T${_startTime}00
DTEND;TZID=America/Santiago:${_year}${_month}${_day}T${_endTime}00
DESCRIPTION:[${ramo.materia} ${ramo.NRC}] ${ramo.nombre}
STATUS:CONFIRMED
SUMMARY:${SpanishToStringOf.ramoEventType(eventType=event.type, shorten=false)} ${ramo.nombre} (${event.startTime})
END:VEVENT
"""
                }
            }
        }

        return "%s%s%s".format(fileHeader, fileBody, fileFooter)
    }
}