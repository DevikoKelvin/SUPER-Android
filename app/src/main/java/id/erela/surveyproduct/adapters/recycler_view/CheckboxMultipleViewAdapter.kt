package id.erela.surveyproduct.adapters.recycler_view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.databinding.ListItemHistoryCheckboxMultipleBinding
import id.erela.surveyproduct.objects.AnswerItem

class CheckboxMultipleViewAdapter(private val answer: List<AnswerItem?>) :
    RecyclerView.Adapter<CheckboxMultipleViewAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemHistoryCheckboxMultipleBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ListItemHistoryCheckboxMultipleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).root
    )

    override fun getItemCount(): Int = answer.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = answer[position]

        with(holder) {
            binding.apply {
                answer.text = item?.option
                if (item?.answer == "1")
                    checkIcon.visibility = View.VISIBLE
                else
                    checkIcon.visibility = View.GONE
            }
        }
    }
}