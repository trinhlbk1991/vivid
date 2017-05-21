package com.icetea09.vivid.imagepicker

import android.support.v4.app.Fragment

/**
 * Created by trinhlbk on 2/19/17.
 */

class ImagePickerWithFragment internal constructor(private val fragment: Fragment) : ImagePicker() {

    init {
        init(fragment.activity)
    }

    override fun start(requestCode: Int) {
        fragment.startActivityForResult(ImagePickerActivity.newIntent(fragment.context, config),
                requestCode)
    }
}