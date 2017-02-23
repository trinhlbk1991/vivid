package com.icetea09.vivid.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import com.icetea09.vivid.LoadImagesTask;
import com.icetea09.vivid.R;
import com.icetea09.vivid.camera.CameraModule;
import com.icetea09.vivid.camera.DefaultCameraModule;
import com.icetea09.vivid.camera.OnImageReadyListener;
import com.icetea09.vivid.common.BasePresenter;
import com.icetea09.vivid.common.ImageLoaderListener;
import com.icetea09.vivid.model.Folder;
import com.icetea09.vivid.model.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.icetea09.vivid.imagepicker.ImagePicker.SINGLE;

public class ImagePickerPresenter extends BasePresenter<ImagePickerActivity> implements ImageLoaderListener {

    private LoadImagesTask loadImagesTask;
    private CameraModule cameraModule = new DefaultCameraModule();
    private Configuration configuration;
    private List<Image> selectedImages;

    public ImagePickerPresenter(Configuration configuration) {
        this.configuration = configuration;
        this.selectedImages = new ArrayList<>();
    }

    @Override
    public void attachView(ImagePickerActivity view) {
        super.attachView(view);
        view.setUpView(configuration.getDefaultToolbarTitle());
    }

    @Override
    public void onImageLoaded(List<Folder> folders) {
        if (isViewAttached()) {
            view.showFetchCompleted(folders);
            if (folders != null) {
                if (folders.isEmpty()) {
                    view.showEmpty();
                } else {
                    view.showLoading(false);
                }
            }
        }
        abortLoad();
    }

    @Override
    public void onFailed(Throwable throwable) {
        if (isViewAttached()) {
            view.showError(throwable);
        }
    }

    public void abortLoad() {
        if (loadImagesTask != null) {
            loadImagesTask.cancel(true);
        }
        loadImagesTask = null;
    }

    public void loadImages() {
        if (isViewAttached()) {
            view.showLoading(true);
            loadImagesTask = new LoadImagesTask(view, this);
            loadImagesTask.execute();
        }
    }

    public void onDoneSelectImages() {
        if (selectedImages != null && selectedImages.size() > 0) {
            for (int i = 0; i < selectedImages.size(); i++) {
                Image image = selectedImages.get(i);
                File file = new File(image.getPath());
                if (!file.exists()) {
                    selectedImages.remove(i);
                    i--;
                }
            }
            view.finishPickImages(selectedImages);
        }
    }

    public void captureImage(Activity activity, int requestCode) {
        Context context = activity.getApplicationContext();
        Intent intent = cameraModule.getCameraIntent(activity, configuration);
        if (intent == null) {
            Toast.makeText(context, context.getString(R.string.create_file_failed), Toast.LENGTH_LONG).show();
            return;
        }
        activity.startActivityForResult(intent, requestCode);
    }

    public void finishCaptureImage(Context context, Intent data) {
        cameraModule.getImage(context, data, new OnImageReadyListener() {
            @Override
            public void onImageReady(List<Image> images) {
                if (configuration.isReturnAfterFirst()) {
                    view.finishPickImages(images);
                } else {
                    view.showCapturedImage();
                }
            }
        });
    }

    public void updateMenuDoneVisibility(MenuItem menuDone) {
        if (menuDone != null) {
            menuDone.setVisible(!view.isDisplayingFolderView() && !selectedImages.isEmpty());
            if (configuration.getMode() == SINGLE && configuration.isReturnAfterFirst()) {
                menuDone.setVisible(false);
            }
        }
    }

    public void handleImageClick(int clickPosition, Image image) {
        int selectedItemPosition = selectedImagePosition(image);
        if (configuration.getMode() == ImagePicker.MULTIPLE) {
            if (selectedItemPosition == -1) {
                if (selectedImages.size() < configuration.getLimit()) {
                    selectedImages.add(image);
                    view.updateSelectedImage(image);
                    updateTitle();
                } else {
                    view.showErrorExceedLimit();
                }
            } else {
                selectedImages.remove(selectedItemPosition);
                view.removeImage(image, clickPosition);
                updateTitle();
            }
        } else {
            if (selectedItemPosition != -1) {
                view.removeImage(image, clickPosition);
            } else {
                selectedImages.clear();
                view.removeAllImages();
                selectedImages.add(image);
                view.updateSelectedImage(image);
                updateTitle();

                if (configuration.isReturnAfterFirst()) {
                    view.finishPickImages(selectedImages);
                }
            }
        }
        updateTitle();
    }

    private void updateTitle() {
        view.updateTitle(configuration.getDefaultToolbarTitle(), configuration.getMode(),
                selectedImages.size(), configuration.getLimit());
    }

    private int selectedImagePosition(Image image) {
        for (int i = 0; i < selectedImages.size(); i++) {
            if (selectedImages.get(i).getPath().equals(image.getPath())) {
                return i;
            }
        }
        return -1;
    }

}
