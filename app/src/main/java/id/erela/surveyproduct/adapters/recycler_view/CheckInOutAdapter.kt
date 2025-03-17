package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.databinding.ListItemCheckInOutBinding
import id.erela.surveyproduct.objects.CheckInOutItem
import java.text.SimpleDateFormat
import java.util.Locale

class CheckInOutAdapter(
    private val todayTrackingList: List<CheckInOutItem?>
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

    override fun getItemCount(): Int = todayTrackingList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = todayTrackingList[position]

        with(holder) {
            binding.apply {
                surveyId.text = item?.surveyID

                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val dateOutputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
                val timeOutputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val parsedDate = inputFormat.parse(item?.checkInTime ?: "")
                val parsedCheckInTime = inputFormat.parse(item?.checkInTime ?: "-")
                val parsedCheckOutTime = inputFormat.parse(item?.checkOutTime ?: "-")
                val formattedDate = parsedDate?.let { dateOutputFormat.format(it) }
                val formatedCheckInTime = parsedCheckInTime?.let { timeOutputFormat.format(it) }
                val formatedCheckOutTime = parsedCheckOutTime?.let { timeOutputFormat.format(it) }

                date.text = formattedDate
                outletName.text = item?.outletName
                outletAddress.text = item?.outletAddress
                checkInTime.text = "Checked In\n$formatedCheckInTime"
                checkOutTime.text = "Checked Out\n$formatedCheckOutTime"

                itemView.setOnClickListener {
                    onCheckInOutItemClickListener.onCheckInOutItemClick(item)
                }
            }
        }
    }

    fun setOnTodayTrackingItemClickListener(onCheckInOutItemClickListener: OnCheckInOutItemClickListener) {
        this.onCheckInOutItemClickListener = onCheckInOutItemClickListener
    }

    interface OnCheckInOutItemClickListener {
        fun onCheckInOutItemClick(item: CheckInOutItem?)
    }
}