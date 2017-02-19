package com.esafirm.imagepicker.features.imagepicker;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by trinhlbk on 2/19/17.
 */

public class ImagePickerWithActivity extends ImagePicker {

    private Activity activity;

    ImagePickerWithActivity(Activity activity) {
        this.activity = activity;
        init(activity);
    }

    @Override
    public void start(int requestCode) {
        Intent intent = getIntent(activity);
        activity.startActivityForResult(intent, requestCode);
    }
}
