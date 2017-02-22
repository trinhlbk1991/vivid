package com.icetea09.vivid.features.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
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
import java.util.ArrayList;
import java.util.List;

import static com.icetea09.vivid.features.imagepicker.ImagePicker.SINGLE;

public class ImagePickerPresenter extends BasePresenter<ImagePickerActivity> {

    private ImageLoader imageLoader;
    private CameraModule cameraModule = new DefaultCameraModule();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Configuration configuration;
    private List<Image> selectedImages;

    public ImagePickerPresenter(ImageLoader imageLoader, Configuration configuration) {
        this.imageLoader = imageLoader;
        this.configuration = configuration;
        this.selectedImages = new ArrayList<>();
    }

    @Override
    public void attachView(ImagePickerActivity view) {
        super.attachView(view);
        view.setUpView(configuration.getDefaultToolbarTitle());
    }

    public void abortLoad() {
        imageLoader.abortLoadImages();
    }

    public void loadImages(boolean isFolderMode) {
        if (!isViewAttached()) {
            return;
        }

        view.showLoading(true);
        imageLoader.loadDeviceImages(isFolderMode, new ImageLoaderListener() {
            @Override
            public void onImageLoaded(final List<Image> images, final List<Folder> folders) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isViewAttached()) {
                            view.showFetchCompleted(folders);

                            if (folders != null) {
                                if (folders.isEmpty()) {
                                    view.showEmpty();
                                } else {
                                    view.showLoading(false);
                                }
                            } else {
                                if (images.isEmpty()) {
                                    view.showEmpty();
                                } else {
                                    view.showLoading(false);
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
                            view.showError(throwable);
                        }
                    }
                });
            }
        });
    }

    public void onDoneSelectImages() {
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
            view.finishPickImages(selectedImages);
        }
    }

    public void captureImage(Activity activity, int requestCode) {
        Context context = activity.getApplicationContext();
        Intent intent = cameraModule.getCameraIntent(activity, configuration);
        if (intent == null) {
            Toast.makeText(context, context.getString(R.string.ef_error_create_image_file), Toast.LENGTH_LONG).show();
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
                view.removeImage(selectedItemPosition, clickPosition);
                updateTitle();
            }
        } else {
            if (selectedItemPosition != -1) {
                view.removeImage(selectedItemPosition, clickPosition);
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
