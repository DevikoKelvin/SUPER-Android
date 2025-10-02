package id.erela.surveyproduct.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.DialogPhotoPreviewBinding

class PhotoPreviewDialog(
    context: Context,
    private val photoName: String?
) : Dialog(context) {
    private val binding: DialogPhotoPreviewBinding by lazy {
        DialogPhotoPreviewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(false)

        init()
    }

    private fun init() {
        binding.apply {
            loadingIndicator.visibility = View.VISIBLE
            photoView.visibility = View.INVISIBLE
            closeButton.setOnClickListener {
                dismiss()
            }
            Glide.with(context)
                .load(BuildConfig.IMAGE_URL + photoName)
                .placeholder(AppCompatResources.getDrawable(context, R.drawable.image_placeholder))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        loadingIndicator.visibility = View.GONE
                        photoView.visibility = View.VISIBLE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        loadingIndicator.visibility = View.GONE
                        photoView.visibility = View.VISIBLE
                        return false
                    }
                })
                .into(photoView)
        }
    }
}