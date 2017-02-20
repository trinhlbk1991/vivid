package com.icetea09.vivid.features.camera;

import android.content.Context;
import android.content.Intent;

import com.icetea09.vivid.features.imagepicker.Configuration;

public interface CameraModule {
    Intent getCameraIntent(Context context, Configuration config);

    void getImage(Context context, Intent intent, OnImageReadyListener imageReadyListener);
}
