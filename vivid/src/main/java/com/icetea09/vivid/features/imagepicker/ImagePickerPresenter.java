package com.icetea09.vivid.features.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.icetea09.vivid.R;
import com.icetea09.vivid.features.ImageLoader;
import com.icetea09.vivid.features.camera.CameraModule;
import com.icetea09.vivid.features.camera.DefaultCameraModule;
import com.icetea09.vivid.features.camera.OnImageReadyListener;
import com.icetea09.vivid.features.common.BasePresenter;
import com.icetea09.vivid.features.common.ImageLoaderListener;
import com.icetea09.vivid.model.Folder;
import com.icetea09.vivid.model.Image;

import java.io.File;
import java.util.List;

public class ImagePickerPresenter extends BasePresenter<ImagePickerView> {

    private ImageLoader imageLoader;
    private CameraModule cameraModule = new DefaultCameraModule();
    private Handler handler = new Handler(Looper.getMainLooper());

    public ImagePickerPresenter(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public void abortLoad() {
        imageLoader.abortLoadImages();
    }

    public void loadImages(boolean isFolderMode) {
        if (!isViewAttached()) return;

        getView().showLoading(true);
        imageLoader.loadDeviceImages(isFolderMode, new ImageLoaderListener() {
            @Override
            public void onImageLoaded(final List<Image> images, final List<Folder> folders) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isViewAttached()) {
                            getView().showFetchCompleted(images, folders);

                            if (folders != null) {
                                if (folders.isEmpty()) {
                                    getView().showEmpty();
                                } else {
                                    getView().showLoading(false);
                                }
                            } else {
                                if (images.isEmpty()) {
                                    getView().showEmpty();
                                } else {
                                    getView().showLoading(false);
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailed(final Throwable throwable) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isViewAttached()) {
                            getView().showError(throwable);
                        }
                    }
                });
            }
        });
    }

    public void onDoneSelectImages(List<Image> selectedImages) {
        if (selectedImages != null && selectedImages.size() > 0) {

            /** Scan selected images which not existed */
            for (int i = 0; i < selectedImages.size(); i++) {
                Image image = selectedImages.get(i);
                File file = new File(image.getPath());
                if (!file.exists()) {
                    selectedImages.remove(i);
                    i--;
                }
            }
            getView().finishPickImages(selectedImages);
        }
    }

    public void captureImage(Activity activity, Configuration config, int requestCode) {
        Context context = activity.getApplicationContext();
        Intent intent = cameraModule.getCameraIntent(activity, config);
        if (intent == null) {
            Toast.makeText(context, context.getString(R.string.ef_error_create_image_file), Toast.LENGTH_LONG).show();
            return;
        }
        activity.startActivityForResult(intent, requestCode);
    }

    public void finishCaptureImage(Context context, Intent data, final Configuration config) {
        cameraModule.getImage(context, data, new OnImageReadyListener() {
            @Override
            public void onImageReady(List<Image> images) {
                if (config.isReturnAfterFirst()) {
                    getView().finishPickImages(images);
                } else {
                    getView().showCapturedImage();
                }
            }
        });
    }
}
