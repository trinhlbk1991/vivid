package com.icetea09.vivid.camera

import android.content.Context
import android.content.Intent

import com.icetea09.vivid.imagepicker.Configuration

interface CameraModule {
    fun getCameraIntent(context: Context, config: Configuration): Intent?

    fun getImage(context: Context, intent: Intent, imageReadyListener: OnImageReadyListener?)
}
