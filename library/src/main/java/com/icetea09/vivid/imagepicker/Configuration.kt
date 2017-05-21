package com.icetea09.vivid.imagepicker

import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable

import com.icetea09.vivid.R
import com.icetea09.vivid.imagepicker.ImagePicker.Companion.EXTRA_FOLDER_TITLE
import com.icetea09.vivid.imagepicker.ImagePicker.Companion.EXTRA_IMAGE_DIRECTORY
import com.icetea09.vivid.imagepicker.ImagePicker.Companion.EXTRA_LIMIT
import com.icetea09.vivid.imagepicker.ImagePicker.Companion.EXTRA_MODE
import com.icetea09.vivid.imagepicker.ImagePicker.Companion.EXTRA_RETURN_AFTER_FIRST
import com.icetea09.vivid.imagepicker.ImagePicker.Companion.MAX_LIMIT
import com.icetea09.vivid.imagepicker.ImagePicker.Companion.MULTIPLE

class Configuration : Parcelable {

    var defaultToolbarTitle: String? = null
    var capturedImageDirectory: String? = null
    var mode: Int = 0
    var limit: Int = 0
    var isReturnAfterFirst: Boolean = false
    var theme: Int = 0

    constructor(context: Context) {
        this.mode = ImagePicker.MULTIPLE
        this.limit = ImagePicker.MAX_LIMIT
        this.defaultToolbarTitle = context.getString(R.string.tap_to_select_image)
        this.capturedImageDirectory = context.getString(R.string.image_directory)
        this.isReturnAfterFirst = true
        this.theme = R.style.AppTheme
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.defaultToolbarTitle)
        dest.writeString(this.capturedImageDirectory)
        dest.writeInt(this.mode)
        dest.writeInt(this.limit)
        dest.writeByte(if (this.isReturnAfterFirst) 1.toByte() else 0.toByte())
        dest.writeInt(this.theme)
    }

    protected constructor(parcel: Parcel) {
        this.defaultToolbarTitle = parcel.readString()
        this.capturedImageDirectory = parcel.readString()
        this.mode = parcel.readInt()
        this.limit = parcel.readInt()
        this.isReturnAfterFirst = parcel.readByte().toInt() != 0
        this.theme = parcel.readInt()
    }

    companion object {

        fun create(context: Context, intent: Intent): Configuration {
            val config = Configuration(context)
            config.mode = intent.getIntExtra(EXTRA_MODE, MULTIPLE)
            config.limit = intent.getIntExtra(EXTRA_LIMIT, MAX_LIMIT)
            config.defaultToolbarTitle = intent.getStringExtra(EXTRA_FOLDER_TITLE)
            config.capturedImageDirectory = intent.getStringExtra(EXTRA_IMAGE_DIRECTORY)
            config.isReturnAfterFirst = intent.getBooleanExtra(EXTRA_RETURN_AFTER_FIRST, false)
            return config
        }

        @JvmField @Suppress("unused")
        val CREATOR: Parcelable.Creator<Configuration> = object : Parcelable.Creator<Configuration> {
            override fun createFromParcel(source: Parcel): Configuration {
                return Configuration(source)
            }

            override fun newArray(size: Int): Array<Configuration> {
                return arrayOf()
            }
        }
    }
}
