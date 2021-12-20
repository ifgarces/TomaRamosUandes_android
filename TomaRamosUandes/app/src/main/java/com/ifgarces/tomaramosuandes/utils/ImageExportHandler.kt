package com.ifgarces.tomaramosuandes.utils

import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


/**
 * Contains methods to export the schedule view (i.e. an UI element) as an image to external storage.
 * @property IMG_SAVE_FOLDER Folder name inside "Pictures" (the standard folder for pics in Android).
 */
object ImageExportHandler {
    private const val IMG_SAVE_FOLDER :String = "Pictures/Horario"

    /**
     * Exports the week schedule as a PNG image, at path "Pictures/`this.saveFolder`"
     * @param activity Needs the calling activity for several methods.
     * @param targetView The view that has all the elements that are wanted to be captured,
     * including itself (ScrollView).
     * @param tallView The view inside the `targetView`, which height could be larger (taller)
     * than the device's display height (LinearLayout).
     */
    public fun exportWeekScheduleImage(activity :Activity, targetView :View, tallView :View) {
        val capture :Bitmap = this.getBitmapOf(targetView, tallView)
        this.saveImage(capture, activity)
    }

    private fun getBitmapOf(
        targetView :View,
        tallView :View
    ) :Bitmap { // references: http://hackerseve.com/android-save-view-as-image-and-share-externally/
        val bitmap :Bitmap = Bitmap.createBitmap( //! <- see issue #16
            targetView.width, tallView.height, Bitmap.Config.ARGB_8888
        )

        // Handling background and layers (...)
        val canvas :Canvas = Canvas(bitmap)
        val baground :Drawable? = targetView.background
        if (baground != null) {
            baground.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        targetView.draw(canvas)
        return bitmap
    }

    private fun saveImage(
        bitmap :Bitmap,
        activity :Activity
    ) { // references: https://stackoverflow.com/a/57265702
//        this.checkPermissions(activity)

        val fileMetadata :ContentValues = ContentValues()
        fileMetadata.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        fileMetadata.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            fileMetadata.put(MediaStore.Images.Media.RELATIVE_PATH, this.IMG_SAVE_FOLDER)
            fileMetadata.put(MediaStore.Images.Media.IS_PENDING, true)
            fileMetadata.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

            val uri :Uri = activity.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                fileMetadata
            )!!
            this.saveImageToStream(
                img = bitmap,
                stream = activity.contentResolver.openOutputStream(uri)!!,
                activity = activity
            )
            fileMetadata.put(MediaStore.Images.Media.IS_PENDING, false)
            activity.contentResolver.update(uri, fileMetadata, null, null)
        } else {
            val directory = File(
                "%s/%s".format(
                    Environment.getExternalStorageDirectory().toString(),
                    this.IMG_SAVE_FOLDER
                )
            )
//            val directory :File =
//                if (Build.VERSION.SDK_INT >= 30) { // references: https://stackoverflow.com/a/66317287/12684271
//                    File("%s/%s".format(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), this.IMG_SAVE_FOLDER))
//                } else {
//                    File("%s/%s".format(Environment.getExternalStorageDirectory(), this.IMG_SAVE_FOLDER))
//                }
            if (!directory.exists()) directory.mkdirs() // creating parent directories, if needed

            // Declaring file
            val fileName :String = "%s.png".format(System.currentTimeMillis().toString())
            val file = File(directory, fileName)

            // Actually saving the image as a PNG file
            val fStream :FileOutputStream = FileOutputStream(file)
            this.saveImageToStream(
                img = bitmap,
                stream = fStream,
                activity = activity
            )
            fileMetadata.put(MediaStore.Images.Media.DATA, file.absolutePath)
            activity.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                fileMetadata
            )
        }
    }

    /**
     * Attempts to save the Bitmap as a PNG image on the device's storage.
     * References: https://stackoverflow.com/a/57265702
     * @exception Exception Raised when on compress/stream error (most likely to be an
     * `IOException`).
     */
    private fun saveImageToStream(
        img :Bitmap,
        stream :OutputStream,
        activity :Activity
    ) {
        try {
            img.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.close()
            Logf.debug(this::class, "Image successfully saved at %s.", this.IMG_SAVE_FOLDER)
            activity.toastf(
                "Imagen guardada en carpeta %s.\nRevise su galería.",
                this.IMG_SAVE_FOLDER
            )
        } catch (e :Exception) { // <==> IOException ??
            Logf.error(this::class, "Error when attempting to save the image in storage: %s", e)
            throw e // this will be catched outside
        }
    }

//    /**
//     * Makes sure the app has permission to create a new file (image) in the device's storage.
//     */
//    private fun ensurePermissions(activity :Activity) {
//        // TODO: fix this not working properly. Permission dialog-thing is never shown. Is this unnecessary???
//        Logf.debug(this::class, "Checking permissions...")
//        while (ActivityCompat.checkSelfPermission(
//                activity,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            this.askPermissions(activity)
//        }
//        Logf.debug(this::class, "Storage permissions granted.")
//    }

//    private fun askPermissions(activity :Activity) {
//        ActivityCompat.requestPermissions(
//            activity,
//            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//            1 // TODO: make sure this is the right expected request code for this permission
//        )
//    }
}