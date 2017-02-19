package com.esafirm.imagepicker.features.imagepicker;

import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by trinhlbk on 2/19/17.
 */

public class ImagePickerWithFragment extends ImagePicker {

    private Fragment fragment;

    ImagePickerWithFragment(Fragment fragment) {
        this.fragment = fragment;
        init(fragment.getActivity());
    }

    @Override
    public void start(int requestCode) {
        Intent intent = getIntent(fragment.getActivity());
        fragment.startActivityForResult(intent, requestCode);
    }
}