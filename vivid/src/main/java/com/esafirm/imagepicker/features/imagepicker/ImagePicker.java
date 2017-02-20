package com.esafirm.imagepicker.features.imagepicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;

import com.esafirm.imagepicker.model.Image;

import java.lang.annotation.Retention;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public abstract class ImagePicker {

    public static final String EXTRA_SELECTED_IMAGES = "selectedImages";
    public static final String EXTRA_LIMIT = "limit";
    public static final String EXTRA_SHOW_CAMERA = "showCamera";
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_FOLDER_MODE = "folderMode";
    public static final String EXTRA_FOLDER_TITLE = "folderTitle";
    public static final String EXTRA_IMAGE_TITLE = "imageTitle";
    public static final String EXTRA_IMAGE_DIRECTORY = "capturedImageDirectory";
    public static final String EXTRA_RETURN_AFTER_FIRST = "returnAfterFirst";

    public static final int MAX_LIMIT = 99;

    @Retention(SOURCE)
    @IntDef({SINGLE, MULTIPLE})
    public @interface ImagePickerMode {
    }

    public static final int SINGLE = 1;
    public static final int MULTIPLE = 2;

    private Configuration config;

    public abstract void start(int requestCode);

    public static ImagePickerWithActivity create(Activity activity) {
        return new ImagePickerWithActivity(activity);
    }

    public static ImagePickerWithFragment create(Fragment fragment) {
        return new ImagePickerWithFragment(fragment);
    }

    void init(Context context) {
        config = new Configuration(context);
    }

    /**
     * Set the image picker mode
     *
     * @param mode must be ImagePicker.SINGLE or ImagePicker.MULTIPLE
     * @return ImagePicker object
     */
    public ImagePicker mode(@ImagePickerMode int mode) {
        config.setMode(mode);
        return this;
    }

    /**
     * Set whether pick action or camera action should return immediate result or not.
     * Only works in single mode for image picker
     *
     * @param returnAfterFirst true if you want to return result immediately after selection
     * @return ImagePicker object
     */
    public ImagePicker returnAfterFirst(boolean returnAfterFirst) {
        config.setReturnAfterFirst(returnAfterFirst);
        return this;
    }

    /**
     * Set maximum number of images user can select
     *
     * @param count max images
     * @return ImagePicker object
     */
    public ImagePicker limit(int count) {
        config.setLimit(count);
        return this;
    }

    /**
     * Set the location to store captured images
     *
     * @param directory name of the directory
     * @return ImagePicker object
     */
    public ImagePicker capturedImageDirectory(String directory) {
        config.setCapturedImageDirectory(directory);
        return this;
    }

    public Intent getIntent(Context context) {
        Intent intent = new Intent(context, ImagePickerActivity.class);
        intent.putExtra(Configuration.class.getSimpleName(), config);
        return intent;
    }

    public static List<Image> getImages(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getParcelableArrayListExtra(ImagePicker.EXTRA_SELECTED_IMAGES);
    }
}
