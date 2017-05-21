package com.icetea09.vivid.model

import android.os.Parcel
import android.os.Parcelable

class Image : Parcelable {

    var id: Long = 0
    var name: String? = null
    var path: String? = null
    var isSelected: Boolean = false

    constructor(id: Long, name: String, path: String, isSelected: Boolean) {
        this.id = id
        this.name = name
        this.path = path
        this.isSelected = isSelected
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(this.id)
        dest.writeString(this.name)
        dest.writeString(this.path)
        dest.writeByte(if (this.isSelected) 1.toByte() else 0.toByte())
    }

    protected constructor(parcel: Parcel) {
        this.id = parcel.readLong()
        this.name = parcel.readString()
        this.path = parcel.readString()
        this.isSelected = parcel.readByte().toInt() != 0
    }

    companion object {
        @JvmField @Suppress("unused")
        val CREATOR: Parcelable.Creator<Image> = object : Parcelable.Creator<Image> {
            override fun createFromParcel(source: Parcel): Image {
                return Image(source)
            }

            override fun newArray(size: Int): Array<Image> {
                return arrayOf()
            }
        }
    }
}
