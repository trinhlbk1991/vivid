package com.esafirm.imagepicker.helper;

import android.content.Context;
import android.content.Intent;

import com.esafirm.imagepicker.features.imagepicker.ImagePickerConfig;
import com.esafirm.imagepicker.model.Image;

import static com.esafirm.imagepicker.features.imagepicker.ImagePicker.EXTRA_FOLDER_MODE;
import static com.esafirm.imagepicker.features.imagepicker.ImagePicker.EXTRA_FOLDER_TITLE;
import static com.esafirm.imagepicker.features.imagepicker.ImagePicker.EXTRA_IMAGE_DIRECTORY;
import static com.esafirm.imagepicker.features.imagepicker.ImagePicker.EXTRA_IMAGE_TITLE;
import static com.esafirm.imagepicker.features.imagepicker.ImagePicker.EXTRA_LIMIT;
import static com.esafirm.imagepicker.features.imagepicker.ImagePicker.EXTRA_MODE;
import static com.esafirm.imagepicker.features.imagepicker.ImagePicker.EXTRA_RETURN_AFTER_FIRST;
import static com.esafirm.imagepicker.features.imagepicker.ImagePicker.EXTRA_SELECTED_IMAGES;
import static com.esafirm.imagepicker.features.imagepicker.ImagePicker.EXTRA_SHOW_CAMERA;
import static com.esafirm.imagepicker.features.imagepicker.ImagePicker.MAX_LIMIT;
import static com.esafirm.imagepicker.features.imagepicker.ImagePicker.MULTIPLE;

public class IntentHelper {

    public static ImagePickerConfig makeConfigFromIntent(Context context, Intent intent) {
        ImagePickerConfig config = new ImagePickerConfig(context);
        config.setMode(intent.getIntExtra(EXTRA_MODE, MULTIPLE));
        config.setLimit(intent.getIntExtra(EXTRA_LIMIT, MAX_LIMIT));
        config.setShowCamera(intent.getBooleanExtra(EXTRA_SHOW_CAMERA, true));
        config.setFolderTitle(intent.getStringExtra(EXTRA_FOLDER_TITLE));
        config.setImageTitle(intent.getStringExtra(EXTRA_IMAGE_TITLE));
        config.setSelectedImages(intent.<Image>getParcelableArrayListExtra(EXTRA_SELECTED_IMAGES));
        config.setImageDirectory(intent.getStringExtra(EXTRA_IMAGE_DIRECTORY));
        config.setReturnAfterFirst(intent.getBooleanExtra(EXTRA_RETURN_AFTER_FIRST, false));
        return config;
    }
}
