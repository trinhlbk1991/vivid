package com.icetea09.vivid.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.icetea09.vivid.GlideApp
import com.icetea09.vivid.R
import com.icetea09.vivid.model.Folder

class FolderPickerAdapter(private val context: Context, private val folderClickListener: FolderPickerAdapter.OnFolderClickListener?)
    : RecyclerView.Adapter<FolderPickerAdapter.FolderViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(this.context)

    private var folders: List<Folder>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val itemView = inflater.inflate(R.layout.imagepicker_item_folder, parent, false)
        return FolderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders?.get(position)
        folder?.let {
            GlideApp.with(context)
                    .load(folder.images[0].path)
                    .placeholder(R.drawable.folder_placeholder)
                    .error(R.drawable.folder_placeholder)
                    .into(holder.image)

            holder.name.text = folder.folderName
            holder.number.text = folder.images.size.toString()

            holder.itemView.setOnClickListener {
                folderClickListener?.onFolderClick(folder)
            }
        }
    }

    fun setData(folders: List<Folder>) {
        this.folders = folders
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return folders?.let { (folders as List<Folder>).size } ?: 0
    }

    class FolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val image: ImageView = itemView.findViewById(R.id.image)
        internal val name: TextView = itemView.findViewById(R.id.tv_name)
        internal val number: TextView = itemView.findViewById(R.id.tv_number)

    }

    interface OnFolderClickListener {
        fun onFolderClick(folder: Folder)
    }

}
