package net.filiperamos.photogrid.view.listscreen

import android.os.Build
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_image.view.*
import net.filiperamos.photogrid.R
import net.filiperamos.photogrid.databinding.ItemImageBinding
import net.filiperamos.photogrid.model.PictureData

class PictureListAdapter(private val imageList: ArrayList<PictureData>, val deletePicture: (Long) -> Unit) :
    ListAdapter<PictureData, PictureListAdapter.ListViewHolder>(PictureData.DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<ItemImageBinding>(inflater, R.layout.item_image, parent, false)

        return ListViewHolder(view)

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val pictureData = imageList[position]
        Log.d(TAG, "$position: ${pictureData.id} | ${pictureData.contentUri.path}")
        val bmp = holder.itemView.context.contentResolver.loadThumbnail(
            imageList[position].contentUri,
            Size(250, 250),
            null
        )

        holder.view.imageView.setImageBitmap(bmp)
        holder.view.imageIdTextView.text = "${imageList[position].id}"

        holder.view.imageView.setOnClickListener {
            onImageClickListener(holder.view.root)
        }
        holder.view.imageView.setOnLongClickListener {
            deletePicture(imageList[position].id)
            true
        }
    }

    override fun getItemCount(): Int = imageList.size

    fun updateData(pictures: List<PictureData>) {
        imageList.clear()
        imageList.addAll(pictures)
        notifyDataSetChanged()
    }

    fun onImageClickListener(view: View) {
        Log.d(TAG, "Click!")
        val action = ListFragmentDirections.actionListFragmentToImageFragment()
        action.imageId = view.imageIdTextView.text.toString().toLong()
        Navigation.findNavController(view).navigate(action)
    }

    class ListViewHolder(var view: ItemImageBinding) : RecyclerView.ViewHolder(view.root)

    companion object {
        @Suppress("unused")
        private const val TAG = "PictureListAdapter"
    }
}
