package net.filiperamos.photogrid.view.listscreen

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_list.*
import net.filiperamos.photogrid.R
import net.filiperamos.photogrid.view.MainActivity
import net.filiperamos.photogrid.viewmodel.PictureListViewModel

class ListFragment : Fragment() {
    private lateinit var vm: PictureListViewModel
    private lateinit var picturesAdapter: PictureListAdapter
    private var canUseCamera = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        canUseCamera = activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) ?: false

        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProvider(this).get(PictureListViewModel::class.java)
        picturesAdapter = PictureListAdapter(arrayListOf(), ::deletePicture)

        photosRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            adapter = picturesAdapter
        }

        refreshLayout.setOnRefreshListener {
            refreshLayout.isRefreshing = false
            loadData()
        }

        observe()
        loadData()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        loadData()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadData() {
        vm.refresh()
    }

    private fun observe() {
        vm.loading.observe(viewLifecycleOwner, {
            it?.let { isLoading ->
                if (isLoading) {
                    noPhotosTextView.visibility = View.GONE
                }
                loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        })

        vm.empty.observe(viewLifecycleOwner, {
            it?.let { isEmpty ->
                noPhotosTextView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            }
        })

        vm.pictures.observe(viewLifecycleOwner, {
            it?.let { pictures ->
                photosRecyclerView.visibility = View.VISIBLE
                picturesAdapter.updateData(pictures)
            }
        })

        vm.permissionNeededForDelete.observe(viewLifecycleOwner, { intentSender ->
            intentSender?.let {
                startIntentSenderForResult(
                    intentSender,
                    DELETE_REQUEST,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun deletePicture(id: Long) {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(R.string.delete_photo)
                .setMessage(R.string.confirm_delete_photo)
                .setPositiveButton(R.string.yes) { _, _ ->
                    vm.deletePicture(id)
                }
                .setNegativeButton(R.string.no) { _, _ -> }
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == DELETE_REQUEST) {
            vm.deletePendingPicture()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (canUseCamera) {
            inflater.inflate(R.menu.main_menu, menu)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionTakePhoto -> {
                (activity as MainActivity).takePhoto()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "ListFragment"
        private const val DELETE_REQUEST = 2
    }
}