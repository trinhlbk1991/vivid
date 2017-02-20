package com.icetea09.vivid.features.imagepicker;

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
        fragment.startActivityForResult(ImagePickerActivity.newIntent(fragment.getContext(), config),
                requestCode);
    }
}