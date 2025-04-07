package id.erela.surveyproduct.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.databinding.DialogPhotoPreviewBinding

class PhotoPreviewDialog(
    context: Context,
    private val photoName: String?
): Dialog(context) {
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
            closeButton.setOnClickListener {
                dismiss()
            }
            Glide.with(context)
                .load(BuildConfig.IMAGE_URL + photoName)
                .into(photoView)
        }
    }
}