package com.icetea09.vivid.helper;

import android.content.Context;
import android.content.Intent;

import com.icetea09.vivid.features.imagepicker.Configuration;
import com.icetea09.vivid.model.Image;

import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_FOLDER_TITLE;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_IMAGE_DIRECTORY;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_IMAGE_TITLE;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_LIMIT;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_MODE;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_RETURN_AFTER_FIRST;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_SELECTED_IMAGES;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.MAX_LIMIT;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.MULTIPLE;

public class IntentHelper {

    public static Configuration makeConfigFromIntent(Context context, Intent intent) {
        Configuration config = new Configuration(context);
        config.setMode(intent.getIntExtra(EXTRA_MODE, MULTIPLE));
        config.setLimit(intent.getIntExtra(EXTRA_LIMIT, MAX_LIMIT));
        config.setFolderTitle(intent.getStringExtra(EXTRA_FOLDER_TITLE));
        config.setImageTitle(intent.getStringExtra(EXTRA_IMAGE_TITLE));
        config.setSelectedImages(intent.<Image>getParcelableArrayListExtra(EXTRA_SELECTED_IMAGES));
        config.setCapturedImageDirectory(intent.getStringExtra(EXTRA_IMAGE_DIRECTORY));
        config.setReturnAfterFirst(intent.getBooleanExtra(EXTRA_RETURN_AFTER_FIRST, false));
        return config;
    }
}
