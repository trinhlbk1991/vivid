package com.icetea09.vivid.features.common;

import com.icetea09.vivid.model.Folder;
import com.icetea09.vivid.model.Image;

import java.util.List;

public interface ImageLoaderListener {
    void onImageLoaded(List<Image> images, List<Folder> folders);
    void onFailed(Throwable throwable);
}
