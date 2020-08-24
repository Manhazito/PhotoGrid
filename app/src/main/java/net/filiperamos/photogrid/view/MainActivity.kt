package net.filiperamos.photogrid.view

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_main.*
import net.filiperamos.photogrid.R
import net.filiperamos.photogrid.view.listscreen.ListFragment

/**
 * Work log
 *
 * Started @ 20:00 - 20/08/2020
 * Stopped @ 21:30 - 20/08/2020
 *
 * Started @ 10:00 - 21/08/2020
 * Stopped @ 12:00 - 21/08/2020
 *
 * Started @ 10:00 - 22/08/2020
 * Stopped @ 12:00 - 22/08/2020
 *
 * Started @ 15:00 - 22/08/2020
 * Stopped @ 16:00 - 22/08/2020
 *
 * Started @ 11:00 - 23/08/2020
 * Stopped @ 12:00 - 2#/08/2020
 *
 * Started @ 11:00 - 24/08/2020
 * Stopped @ 12:30 - 24/08/2020
 *
 * TOTAL: 10h
 */
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navController = Navigation.findNavController(this, R.id.fragment)
        NavigationUI.setupActionBarWithNavController(this, navController)

        checkPermissions()
    }

    override fun onSupportNavigateUp(): Boolean = NavigationUI.navigateUp(navController, null)

    private fun checkPermissions() {
        if (!hasStoragePermissions()) {
            if (shouldShowStoragePermissionsRationale()) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.access_pictures)
                    .setMessage(R.string.require_access_pictures)
                    .setPositiveButton(R.string.ask_me) { _, _ ->
                        requestPermissions()
                    }
                    .setNegativeButton(R.string.no) { _, _ -> }
                    .show()
            } else {
                requestPermissions()
            }
        }
    }

    private fun hasStoragePermissions() =
        ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowStoragePermissionsRationale() =
        ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)


    private fun requestPermissions() {
        val permissions = arrayOf(
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE
        )

        ActivityCompat.requestPermissions(this, permissions, READ_EXTERNAL_STORAGE_REQUEST)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    reloadListFragmentData()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun reloadListFragmentData() {
        val activeFragment = fragment.childFragmentManager.primaryNavigationFragment
        (activeFragment as? ListFragment)?.loadData()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.also {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "tmp_image.jpg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, CAMERA_REQUEST)
            }
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "MainActivity"

        const val READ_EXTERNAL_STORAGE_REQUEST = 1
        const val CAMERA_REQUEST = 3
    }
}