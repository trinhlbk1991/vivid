package com.icetea09.vivid.imagepicker;

import android.app.Activity;

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
        activity.startActivityForResult(ImagePickerActivity.newIntent(activity, config), requestCode);
    }
}
