package com.icetea09.vivid.camera;

import android.content.Context;
import android.content.Intent;

import com.icetea09.vivid.imagepicker.Configuration;

public interface CameraModule {
    Intent getCameraIntent(Context context, Configuration config);

    void getImage(Context context, Intent intent, OnImageReadyListener imageReadyListener);
}
