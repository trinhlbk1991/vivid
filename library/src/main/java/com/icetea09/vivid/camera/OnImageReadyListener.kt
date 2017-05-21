package com.icetea09.vivid.camera

import com.icetea09.vivid.model.Image

interface OnImageReadyListener {
    fun onImageReady(image: List<Image>)
}
