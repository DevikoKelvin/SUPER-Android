package id.erela.surveyproduct.helpers.customs

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import id.erela.surveyproduct.databinding.CustomToastBinding

class CustomToast(private val context: Context) : Toast(context) {
    private var backgroundColor: Int = 0
    private var fontColor: Int = 0
    private var duration: Int = 0
    private var toastMessage: String = ""
    private var gravity: Int = Gravity.TOP
    private var marginTop: Int = 0
    private var marginBottom: Int = 0

    companion object {
        const val MARGIN_DEFAULT = 100
        fun getInstance(context: Context): CustomToast {
            return CustomToast(context)
        }
    }

    fun setBackgroundColor(backgroundColor: Int): CustomToast {
        this.backgroundColor = backgroundColor
        return this
    }

    fun setMessage(message: String): CustomToast {
        this.toastMessage = message
        return this
    }

    fun setFontColor(fontColor: Int): CustomToast {
        this.fontColor = fontColor
        return this
    }

    fun setDurations(duration: Int): CustomToast {
        this.duration = duration
        return this
    }

    fun setGravity(gravity: Int): CustomToast {
        this.gravity = gravity
        return this
    }

    fun setMarginTop(margin: Int): CustomToast {
        this.marginTop = margin
        return this
    }

    fun setMarginBottom(margin: Int): CustomToast {
        this.marginBottom = margin
        return this
    }

    override fun show() {
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding: CustomToastBinding = CustomToastBinding.inflate(layoutInflater)

        binding.apply {
            toastRootLayout.setPadding(30, marginTop, 30, marginBottom)
            if (fontColor != 0) message.setTextColor(fontColor)
            message.text = toastMessage.ifEmpty { "" }
            if (backgroundColor != 0) container.setBackgroundColor(backgroundColor)
        }

        Toast(context).also {
            with(it) {
                duration = duration
                setGravity(this@CustomToast.gravity or Gravity.FILL_HORIZONTAL, 100, 20)
                view = binding.root
                show()
            }
        }
    }
}