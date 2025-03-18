package id.erela.surveyproduct.bottom_sheets

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.bottomsheet.BottomSheetDialog
import id.erela.surveyproduct.databinding.BsFilterHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class FilterHistoryBottomSheet(
    context: Context,
    private var start: String,
    private var end: String
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
            val startCalendar = Calendar.getInstance()

            if (start.isNotEmpty()){
                startCalendar.time = serverDateFormat.parse(start)!!
            }

            startDate.text = dateFormat.format(startCalendar.time)

            startDateButton.setOnClickListener {
                val calendar = Calendar.getInstance().apply {
                    if (start.isNotEmpty()) {
                        time = serverDateFormat.parse(start)!!
                    }
                }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(mYear, mMonth, mDayOfMonth)
                        startDate.text = dateFormat.format(selectedDate.time)
                        start = serverDateFormat.format(selectedDate.time)
                        Log.e("START", start)
                    },
                    year, month, day
                )
                datePickerDialog.show()
            }

            val nowCalendar = Calendar.getInstance()

            if (end.isNotEmpty()){
                nowCalendar.time = serverDateFormat.parse(end)!!
            }
            endDate.text = dateFormat.format(nowCalendar.time)

            endDateButton.setOnClickListener {
                val calendar = Calendar.getInstance().apply {
                    if (end.isNotEmpty()) {
                        time = serverDateFormat.parse(end)!!
                    }
                }
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(mYear, mMonth, mDayOfMonth)
                        endDate.text = dateFormat.format(selectedDate.time)
                        end = serverDateFormat.format(selectedDate.time)
                        Log.e("END", end)
                    },
                    year, month, day
                )
                datePickerDialog.show()
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