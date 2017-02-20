package com.icetea09.vivid.features.camera;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;

public class CameraHelper {
    public static boolean checkCameraAvailability(Context context) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        return intent.resolveActivity(context.getPackageManager()) != null;
    }
}
