package com.icetea09.vivid.common;

import com.icetea09.vivid.model.Folder;

import java.util.List;

public interface ImageLoaderListener {
    void onImageLoaded(List<Folder> folders);

    void onFailed(Throwable throwable);
}
