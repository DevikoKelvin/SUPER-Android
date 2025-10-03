package id.erela.surveyproduct.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.core.graphics.drawable.toDrawable
import id.erela.surveyproduct.databinding.DialogConfirmationBinding

class ConfirmationDialog(context: Context): Dialog(context) {
    private val binding: DialogConfirmationBinding by lazy {
        DialogConfirmationBinding.inflate(layoutInflater)
    }
    private lateinit var confirmationDialogListener: ConfirmationDialogListener
    private lateinit var message: String
    private lateinit var confirmationText: String

    constructor(context: Context, message: String, confirmationText: String): this(context) {
        this.message = message
        this.confirmationText = confirmationText
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        setCancelable(false)

        init()
    }

    @SuppressLint("GestureBackNavigation")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        dismiss()
    }

    private fun init() {
        binding.apply {
            messageText.text = message
            confirmText.text = confirmationText

            cancelButton.setOnClickListener {
                dismiss()
            }

            confirmButton.setOnClickListener {
                confirmationDialogListener.onConfirm()
                dismiss()
            }
        }
    }

    fun setConfirmationDialogListener(confirmationDialogListener: ConfirmationDialogListener) {
        this.confirmationDialogListener = confirmationDialogListener
    }

    interface ConfirmationDialogListener {
        fun onConfirm()
    }
}