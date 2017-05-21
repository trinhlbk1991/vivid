package com.icetea09.vivid

import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import android.provider.MediaStore

import com.icetea09.vivid.common.ImageLoaderListener
import com.icetea09.vivid.model.Folder
import com.icetea09.vivid.model.Image

import java.io.File
import java.lang.ref.WeakReference
import java.util.ArrayList
import java.util.HashMap


class LoadImagesTask(context: Context, internal var listener: ImageLoaderListener?) : AsyncTask<Void, Int, List<Folder>>() {

    private val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

    internal var contextWeakReference: WeakReference<Context> = WeakReference(context)

    override fun doInBackground(vararg params: Void): List<Folder>? {
        val context = contextWeakReference.get() ?: return null

        val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, MediaStore.Images.Media.DATE_ADDED) ?: return null

        val folderMap = HashMap<String, Folder>()
        if (cursor.moveToLast()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(projection[0]))
                val name = cursor.getString(cursor.getColumnIndex(projection[1]))
                val path = cursor.getString(cursor.getColumnIndex(projection[2]))
                val bucketDisplayName = cursor.getString(cursor.getColumnIndex(projection[3]))

                if (path != null) {
                    val file = File(path)
                    if (file.exists()) {
                        val image = Image(id, name, path, false)
                        var folder: Folder? = folderMap[bucketDisplayName]
                        if (folder == null) {
                            folder = Folder(bucketDisplayName)
                            folderMap.put(bucketDisplayName, folder)
                        }
                        folder.images.add(image)
                    }
                }

            } while (cursor.moveToPrevious())
        }
        cursor.close()
        return ArrayList(folderMap.values)
    }


    override fun onPostExecute(folders: List<Folder>) {
        if (listener != null) {
            listener!!.onImageLoaded(folders)
        }
    }

}
