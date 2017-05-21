package com.icetea09.vivid.model

import com.icetea09.vivid.helper.ImagePickerUtils

import java.util.ArrayList

object ImageFactory {

    fun singleListFromPath(path: String): List<Image> {
        val images = ArrayList<Image>()
        images.add(Image(0, ImagePickerUtils.getNameFromFilePath(path), path, true))
        return images
    }
}
