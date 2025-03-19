package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.databinding.ListItemOutletBinding
import id.erela.surveyproduct.objects.OutletItem

class OutletAdapter(private val outlets: ArrayList<OutletItem>) :
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
                cityName.text = item.cityRegency

                itemView.setOnClickListener {
                    onOutletItemClickListener.onOutletItemClick(item)
                }
            }
        }
    }

    fun setOnOutletItemClickListener(onOutletItemClickListener: OnOutletItemClickListener) {
        this.onOutletItemClickListener = onOutletItemClickListener
    }

    interface OnOutletItemClickListener {
        fun onOutletItemClick(outlet: OutletItem)
    }
}

class OutletDiffUtilCallback(
    private val oldList: List<OutletItem>,
    private val newList: List<OutletItem>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].name == newList[newItemPosition].name
                || oldList[oldItemPosition].address == newList[newItemPosition].address
                || oldList[oldItemPosition].cityRegency == newList[newItemPosition].cityRegency

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]
}