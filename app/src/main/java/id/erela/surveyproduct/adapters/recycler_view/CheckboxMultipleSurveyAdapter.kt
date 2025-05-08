package id.erela.surveyproduct.adapters.recycler_view

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.activities.AnswerActivity
import id.erela.surveyproduct.databinding.ListItemHistoryCheckboxMultipleBinding
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.objects.CheckboxMultipleItem

class CheckboxMultipleSurveyAdapter(
    private val checkboxMultipleList: List<CheckboxMultipleItem>?,
    private val type: String,
    private val context: Context
) : MultipleSurveyAdapter<CheckboxMultipleItem>(checkboxMultipleList) {
    override fun onBindViewHolder(
        holder: MultipleSurveyAdapter<CheckboxMultipleItem>.ViewHolder,
        position: Int
    ) {
        val item = checkboxMultipleList?.get(position)

        with(holder) {
            binding.apply {
                checkboxMultipleViewContainer.visibility = View.GONE
                val questionID = item?.checkboxMultipleOptions?.questionID ?: 0
                val subQuestionID = item?.checkboxMultipleOptions?.subQuestionID ?: 0

                if (item != null) {
                    when (type) {
                        "checkbox" -> {
                            checkboxMultipleViewContainer.visibility = View.GONE
                            checkboxContainer.visibility = View.VISIBLE
                            multipleContainer.visibility = View.GONE
                            checkboxOptionText.text = item.checkboxMultipleOptions?.options

                            // Update the checkbox state from SharedPreferences
                            val prefKey =
                                "${AnswerActivity.ANSWER_CHECKBOX_MULTIPLE}_${questionID}_${subQuestionID}_${position}"
                            val isCheckboxChecked =
                                SharedPreferencesHelper.getSharedPreferences(context)
                                    .getBoolean(prefKey, false)

                            // Update both the view and data model
                            item.isChecked = isCheckboxChecked
                            checkBoxItem.isChecked = isCheckboxChecked

                            checkboxContainer.setOnClickListener {
                                checkBoxItem.isChecked = !checkBoxItem.isChecked
                                // Update the data model
                                item.isChecked = checkBoxItem.isChecked

                                // Immediately save to SharedPreferences
                                SharedPreferencesHelper.getSharedPreferences(context).edit {
                                    putInt(
                                        "${AnswerActivity.ANSWER_QUESTION_ID}_${questionID}_${subQuestionID}",
                                        questionID
                                    )
                                    putInt(
                                        "${AnswerActivity.ANSWER_SUBQUESTION_ID}_${questionID}_${subQuestionID}",
                                        subQuestionID
                                    )
                                    putBoolean(prefKey, item.isChecked)
                                    apply() // Use apply() instead of commit() for better performance
                                }

                                // Notify just this item changed
                                notifyDataSetChanged()
                            }

                            checkBoxItem.setOnCheckedChangeListener { _, isChecked ->
                                checkBoxItem.isChecked = isChecked
                                // Update the data model
                                item.isChecked = checkBoxItem.isChecked

                                // Immediately save to SharedPreferences
                                SharedPreferencesHelper.getSharedPreferences(context).edit {
                                    putInt(
                                        "${AnswerActivity.ANSWER_QUESTION_ID}_${questionID}_${subQuestionID}",
                                        questionID
                                    )
                                    putInt(
                                        "${AnswerActivity.ANSWER_SUBQUESTION_ID}_${questionID}_${subQuestionID}",
                                        subQuestionID
                                    )
                                    putBoolean(prefKey, item.isChecked)
                                    apply() // Use apply() instead of commit() for better performance
                                }

                                // Notify just this item changed
                                notifyDataSetChanged()
                            }
                        }

                        "multiple" -> {
                            checkboxMultipleViewContainer.visibility = View.GONE
                            checkboxContainer.visibility = View.GONE
                            multipleContainer.visibility = View.VISIBLE
                            multipleOptionText.text = item.checkboxMultipleOptions?.options

                            // Load the saved state for this radio button from SharedPreferences
                            val prefKey = "${AnswerActivity.ANSWER_CHECKBOX_MULTIPLE}_${questionID}_${subQuestionID}_${position}"
                            val isMultipleChecked = SharedPreferencesHelper.getSharedPreferences(context)
                                .getBoolean(prefKey, false)

                            // Update both the view and data model with the saved state
                            item.isChecked = isMultipleChecked
                            radioItem.isChecked = isMultipleChecked
                            if (isMultipleChecked) {
                                selectedPosition = position
                            }

                            // Handle radio button click on the container
                            multipleContainer.setOnClickListener {
                                handleRadioSelection(position, item, questionID, subQuestionID)
                            }

                            // Handle the radio button click itself
                            radioItem.setOnCheckedChangeListener { _, isChecked ->
                                if (isChecked) {
                                    handleRadioSelection(position, item, questionID, subQuestionID)
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private fun handleRadioSelection(position: Int, item: CheckboxMultipleItem, questionID: Int, subQuestionID: Int) {
        // Only process if this isn't already the selected position
        if (position != selectedPosition) {
            selectedPosition = position

            // Update the selected item
            item.isChecked = true

            // Uncheck all other items
            checkboxMultipleList?.forEachIndexed { index, listItem ->
                if (index != position) {
                    listItem.isChecked = false
                }
            }

            // Save all states to SharedPreferences
            SharedPreferencesHelper.getSharedPreferences(context).edit {
                putInt(
                    "${AnswerActivity.ANSWER_QUESTION_ID}_${questionID}_${subQuestionID}",
                    questionID
                )
                putInt(
                    "${AnswerActivity.ANSWER_SUBQUESTION_ID}_${questionID}_${subQuestionID}",
                    subQuestionID
                )

                // Update all radio selections for this question
                checkboxMultipleList?.forEachIndexed { idx, _ ->
                    val itemPrefKey = "${AnswerActivity.ANSWER_CHECKBOX_MULTIPLE}_${questionID}_${subQuestionID}_${idx}"
                    putBoolean(itemPrefKey, idx == position)
                }
                apply()
            }

            // Notify the adapter to update all items
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(
        ListItemHistoryCheckboxMultipleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun getItemCount(): Int = checkboxMultipleList?.size!!
}

abstract class MultipleSurveyAdapter<T>(private val item: List<T>?) :
    RecyclerView.Adapter<MultipleSurveyAdapter<T>.ViewHolder>() {
    var selectedPosition = -1

    inner class ViewHolder(val binding: ListItemHistoryCheckboxMultipleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val clickHandler: (View) -> Unit = {
            selectedPosition = adapterPosition
            notifyDataSetChanged()
        }

        init {
            binding.apply {
                root.setOnClickListener(clickHandler)
                radioItem.setOnClickListener(clickHandler)
            }
        }
    }
}