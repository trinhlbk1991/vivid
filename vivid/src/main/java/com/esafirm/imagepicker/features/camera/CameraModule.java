package com.esafirm.imagepicker.features.camera;

import android.content.Context;
import android.content.Intent;

import com.esafirm.imagepicker.features.imagepicker.Configuration;

public interface CameraModule {
    Intent getCameraIntent(Context context, Configuration config);

    void getImage(Context context, Intent intent, OnImageReadyListener imageReadyListener);
}
