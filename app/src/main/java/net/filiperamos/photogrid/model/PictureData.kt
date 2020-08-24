package net.filiperamos.photogrid.model

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil

data class PictureData(
    val id: Long,
    val contentUri: Uri
) {
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<PictureData>() {
            override fun areItemsTheSame(oldItem: PictureData, newItem: PictureData) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PictureData, newItem: PictureData) =
                oldItem == newItem
        }
    }
}