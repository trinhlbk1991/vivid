package com.icetea09.vivid.features.imagepicker;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.icetea09.vivid.R;
import com.icetea09.vivid.model.Image;

import java.util.ArrayList;

import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_FOLDER_TITLE;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_IMAGE_DIRECTORY;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_IMAGE_TITLE;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_LIMIT;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_MODE;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_RETURN_AFTER_FIRST;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.EXTRA_SELECTED_IMAGES;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.MAX_LIMIT;
import static com.icetea09.vivid.features.imagepicker.ImagePicker.MULTIPLE;

public class Configuration implements Parcelable {

    private String folderTitle;
    private String imageTitle;
    private String capturedImageDirectory;
    private int mode;
    private int limit;
    private boolean returnAfterFirst;

    public static Configuration create(Context context, Intent intent) {
        Configuration config = new Configuration(context);
        config.setMode(intent.getIntExtra(EXTRA_MODE, MULTIPLE));
        config.setLimit(intent.getIntExtra(EXTRA_LIMIT, MAX_LIMIT));
        config.setFolderTitle(intent.getStringExtra(EXTRA_FOLDER_TITLE));
        config.setImageTitle(intent.getStringExtra(EXTRA_IMAGE_TITLE));
        config.setCapturedImageDirectory(intent.getStringExtra(EXTRA_IMAGE_DIRECTORY));
        config.setReturnAfterFirst(intent.getBooleanExtra(EXTRA_RETURN_AFTER_FIRST, false));
        return config;
    }

    public Configuration(Context context) {
        this.mode = ImagePicker.MULTIPLE;
        this.limit = ImagePicker.MAX_LIMIT;
        this.folderTitle = context.getString(R.string.ef_title_folder);
        this.imageTitle = context.getString(R.string.ef_title_select_image);
        this.capturedImageDirectory = context.getString(R.string.ef_image_directory);
        this.returnAfterFirst = true;
    }

    public boolean isReturnAfterFirst() {
        return returnAfterFirst;
    }

    public void setReturnAfterFirst(boolean returnAfterFirst) {
        this.returnAfterFirst = returnAfterFirst;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getFolderTitle() {
        return folderTitle;
    }

    public void setFolderTitle(String folderTitle) {
        this.folderTitle = folderTitle;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public String getCapturedImageDirectory() {
        return capturedImageDirectory;
    }

    public void setCapturedImageDirectory(String capturedImageDirectory) {
        this.capturedImageDirectory = capturedImageDirectory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.folderTitle);
        dest.writeString(this.imageTitle);
        dest.writeString(this.capturedImageDirectory);
        dest.writeInt(this.mode);
        dest.writeInt(this.limit);
        dest.writeByte(this.returnAfterFirst ? (byte) 1 : (byte) 0);
    }

    protected Configuration(Parcel in) {
        this.folderTitle = in.readString();
        this.imageTitle = in.readString();
        this.capturedImageDirectory = in.readString();
        this.mode = in.readInt();
        this.limit = in.readInt();
        this.returnAfterFirst = in.readByte() != 0;
    }

    public static final Creator<Configuration> CREATOR = new Creator<Configuration>() {
        @Override
        public Configuration createFromParcel(Parcel source) {
            return new Configuration(source);
        }

        @Override
        public Configuration[] newArray(int size) {
            return new Configuration[size];
        }
    };
}
