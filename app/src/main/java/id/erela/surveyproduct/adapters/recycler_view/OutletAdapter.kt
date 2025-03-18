package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.databinding.ListItemOutletBinding
import id.erela.surveyproduct.objects.OutletItem

class OutletAdapter(private val outlets: ArrayList<OutletItem>) :
    RecyclerView.Adapter<OutletAdapter.ViewHolder>() {
    private lateinit var onOutletItemClickListener: OnOutletItemClickListener
    private var originalOutlets: ArrayList<OutletItem> = ArrayList(outlets)

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

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        outlets.clear()
        if (query.isEmpty()) {
            outlets.addAll(originalOutlets)
        } else {
            for (i in 0 until originalOutlets.size) {
                if (originalOutlets[i].name!!.contains(
                        query,
                        ignoreCase = true
                    ) || originalOutlets[i].outletID!!.contains(
                        query,
                        ignoreCase = true
                    ) || originalOutlets[i].address!!.contains(
                        query,
                        ignoreCase = true
                    ) || originalOutlets[i].cityRegency!!.contains(query, ignoreCase = true)
                ) {
                    outlets.add(originalOutlets[i])
                }
            }
            /*val filteredList = originalOutlets.filter {
                it.name!!.contains(query, ignoreCase = true) ||
                        it.outletID!!.contains(query, ignoreCase = true) ||
                        it.address!!.contains(query, ignoreCase = true) ||
                        it.cityRegency!!.contains(query, ignoreCase = true)
            }
            outlets.addAll(filteredList)*/
        }
        notifyDataSetChanged()
    }

    interface OnOutletItemClickListener {
        fun onOutletItemClick(outlet: OutletItem)
    }
}