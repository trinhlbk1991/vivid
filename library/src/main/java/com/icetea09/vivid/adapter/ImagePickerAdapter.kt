package com.icetea09.vivid.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.icetea09.vivid.R
import com.icetea09.vivid.model.Image

import java.util.ArrayList

class ImagePickerAdapter(private val context: Context, private val itemClickListener: ImagePickerAdapter.OnImageClickListener) : RecyclerView.Adapter<ImagePickerAdapter.ImageViewHolder>() {

    private val images = ArrayList<Image>()
    private val inflater: LayoutInflater = LayoutInflater.from(this.context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = inflater.inflate(R.layout.imagepicker_item_image, parent, false)
        return ImageViewHolder(itemView, itemClickListener)
    }

    override fun onBindViewHolder(viewHolder: ImageViewHolder, position: Int) {
        val image = images[position]
        Glide.with(context)
                .load(image.path)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(viewHolder.imageView)

        if (image.isSelected) {
            viewHolder.alphaView.alpha = 0.5f
            (viewHolder.itemView as FrameLayout).foreground = ContextCompat.getDrawable(context, R.drawable.ic_done_white)
        } else {
            viewHolder.alphaView.alpha = 0.0f
            (viewHolder.itemView as FrameLayout).foreground = null
        }

    }

    override fun getItemCount(): Int {
        return images.size
    }


    fun setData(images: List<Image>) {
        this.images.clear()
        this.images.addAll(images)
    }

    fun addSelected(image: Image) {
        image.isSelected = true
        notifyItemChanged(images.indexOf(image))
    }

    fun removeSelectedPosition(image: Image, clickPosition: Int) {
        image.isSelected = false
        notifyItemChanged(clickPosition)
    }

    fun removeAllSelectedSingleClick() {
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Image {
        return images[position]
    }

    class ImageViewHolder(itemView: View, private val itemClickListener: OnImageClickListener)
        : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        internal val imageView: ImageView = itemView.findViewById(R.id.image_view) as ImageView
        internal val alphaView: View = itemView.findViewById(R.id.view_alpha)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            view.isSelected = true
            itemClickListener.onImageClick(view, adapterPosition)
        }
    }

    interface OnImageClickListener {
        fun onImageClick(view: View, position: Int)
    }

}
