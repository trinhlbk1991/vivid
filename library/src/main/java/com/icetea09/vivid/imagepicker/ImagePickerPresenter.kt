package com.icetea09.vivid.imagepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.widget.Toast
import com.icetea09.vivid.LoadImagesTask
import com.icetea09.vivid.R
import com.icetea09.vivid.camera.DefaultCameraModule
import com.icetea09.vivid.camera.OnImageReadyListener
import com.icetea09.vivid.common.BasePresenter
import com.icetea09.vivid.common.ImageLoaderListener
import com.icetea09.vivid.imagepicker.ImagePicker.Companion.SINGLE
import com.icetea09.vivid.model.Folder
import com.icetea09.vivid.model.Image
import java.io.File


class ImagePickerPresenter(private val configuration: Configuration) : BasePresenter<ImagePickerActivity>(), ImageLoaderListener {

    private var loadImagesTask: LoadImagesTask? = null
    private val cameraModule = DefaultCameraModule()
    private val selectedImages: MutableList<Image> = mutableListOf()

    override fun attachView(view: ImagePickerActivity) {
        super.attachView(view)
        configuration.defaultToolbarTitle?.let { view.setUpView(it) }
    }

    override fun onImageLoaded(folders: List<Folder>?) {
        folders?.let {
            view?.showFetchCompleted(it)
            if (folders.isEmpty()) {
                view?.showEmpty()
            } else {
                view?.showLoading(false)
            }
        }
        abortLoad()
    }

    override fun onFailed(throwable: Throwable) {
        view?.showError(throwable)
    }

    fun abortLoad() {
        loadImagesTask?.cancel(true)
        loadImagesTask = null
    }

    fun loadImages() {
        view?.showLoading(true)
        loadImagesTask = view?.let { LoadImagesTask(it, this) }
        loadImagesTask?.execute()
    }

    fun onDoneSelectImages() {
        if (selectedImages.isNotEmpty()) {
            var i = 0
            while (i < selectedImages.size) {
                val image = selectedImages[i]
                val file = File(image.path)
                if (!file.exists()) {
                    selectedImages.removeAt(i)
                    i--
                }
                i++
            }
            view?.finishPickImages(selectedImages)
        }
    }

    fun captureImage(activity: Activity, requestCode: Int) {
        val context = activity.applicationContext
        val intent = cameraModule.getCameraIntent(activity, configuration)
        if (intent == null) {
            Toast.makeText(context, context.getString(R.string.create_file_failed), Toast.LENGTH_LONG).show()
            return
        }
        activity.startActivityForResult(intent, requestCode)
    }

    fun finishCaptureImage(context: Context, data: Intent?) {
        cameraModule.getImage(context, data, imageReadyListener = object : OnImageReadyListener {
            override fun onImageReady(image: List<Image>) {
                if (configuration.isReturnAfterFirst) {
                    view?.finishPickImages(image)
                } else {
                    view?.showCapturedImage()
                }
            }
        })
    }

    fun updateMenuDoneVisibility(menuDone: MenuItem?) {
        menuDone?.let {
            menuDone.isVisible = !selectedImages.isEmpty()
            if (configuration.mode == SINGLE && configuration.isReturnAfterFirst) {
                menuDone.isVisible = false
            }
        }
    }

    fun handleImageClick(clickPosition: Int, image: Image) {
        val selectedItemPosition = selectedImagePosition(image)
        if (configuration.mode == ImagePicker.MULTIPLE) {
            if (selectedItemPosition == -1) {
                if (selectedImages.size < configuration.limit) {
                    selectedImages.add(image)
                    view?.updateSelectedImage(image)
                    updateTitle()
                } else {
                    view?.showErrorExceedLimit()
                }
            } else {
                selectedImages.removeAt(selectedItemPosition)
                view?.removeImage(image, clickPosition)
                updateTitle()
            }
        } else {
            if (selectedItemPosition != -1) {
                view?.removeImage(image, clickPosition)
            } else {
                selectedImages.clear()
                view?.removeAllImages()
                selectedImages.add(image)
                view?.updateSelectedImage(image)
                updateTitle()

                if (configuration.isReturnAfterFirst) {
                    view?.finishPickImages(selectedImages)
                }
            }
        }
        updateTitle()
    }

    private fun updateTitle() {
        configuration.defaultToolbarTitle?.let {
            view?.updateTitle(it, configuration.mode, selectedImages.size, configuration.limit)
        }
    }

    private fun selectedImagePosition(image: Image): Int {
        selectedImages.indices
                .asSequence()
                .filter { selectedImages[it].path == image.path }
                .forEach { return it }
        return -1
    }

}
