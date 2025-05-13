package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup.MarginLayoutParams
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.databinding.ListItemOutletBinding
import id.erela.surveyproduct.objects.OutletItem

class OutletAdapter(private val outlets: ArrayList<OutletItem>, private val usage: String) :
    RecyclerView.Adapter<OutletAdapter.ViewHolder>() {
    private lateinit var onOutletItemClickListener: OnOutletItemClickListener

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemOutletBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ListItemOutletBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).root
    )

    override fun getItemCount(): Int = outlets.size
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = outlets[position]

        with(holder) {
            binding.apply {
                outletName.text = item.name
                outletID.text = item.outletID
                address.text = item.address
                cityName.text = item.provinceName

                itemView.setOnClickListener {
                    when (usage) {
                        "detail" -> onOutletItemClickListener.onOutletForDetailItemClick(item.iD!!)
                        "survey" -> onOutletItemClickListener.onOutletForSurveyItemClick(item)
                    }
                }

                if (usage == "detail") {
                    if (position == outlets.size - 1) {
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
    }

    fun setOnOutletItemClickListener(onOutletItemClickListener: OnOutletItemClickListener) {
        this.onOutletItemClickListener = onOutletItemClickListener
    }

    interface OnOutletItemClickListener {
        fun onOutletForDetailItemClick(id: Int)
        fun onOutletForSurveyItemClick(outlet: OutletItem)
    }
}