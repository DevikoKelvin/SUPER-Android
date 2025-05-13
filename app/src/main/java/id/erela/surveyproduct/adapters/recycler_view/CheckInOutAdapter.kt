package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.databinding.ListItemCheckInOutBinding
import id.erela.surveyproduct.objects.CheckInOutHistoryItem
import java.text.SimpleDateFormat
import java.util.Locale

class CheckInOutAdapter(
    private val checkInOutHistoryItemList: ArrayList<CheckInOutHistoryItem?>
) : RecyclerView.Adapter<CheckInOutAdapter.ViewHolder>() {
    private lateinit var onCheckInOutItemClickListener: OnCheckInOutItemClickListener

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemCheckInOutBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ListItemCheckInOutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).root
    )

    override fun getItemCount(): Int = checkInOutHistoryItemList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = checkInOutHistoryItemList[position]

        with(holder) {
            binding.apply {
                surveyId.text = item?.surveyID
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val dateOutputFormat =
                    SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
                val timeOutputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val parsedDate = inputFormat.parse(item?.checkInTime.toString())
                val parsedCheckInTime = inputFormat.parse(item?.checkInTime.toString())
                val formattedDate = parsedDate?.let { dateOutputFormat.format(it) }
                val formatedCheckInTime = parsedCheckInTime?.let { timeOutputFormat.format(it) }
                var formatedCheckOutTime = "-"
                if (item?.checkOutTime != null) {
                    val parsedCheckOutTime = inputFormat.parse(item.checkOutTime.toString())
                    formatedCheckOutTime = parsedCheckOutTime?.let { timeOutputFormat.format(it) }.toString()
                }

                date.text = formattedDate
                outletName.text = item?.outletName
                outletAddress.text = item?.outletAddress
                checkInTime.text = "Checked In\n${formatedCheckInTime ?: "-"}"
                checkOutTime.text = "Checked Out\n${formatedCheckOutTime}"

                itemView.setOnClickListener {
                    onCheckInOutItemClickListener.onCheckInOutItemClick(item)
                }

                if (position == checkInOutHistoryItemList.size - 1) {
                    val params = itemView.layoutParams as MarginLayoutParams
                    params.setMargins(
                        0,
                        0,
                        0,
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            200f,
                            itemView.resources.displayMetrics
                        ).toInt()
                    )
                    itemView.layoutParams = params
                }
            }
        }
    }

    fun setOnTodayTrackingItemClickListener(onCheckInOutItemClickListener: OnCheckInOutItemClickListener) {
        this.onCheckInOutItemClickListener = onCheckInOutItemClickListener
    }

    interface OnCheckInOutItemClickListener {
        fun onCheckInOutItemClick(item: CheckInOutHistoryItem?)
    }
}