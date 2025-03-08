package id.erela.surveyproduct.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.core.graphics.drawable.toDrawable
import id.erela.surveyproduct.databinding.DialogLoadingBinding

class LoadingDialog(context: Context) : Dialog(context) {
    private val binding: DialogLoadingBinding by lazy {
        DialogLoadingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(false)
    }
}