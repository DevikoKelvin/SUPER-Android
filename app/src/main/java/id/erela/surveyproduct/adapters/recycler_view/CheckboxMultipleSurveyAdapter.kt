package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.content.Context
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
                val questionID = SharedPreferencesHelper.getSharedPreferences(context).getInt(
                    "${AnswerActivity.ANSWER_QUESTION_ID}_${item?.checkboxMultipleOptions?.questionID}",
                    0
                )
                val subQuestionID = SharedPreferencesHelper.getSharedPreferences(context).getInt(
                    "${AnswerActivity.ANSWER_SUBQUESTION_ID}_${item?.checkboxMultipleOptions?.questionID}",
                    0
                )

                if (item != null) {
                    when (type) {
                        "checkbox" -> {
                            checkboxMultipleViewContainer.visibility = View.GONE
                            checkboxContainer.visibility = View.VISIBLE
                            multipleContainer.visibility = View.GONE
                            checkboxOptionText.text = item.checkboxMultipleOptions?.options
                            val checkbox =
                                SharedPreferencesHelper.getSharedPreferences(context).getBoolean(
                                    "${AnswerActivity.ANSWER_CHECKBOX_MULTIPLE}_${questionID}_" +
                                            "${subQuestionID}_${position}",
                                    false
                                )
                            checkBoxItem.isChecked = checkbox

                            checkboxContainer.setOnClickListener {
                                checkBoxItem.isChecked = !checkBoxItem.isChecked
                                item.isChecked = checkBoxItem.isChecked
                                notifyDataSetChanged()
                                SharedPreferencesHelper.getSharedPreferences(context).edit {
                                    putInt(
                                        "${AnswerActivity.ANSWER_QUESTION_ID}_${item.checkboxMultipleOptions?.questionID}",
                                        item.checkboxMultipleOptions?.questionID!!
                                    )
                                    val subQuestionId =
                                        item.checkboxMultipleOptions.subQuestionID ?: 0
                                    putInt(
                                        "${AnswerActivity.ANSWER_SUBQUESTION_ID}_${item.checkboxMultipleOptions.questionID}",
                                        subQuestionId
                                    )
                                    putBoolean(
                                        "${AnswerActivity.ANSWER_CHECKBOX_MULTIPLE}_${item.checkboxMultipleOptions.questionID}_" +
                                                "${subQuestionId}_${position}",
                                        item.isChecked
                                    )
                                }
                            }
                        }

                        "multiple" -> {
                            checkboxMultipleViewContainer.visibility = View.GONE
                            checkboxContainer.visibility = View.GONE
                            multipleContainer.visibility = View.VISIBLE
                            multipleOptionText.text = item.checkboxMultipleOptions?.options
                            val radio =
                                SharedPreferencesHelper.getSharedPreferences(context).getBoolean(
                                    "${AnswerActivity.ANSWER_CHECKBOX_MULTIPLE}_${questionID}_${subQuestionID}_${position}",
                                    false
                                )
                            if (
                                questionID == item.checkboxMultipleOptions?.questionID
                                && subQuestionID == item.checkboxMultipleOptions.subQuestionID
                            )
                                radioItem.isChecked = radio
                            multipleContainer.setOnClickListener {
                                radioItem.isChecked = !radioItem.isChecked
                                item.isChecked = radioItem.isChecked
                                notifyDataSetChanged()
                                SharedPreferencesHelper.getSharedPreferences(context).edit {
                                    putInt(
                                        "${AnswerActivity.ANSWER_QUESTION_ID}_${item.checkboxMultipleOptions?.questionID}",
                                        item.checkboxMultipleOptions?.questionID!!
                                    )
                                    val subQuestionId =
                                        item.checkboxMultipleOptions.subQuestionID ?: 0
                                    putInt(
                                        "${AnswerActivity.ANSWER_SUBQUESTION_ID}_${item.checkboxMultipleOptions.subQuestionID}",
                                        subQuestionId
                                    )
                                    putBoolean(
                                        "${AnswerActivity.ANSWER_CHECKBOX_MULTIPLE}_${item.checkboxMultipleOptions.questionID}_" +
                                                "${subQuestionId}_${position}",
                                        radioItem.isChecked
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}