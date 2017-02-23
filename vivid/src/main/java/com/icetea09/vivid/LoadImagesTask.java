package com.icetea09.vivid;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.icetea09.vivid.common.ImageLoaderListener;
import com.icetea09.vivid.model.Folder;
import com.icetea09.vivid.model.Image;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoadImagesTask extends AsyncTask<Void, Integer, List<Folder>> {

    private final String[] projection = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
    };

    WeakReference<Context> contextWeakReference;
    ImageLoaderListener listener;

    public LoadImagesTask(Context context, ImageLoaderListener listener) {
        this.contextWeakReference = new WeakReference<>(context);
        this.listener = listener;
    }

    @Override
    protected List<Folder> doInBackground(Void... params) {
        Context context = contextWeakReference.get();
        if (context == null) {
            return null;
        }

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, MediaStore.Images.Media.DATE_ADDED);
        if (cursor == null) {
            return null;
        }

        Map<String, Folder> folderMap = new HashMap<>();
        if (cursor.moveToLast()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                String bucketDisplayName = cursor.getString(cursor.getColumnIndex(projection[3]));

                File file = new File(path);
                if (file.exists()) {
                    Image image = new Image(id, name, path, false);
                    Folder folder = folderMap.get(bucketDisplayName);
                    if (folder == null) {
                        folder = new Folder(bucketDisplayName);
                        folderMap.put(bucketDisplayName, folder);
                    }
                    folder.getImages().add(image);
                }

            } while (cursor.moveToPrevious());
        }
        cursor.close();
        return new ArrayList<>(folderMap.values());
    }


    @Override
    protected void onPostExecute(List<Folder> folders) {
        if (listener != null) {
            listener.onImageLoaded(folders);
        }
    }

}
