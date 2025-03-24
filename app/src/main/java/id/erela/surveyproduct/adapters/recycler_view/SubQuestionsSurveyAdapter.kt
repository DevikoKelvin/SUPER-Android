package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.databinding.ListItemSubquestionsBinding
import id.erela.surveyproduct.fragments.AnswerFragment
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.objects.CheckboxMultipleItem
import id.erela.surveyproduct.objects.SubQuestionsItem

class SubQuestionsSurveyAdapter(
    private val subQuestions: List<SubQuestionsItem?>, private val context: Context
) : RecyclerView.Adapter<SubQuestionsSurveyAdapter.ViewHolder>() {
    private lateinit var checkboxMultipleAdapter: CheckboxMultipleSurveyAdapter
    private lateinit var onSubQuestionItemActionClickListener: OnSubQuestionItemActionClickListener

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemSubquestionsBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ListItemSubquestionsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).root
    )

    override fun getItemCount(): Int = subQuestions.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = subQuestions[position]

        with(holder) {
            binding.apply {
                subQuestions.text = "${position + 1}. ${item?.question}"
                answer.visibility = View.GONE
                val questionID =
                    SharedPreferencesHelper.getSharedPreferences(context).getInt(
                        "${AnswerFragment.ANSWER_QUESTION_ID}_${item?.questionID}",
                        0
                    )
                val subQuestionID =
                    SharedPreferencesHelper.getSharedPreferences(context).getInt(
                        "${AnswerFragment.ANSWER_SUBQUESTION_ID}_${item?.iD}",
                        0
                    )

                when (item?.questionType) {
                    "photo" -> {
                        answerFieldLayout.visibility = View.GONE
                        takePhotoButton.visibility = View.VISIBLE
                        multipleCheckboxAnswerRv.visibility = View.GONE
                        val photo =
                            SharedPreferencesHelper.getSharedPreferences(context).getString(
                                "${AnswerFragment.ANSWER_PHOTO}_${questionID}_${subQuestionID}",
                                null
                            )?.toUri()
                        if (questionID == item.iD) {
                            if (photo != null) {
                                imageAnswer.setImageURI(photo)
                                imageAnswer.visibility = View.VISIBLE
                            } else {
                                imageAnswer.visibility = View.GONE
                            }
                        }
                    }

                    "checkbox" -> {
                        answerFieldLayout.visibility = View.GONE
                        takePhotoButton.visibility = View.GONE
                        imageAnswer.visibility = View.GONE
                        val checkboxMultipleItem = ArrayList<CheckboxMultipleItem>()
                        for (i in item.checkboxOptions!!.indices) {
                            checkboxMultipleItem.add(
                                CheckboxMultipleItem(
                                    false, item.checkboxOptions[i]
                                )
                            )
                        }
                        checkboxMultipleAdapter =
                            CheckboxMultipleSurveyAdapter(checkboxMultipleItem, "checkbox", context)
                        multipleCheckboxAnswerRv.visibility = View.VISIBLE
                        multipleCheckboxAnswerRv.setItemViewCacheSize(1000)
                        multipleCheckboxAnswerRv.adapter = checkboxMultipleAdapter
                        multipleCheckboxAnswerRv.layoutManager = LinearLayoutManager(context)
                        multipleCheckboxAnswerRv.setHasFixedSize(true)
                    }

                    "multiple" -> {
                        answerFieldLayout.visibility = View.GONE
                        takePhotoButton.visibility = View.GONE
                        imageAnswer.visibility = View.GONE
                        val checkboxMultipleItem = ArrayList<CheckboxMultipleItem>()
                        for (i in item.checkboxOptions!!.indices) {
                            checkboxMultipleItem.add(
                                CheckboxMultipleItem(
                                    false, item.checkboxOptions[i]
                                )
                            )
                        }
                        checkboxMultipleAdapter =
                            CheckboxMultipleSurveyAdapter(checkboxMultipleItem, "multiple", context)
                        multipleCheckboxAnswerRv.visibility = View.VISIBLE
                        multipleCheckboxAnswerRv.setItemViewCacheSize(1000)
                        multipleCheckboxAnswerRv.adapter = checkboxMultipleAdapter
                        multipleCheckboxAnswerRv.layoutManager = LinearLayoutManager(context)
                        multipleCheckboxAnswerRv.setHasFixedSize(true)
                    }

                    else -> {
                        answerFieldLayout.visibility = View.VISIBLE
                        takePhotoButton.visibility = View.GONE
                        multipleCheckboxAnswerRv.visibility = View.GONE
                        imageAnswer.visibility = View.GONE
                        val answer =
                            SharedPreferencesHelper.getSharedPreferences(context).getString(
                                "${AnswerFragment.ANSWER_TEXT}_${questionID}_${subQuestionID}",
                                ""
                            )
                        if (questionID == item!!.questionID) {
                            if (subQuestionID == item.iD) {
                                if (answer != null) {
                                    answerField.setText(answer)
                                }
                            }
                        }
                    }
                }

                answerField.addTextChangedListener { editable ->
                    val answer = editable.toString()
                    SharedPreferencesHelper.getSharedPreferences(context).edit {
                        putInt(
                            "${AnswerFragment.ANSWER_QUESTION_ID}_${item.questionID}",
                            item.questionID!!.toInt()
                        )
                        putInt(
                            "${AnswerFragment.ANSWER_SUBQUESTION_ID}_${item.iD}",
                            item.iD!!.toInt()
                        )
                        putString(
                            "${AnswerFragment.ANSWER_TEXT}_${item.questionID}_${item.iD}",
                            answer
                        )
                    }
                }

                takePhotoButton.setOnClickListener {
                    onSubQuestionItemActionClickListener.onTakePhotoButtonClick(
                        position,
                        item.questionID!!.toInt(),
                        item.iD
                    )
                }
            }
        }
    }

    fun setOnSubQuestionItemActionClickListener(onSubQuestionItemActionClickListener: OnSubQuestionItemActionClickListener) {
        this.onSubQuestionItemActionClickListener = onSubQuestionItemActionClickListener
    }

    interface OnSubQuestionItemActionClickListener {
        fun onTakePhotoButtonClick(position: Int, questionID: Int, subQuestionID: Int?)
    }
}