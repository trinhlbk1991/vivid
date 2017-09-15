package com.icetea09.vivid.imagepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import com.icetea09.vivid.model.Image

abstract class ImagePicker {

    protected var config: Configuration? = null

    abstract fun start(requestCode: Int)

    internal fun init(context: Context) {
        config = Configuration(context)
    }

    /**
     * Set the image picker mode

     * @param mode must be ImagePicker.SINGLE or ImagePicker.MULTIPLE
     * *
     * @return ImagePicker object
     */
    fun mode(mode: Int): ImagePicker {
        config?.mode = mode
        return this
    }

    /**
     * Set whether pick action or camera action should return immediate result or not.
     * Only works in single mode for image picker

     * @param returnAfterFirst true if you want to return result immediately after selection
     * *
     * @return ImagePicker object
     */
    fun returnAfterFirst(returnAfterFirst: Boolean): ImagePicker {
        config?.isReturnAfterFirst = returnAfterFirst
        return this
    }

    /**
     * Set maximum number of images user can select

     * @param count max images
     * *
     * @return ImagePicker object
     */
    fun limit(count: Int): ImagePicker {
        config?.limit = count
        return this
    }

    /**
     * Set the location to store captured images

     * @param directory name of the directory
     * *
     * @return ImagePicker object
     */
    fun capturedImageDirectory(directory: String): ImagePicker {
        config?.capturedImageDirectory = directory
        return this
    }

    /**
     * Set theme for the Vivid image picker

     * @param theme theme resource id
     * *
     * @return ImagePicker object
     */
    fun theme(theme: Int): ImagePicker {
        config?.theme = theme
        return this
    }


    companion object {
        val EXTRA_SELECTED_IMAGES = "selectedImages"
        val EXTRA_LIMIT = "limit"
        val EXTRA_SHOW_CAMERA = "showCamera"
        val EXTRA_MODE = "mode"
        val EXTRA_FOLDER_MODE = "folderMode"
        val EXTRA_FOLDER_TITLE = "folderTitle"
        val EXTRA_IMAGE_TITLE = "imageTitle"
        val EXTRA_IMAGE_DIRECTORY = "capturedImageDirectory"
        val EXTRA_RETURN_AFTER_FIRST = "returnAfterFirst"

        @JvmField
        val SINGLE = 1
        @JvmField
        val MULTIPLE = 2
        @JvmField
        val MAX_LIMIT = 99

        @JvmStatic
        fun create(activity: Activity): ImagePickerWithActivity {
            return ImagePickerWithActivity(activity)
        }

        @JvmStatic
        fun create(fragment: Fragment): ImagePickerWithFragment {
            return ImagePickerWithFragment(fragment)
        }

        @JvmStatic
        fun getImages(intent: Intent?): List<Image>? {
            if (intent == null) {
                return null
            }
            return intent.getParcelableArrayListExtra<Image>(ImagePicker.EXTRA_SELECTED_IMAGES)
        }
    }
}
