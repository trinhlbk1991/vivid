package com.icetea09.vivid.common

import com.icetea09.vivid.model.Folder

interface ImageLoaderListener {
    fun onImageLoaded(folders: List<Folder>?)

    fun onFailed(throwable: Throwable)
}
