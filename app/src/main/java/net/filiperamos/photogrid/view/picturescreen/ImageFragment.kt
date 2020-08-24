package net.filiperamos.photogrid.view.picturescreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_image.*
import kotlinx.android.synthetic.main.fragment_list.loadingProgressBar
import net.filiperamos.photogrid.R
import net.filiperamos.photogrid.viewmodel.PictureViewModel

class ImageFragment : Fragment() {
    private lateinit var vm: PictureViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var imageId = 0L
        arguments?.let {
            imageId = ImageFragmentArgs.fromBundle(it).imageId
        }

        vm = ViewModelProvider(this).get(PictureViewModel::class.java)

        observe()
        vm.loadImage(imageId)
    }

    private fun observe() {
        vm.loading.observe(viewLifecycleOwner, {
            it?.let { isLoading ->
                if (isLoading) {
                    selectedImageView.visibility = View.GONE
                    loadingErrorTextView.visibility = View.GONE
                }
                loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        })

        vm.error.observe(viewLifecycleOwner, {
            it?.let { hasError ->
                if (hasError) {
                    selectedImageView.visibility = View.GONE
                    loadingProgressBar.visibility = View.GONE
                }
                loadingErrorTextView.visibility = if (hasError) View.VISIBLE else View.GONE
            }
        })

        vm.picture.observe(viewLifecycleOwner, {
            it?.let { picture ->
                selectedImageView.visibility = View.VISIBLE

                selectedImageView.setImageURI(picture.contentUri)

                setTitle(picture.title)
            }
        })
    }

    private fun setTitle(title: String){
        activity?.title = title
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "ImageFragment"
    }
}