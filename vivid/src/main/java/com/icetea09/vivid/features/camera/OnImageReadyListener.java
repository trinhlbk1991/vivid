package com.icetea09.vivid.features.camera;

import com.icetea09.vivid.model.Image;

import java.util.List;

public interface OnImageReadyListener {
    void onImageReady(List<Image> image);
}
