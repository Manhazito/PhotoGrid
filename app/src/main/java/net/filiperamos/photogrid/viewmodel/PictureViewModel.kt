package net.filiperamos.photogrid.viewmodel

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import net.filiperamos.photogrid.model.PictureData

class PictureViewModel(application: Application) : BaseViewModel(application) {
    val picture = MutableLiveData<PictureData>()
    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<Boolean>()

    fun loadImage(id: Long) {
        loading.value = true

        launch {
            var loadedPicture: PictureData? = null

            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = "${MediaStore.Images.Media._ID} == ?"
            val selectionArgs = arrayOf("$id")

            getApplication<Application>().contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )?.use {
                loadedPicture = getImageFromCursor(it)
            }

            imageLoaded(loadedPicture)
        }
    }

    private fun getImageFromCursor(cursor: Cursor): PictureData? {
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        var picture: PictureData? = null

        if (cursor.count > 0) {
            cursor.moveToFirst()
            val id = cursor.getLong(idColumn)
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            picture = PictureData(id, contentUri)
        }

        return picture
    }

    private fun imageLoaded(image: PictureData?) {
        picture.value = image
        loading.value = false
        error.value = image == null
    }
}