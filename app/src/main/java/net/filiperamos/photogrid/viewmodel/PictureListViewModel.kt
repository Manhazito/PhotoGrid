package net.filiperamos.photogrid.viewmodel

import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.IntentSender
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import net.filiperamos.photogrid.model.PictureData

/**
 * View model exposing observables related to the list of Pictures
 */
class PictureListViewModel(application: Application) : BaseViewModel(application) {
    val pictures = MutableLiveData<List<PictureData>>()
    val empty = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()
    val permissionNeededForDelete = MutableLiveData<IntentSender?>()

    private var pendingDeleteImageId: Long? = null
    private var images = mutableListOf<PictureData>()

    /**
     * Manages the fetch of all pictures
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun refresh() {
        loading.value = true

        launch {
            var pictureList = mutableListOf<PictureData>()

            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.TITLE)
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} ASC"

            getApplication<Application>().contentResolver.query(
                uri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                pictureList = addImagesFromCursor(cursor)
            }

            dataRetrieved(pictureList)
        }
    }

    /**
     * Uses the query cursor to access the pictures
     */
    private fun addImagesFromCursor(cursor: Cursor): MutableList<PictureData> {
        images = mutableListOf()

        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val title = cursor.getString(titleColumn)
            Log.d(TAG, "$id title: $title")

            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )

            val image = PictureData(id, title, contentUri)
            images.add(image)

        }

        return images
    }

    /**
     * Emit picture list value.
     * Emit empty list value
     * Emit loading value as false
     */
    private fun dataRetrieved(pictureList: List<PictureData>) {
        pictures.value = pictureList
        empty.value = pictureList.isEmpty()
        loading.value = false
    }

    /**
     * Tries to delete a picture
     * Asks for permission if deletion not authorized
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun deletePicture(id: Long) {
        loading.value = true
        pendingDeleteImageId = null

        launch {
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            val where = "${MediaStore.Images.Media._ID} == ?"
            val whereArgs = arrayOf("$id")

            try {
                getApplication<Application>().contentResolver.delete(
                    contentUri,
                    where,
                    whereArgs
                )

                refresh()
            } catch (securityException: SecurityException) {
                val recoverableSecurityException = securityException as? RecoverableSecurityException ?: throw securityException
                pendingDeleteImageId = id
                permissionNeededForDelete.value = recoverableSecurityException.userAction.actionIntent.intentSender
            }
        }
    }

    /**
     * Deletes the previously non deleted picture (after permission granted)
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun deletePendingPicture() {
        pendingDeleteImageId?.let {
            deletePicture(it)
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "PictureListViewModel"
    }
}

