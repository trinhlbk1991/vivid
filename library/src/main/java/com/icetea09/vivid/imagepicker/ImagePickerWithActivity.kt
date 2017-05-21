package com.icetea09.vivid.imagepicker

import android.app.Activity

/**
 * Created by trinhlbk on 2/19/17.
 */

class ImagePickerWithActivity internal constructor(private val activity: Activity) : ImagePicker() {

    init {
        init(activity)
    }

    override fun start(requestCode: Int) {
        activity.startActivityForResult(ImagePickerActivity.newIntent(activity, config), requestCode)
    }
}
