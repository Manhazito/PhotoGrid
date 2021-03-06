package net.filiperamos.photogrid.viewmodel

import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import net.filiperamos.photogrid.model.PictureData

/**
 * View model exposing observables related to a single picture
 */
class PictureViewModel(application: Application) : BaseViewModel(application) {
    val picture = MutableLiveData<PictureData>()
    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<Boolean>()

    /**
     * Manages the fetch of a single picture
     */
    fun loadImage(id: Long) {
        loading.value = true

        launch {
            var loadedPicture: PictureData? = null

            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE)
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

    /**
     * Uses the query cursor to access the picture
     */
    private fun getImageFromCursor(cursor: Cursor): PictureData? {
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)
        var picture: PictureData? = null

        if (cursor.count > 0) {
            cursor.moveToFirst()
            val id = cursor.getLong(idColumn)
            val title = cursor.getString(titleColumn)
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            picture = PictureData(id, title, contentUri)
        }

        return picture
    }

    /**
     * Emit picture value.
     * Emit loading value as false
     * Emit loading error value
     */
    private fun imageLoaded(image: PictureData?) {
        picture.value = image
        loading.value = false
        error.value = image == null
    }
}