package com.ifgarces.tomaramosuandes.utils

import android.content.ContentValues
import android.content.Context
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


object ImageWorker {
    private const val saveFolder  :String = "Pictures/Horario" // folder name inside "Pictures" standard folder
    //private const val agendaWidth :Float = 2000f // agenda with (in dp) when it is exported as picture (so resulting image should be equivalent in any device)

    /**
     * Exports the agenda as a PNG image, at path "Pictures/`this.saveFolder`"
     * @param context Needs a context
     * @param targetView The view that has all the elements that are wanted to be captured,
     * including itself. It should be a ScrollView, if there exists.
     * @param tallView The view inside the `targetView`, which height could be larger
     * than the device's display height.
     */
    public fun exportAgendaImage(context :Context, targetView :View, tallView :View) {

        // TODO: enlarge targetView width (height will be according to context, do not touch it)

        val capture :Bitmap = this.getBitmapOf(targetView, tallView)
        this.saveImage(capture, context)
    }

    private fun getBitmapOf(targetView :View, largerView :View) : Bitmap { // references: http://hackerseve.com/android-save-view-as-image-and-share-externally/
        val bitmap :Bitmap = Bitmap.createBitmap(
            targetView.width, largerView.height, Bitmap.Config.ARGB_8888
        )

        // handling background and layers (...)
        val canvas :Canvas = Canvas(bitmap)
        val baground :Drawable? = targetView.background
        if (baground != null) {
            baground.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        targetView.draw(canvas) // [?] Should I undo this change after?
        return bitmap
    }

    private fun saveImage(bitmap :Bitmap, context :Context) { // references: https://stackoverflow.com/a/57265702
        val fileMetadata :ContentValues = ContentValues()
        fileMetadata.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        fileMetadata.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            fileMetadata.put(MediaStore.Images.Media.RELATIVE_PATH, this.saveFolder)
            fileMetadata.put(MediaStore.Images.Media.IS_PENDING, true)
            fileMetadata.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

            val uri :Uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileMetadata)!!
            this.saveImageToStream(img=bitmap, stream=context.contentResolver.openOutputStream(uri)!!, context=context)
            fileMetadata.put(MediaStore.Images.Media.IS_PENDING, false)
            context.contentResolver.update(uri, fileMetadata, null, null)
        }
        else {
            val directory = File( "%s/%s".format(Environment.getExternalStorageDirectory().toString(), this.saveFolder) )
            if (! directory.exists()) { directory.mkdirs() }

            val fileName :String = "%s.png".format(System.currentTimeMillis().toString())
            val file = File(directory, fileName)
            this.saveImageToStream(img=bitmap, stream=FileOutputStream(file), context=context)
            fileMetadata.put(MediaStore.Images.Media.DATA, file.absolutePath)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, fileMetadata)
        }
    }

    private fun saveImageToStream(img :Bitmap, stream :OutputStream, context :Context) { // references: https://stackoverflow.com/a/57265702
        try {
            img.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            Logf("[ImageWorker] Image successfully saved at %s.", this.saveFolder)
            context.toastf("Imagen guardada en carpeta %s.\nRevise su galería.", this.saveFolder)
        }
        catch (e :Exception) { // [?] <==> IOException?
            Logf("[ImageWorker] Error: could not save agenda as image. %s", e)
            context.infoDialog(
                title = "Error",
                message = "Ocurrió un error al guardar el horario como imagen. Intente nuevamente."
            )
        }
    }
}