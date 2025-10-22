package id.erela.surveyproduct.bottom_sheets

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import id.erela.surveyproduct.R
import id.erela.surveyproduct.databinding.BsFilterHistoryBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FilterHistoryBottomSheet(
    context: Context,
    private var start: String,
    private var end: String,
    private val fragmentManager: FragmentManager
) : BottomSheetDialog(context) {
    private val binding: BsFilterHistoryBinding by lazy {
        BsFilterHistoryBinding.inflate(layoutInflater)
    }
    private lateinit var onFilterOkListener: OnFilterOkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setCancelable(true)

        init()
    }

    private fun init() {
        binding.apply {
            val dateFormat =
                SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
            val serverDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("id-ID"))
            val startCalendar = Calendar.getInstance().apply {
                if (start.isNotEmpty()) {
                    time = serverDateFormat.parse(start)!!
                }
            }

            startDate.text = dateFormat.format(startCalendar.time)
            startCalendar.add(Calendar.DAY_OF_MONTH, 1)

            startDateButton.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(
                        if (context.getString(R.string.language) == "en") "Select Date"
                        else "Pilih Tanggal"
                    )
                    .setSelection(startCalendar.timeInMillis)
                    .build()
                datePicker.addOnPositiveButtonClickListener { selection ->
                    startCalendar.timeInMillis = selection
                    startDate.text = dateFormat.format(startCalendar.time)
                    start = serverDateFormat.format(startCalendar.time)
                }
                datePicker.show(fragmentManager, "START")
            }

            val nowCalendar = Calendar.getInstance().apply {
                if (end.isNotEmpty()) {
                    time = serverDateFormat.parse(end)!!
                }
            }

            endDate.text = dateFormat.format(nowCalendar.time)
            nowCalendar.add(Calendar.DAY_OF_MONTH, 1)

            endDateButton.setOnClickListener {
                val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(
                        if (context.getString(R.string.language) == "en") "Select Date"
                        else "Pilih Tanggal"
                    )
                    .setSelection(nowCalendar.timeInMillis)
                    .build()
                datePicker.addOnPositiveButtonClickListener { selection ->
                    nowCalendar.timeInMillis = selection
                    endDate.text = dateFormat.format(nowCalendar.time)
                    end = serverDateFormat.format(nowCalendar.time)
                }
                datePicker.show(fragmentManager, "END")
            }

            okButton.setOnClickListener {
                onFilterOkListener.onFilterOk(start, end)
                dismiss()
            }
        }
    }

    fun setOnFilterOkListener(onFilterOkListener: OnFilterOkListener) {
        this.onFilterOkListener = onFilterOkListener
    }

    interface OnFilterOkListener {
        fun onFilterOk(start: String, end: String)
    }
}