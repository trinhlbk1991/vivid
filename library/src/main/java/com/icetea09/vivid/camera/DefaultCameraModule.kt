package com.icetea09.vivid.camera

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Log

import com.icetea09.vivid.helper.ImagePickerUtils
import com.icetea09.vivid.imagepicker.Configuration
import com.icetea09.vivid.model.ImageFactory

import java.io.File
import java.io.Serializable
import java.util.Locale

class DefaultCameraModule : CameraModule, Serializable {

    protected var currentImagePath: String? = null

    fun getCameraIntent(context: Context): Intent? {
        return getCameraIntent(context, Configuration(context))
    }

    override fun getCameraIntent(context: Context, config: Configuration): Intent? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFile = config.capturedImageDirectory?.let { ImagePickerUtils.createImageFile(config.capturedImageDirectory as String) }
        if (imageFile != null) {
            val appContext = context.applicationContext
            val providerName = String.format(Locale.ENGLISH, "%s%s", appContext.packageName, ".imagepicker.provider")
            val uri = FileProvider.getUriForFile(appContext, providerName, imageFile)
            currentImagePath = "file:" + imageFile.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

            ImagePickerUtils.grantAppPermission(context, intent, uri)

            return intent
        }
        return null
    }

    override fun getImage(context: Context, intent: Intent?, imageReadyListener: OnImageReadyListener?) {
        if (imageReadyListener == null) {
            throw IllegalStateException("OnImageReadyListener must not be null")
        }

        if (currentImagePath == null) {
            imageReadyListener.onImageReady(emptyList())
            return
        }

        val imageUri = Uri.parse(currentImagePath)
        if (imageUri != null) {
            MediaScannerConnection.scanFile(context.applicationContext, arrayOf(imageUri.path), null)
            { path, uri ->
                var pathClone = path
                Log.v("ImagePicker", "File $pathClone was scanned successfully: $uri")

                if (pathClone == null) {
                    pathClone = currentImagePath
                }
                imageReadyListener.onImageReady(ImageFactory.singleListFromPath(pathClone))
                ImagePickerUtils.revokeAppPermission(context, imageUri)
            }
        }
    }

}
