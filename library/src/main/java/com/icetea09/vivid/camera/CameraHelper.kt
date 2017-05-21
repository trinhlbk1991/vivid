package com.icetea09.vivid.camera

import android.content.Context
import android.content.Intent
import android.provider.MediaStore

object CameraHelper {

    fun checkCameraAvailability(context: Context): Boolean {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return intent.resolveActivity(context.packageManager) != null
    }

}
