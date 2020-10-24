package com.ifgarces.tomaramosuandes.utils

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.CalendarContract.Events
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
import java.util.TimeZone


/**
 * Worries about exporting the `RamoEvent` as actual events to the user's default calendar.
 */
object CalendarHandler {

    private const val TARGET_CALENDAR_ID :Int = 0 // TODO: make sure this calendar ID will always work

    /**
     * Exports all of the `RamoEvents` for the inscribed `Ramo` collection to the user's default calendar.
     * Each calendar event will have the following attributes:
     *  - Title: the name of the linked `Ramo`.
     *  - Description: the event details given by `RamoEvent.toLargeString()`.
     *  - Start time-date: given by `RamoEvent.startDate` and `RamoEvent.startTime`.
     *  - End time-date: same but with the end.
     */
    fun exportEventsToCalendar(activity :Activity) {
        val evaluations :List<RamoEvent> = DataMaster.getUserEvaluations()

        this.checkPermissions(activity)

        Logf("[CalendarHandler] This is temporal. User ramos:\n%s", DataMaster.getUserRamos())

        Logf("[CalendarHandler] Starting to export %d events...", evaluations.count())
        var e :ContentValues
        var result :Uri? // will hold the got internal feedback for each event insertion in calendar
        var zoneAux :ZonedDateTime
        val baseUri :Uri = Uri.parse("content://com.android.calendar/events")
        val errors :MutableList<RamoEvent> = mutableListOf() // will contain events that somehow couldn't be exported

        evaluations.forEach { event :RamoEvent ->
            Logf("[CalendarHandler] Exporting %s", event)
            e = ContentValues()
            e.put(Events.CALENDAR_ID, this.TARGET_CALENDAR_ID)
            e.put(Events.TITLE, DataMaster.findRamo(NRC=event.ramoNRC, searchInUserList=true)!!.nombre)
            e.put(Events.DESCRIPTION, event.toLargeString().replace("\n", ";"))

            zoneAux = LocalDateTime.of(event.date!!, event.startTime) // we are exporting evaluations, whose date are always assigned, do not worry about NullPointerException
                .atZone(ZoneId.of("America/Santiago"))
            e.put(Events.DTSTART, zoneAux.toInstant().toEpochMilli())
            zoneAux = LocalDateTime.of(event.date!!, event.endTime)
                .atZone(ZoneId.of("America/Santiago"))
            e.put(Events.DTEND, zoneAux.toInstant().toEpochMilli())

            e.put(Events.ALL_DAY, 0) // 0: false, 1: true
            e.put(Events.HAS_ALARM, 0)
            e.put(Events.EVENT_TIMEZONE, TimeZone.getDefault().id)

            result = activity.contentResolver.insert(baseUri, e)
            if (result == null) {
                Logf("[CalendarHandler] Error: got null URI response when inserting the event at the calendar with ID=%d", this.TARGET_CALENDAR_ID)
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
                message = "No se pudo exportar al calendario los siguientes eventos:\n%s".format(aux),
                icon = R.drawable.alert_icon
            )
        }
        else {
            Logf("[CalendarHandler] Events successfully exported!")
        }
    }

    private fun checkPermissions(activity :Activity) {
        Logf("[CalendarHandler] Checking permissions...")
        while ( ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED ) {
            this.askPermissions(activity)
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