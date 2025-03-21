package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.databinding.ListItemHistoryCheckboxMultipleBinding
import id.erela.surveyproduct.objects.CheckboxMultipleItem

class CheckboxMultipleSurveyAdapter(
    private val checkboxMultipleList: List<CheckboxMultipleItem>?,
    private val type: String
) : RecyclerView.Adapter<CheckboxMultipleSurveyAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemHistoryCheckboxMultipleBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ListItemHistoryCheckboxMultipleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).root
    )

    override fun getItemCount(): Int = checkboxMultipleList?.size!!

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = checkboxMultipleList?.get(position)

        with(holder) {
            binding.apply {
                checkboxMultipleViewContainer.visibility = View.GONE

                if (item != null) {
                    when (type) {
                        "checkbox" -> {
                            checkboxContainer.visibility = View.VISIBLE
                            checkboxOptionText.text = item.checkboxMultipleOptions?.options
                            checkBoxItem.isChecked = item.isChecked
                            checkboxContainer.setOnClickListener {
                                checkBoxItem.isChecked = !checkBoxItem.isChecked
                                item.isChecked = checkBoxItem.isChecked
                            }
                        }

                        "multiple" -> {
                            multipleContainer.visibility = View.VISIBLE
                            multipleOptionText.text = item.checkboxMultipleOptions?.options
                            multipleContainer.setOnClickListener {
                                for (i in checkboxMultipleList?.indices!!) {
                                    checkboxMultipleList[i].isChecked = i == position
                                }
                                notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }
}