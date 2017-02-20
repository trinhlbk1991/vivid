package com.icetea09.vivid.features.imagepicker;

import com.icetea09.vivid.features.common.MvpView;
import com.icetea09.vivid.model.Folder;
import com.icetea09.vivid.model.Image;

import java.util.List;

public interface ImagePickerView extends MvpView {
    void showLoading(boolean isLoading);
    void showFetchCompleted(List<Image> images, List<Folder> folders);
    void showError(Throwable throwable);
    void showEmpty();
    void showCapturedImage();
    void finishPickImages(List<Image> images);
}
