package com.icetea09.vivid.helper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Environment
import android.util.Log

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImagePickerUtils {

    private val TAG = "ImageUtils"

    fun createImageFile(directory: String): File? {
        // External sdcard location
        val mediaStorageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), directory)

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create $directory directory")
                return null
            }
        }

        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp

        var imageFile: File? = null
        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", mediaStorageDir)
        } catch (e: IOException) {
            Log.d(TAG, "Oops! Failed create $imageFileName file")
        }

        return imageFile
    }

    fun getNameFromFilePath(path: String): String {
        if (path.contains(File.separator)) {
            return path.substring(path.lastIndexOf(File.separator) + 1)
        }
        return path
    }

    fun grantAppPermission(context: Context, intent: Intent, fileUri: Uri) {
        val resolvedIntentActivities = context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        resolvedIntentActivities
                .map { it.activityInfo.packageName }
                .forEach {
                    context.grantUriPermission(it, fileUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
    }

    fun revokeAppPermission(context: Context, fileUri: Uri) {
        context.revokeUriPermission(fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}
