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
import id.erela.surveyproduct.activities.AnswerActivity
import id.erela.surveyproduct.databinding.ListItemQuestionsBinding
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.objects.CheckboxMultipleItem
import id.erela.surveyproduct.objects.QuestionsItem

class QuestionSurveyAdapter(
    private val questionsArrayList: ArrayList<QuestionsItem>,
    private val context: Context
) : RecyclerView.Adapter<QuestionSurveyAdapter.ViewHolder>(),
    SubQuestionsSurveyAdapter.OnSubQuestionItemActionClickListener {
    private lateinit var adapter: SubQuestionsSurveyAdapter
    private lateinit var checkboxMultipleAdapter: CheckboxMultipleSurveyAdapter
    private lateinit var onQuestionItemActionClickListener: OnQuestionItemActionClickListener

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemQuestionsBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ListItemQuestionsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).root
    )

    override fun getItemCount(): Int = questionsArrayList.size

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questionsArrayList[position]

        with(holder) {
            binding.apply {
                questionNumbers.text = "Question ${position + 1}"
                questions.text = item.question

                answerContainer.visibility = View.VISIBLE
                answer.visibility = View.GONE
                val questionID =
                    SharedPreferencesHelper.getSharedPreferences(context).getInt(
                        "${AnswerActivity.ANSWER_QUESTION_ID}_${item.iD}",
                        0
                    )
                val subQuestionID =
                    SharedPreferencesHelper.getSharedPreferences(context).getInt(
                        "${AnswerActivity.ANSWER_SUBQUESTION_ID}_0",
                        0
                    )

                when (item.questionType) {
                    "sub" -> {
                        answerFieldLayout.visibility = View.GONE
                        takePhotoButton.visibility = View.GONE
                        multipleCheckboxAnswerRv.visibility = View.GONE
                        imageAnswer.visibility = View.GONE
                    }

                    "photo" -> {
                        answerFieldLayout.visibility = View.GONE
                        takePhotoButton.visibility = View.VISIBLE
                        multipleCheckboxAnswerRv.visibility = View.GONE
                        val photo =
                            SharedPreferencesHelper.getSharedPreferences(context).getString(
                                "${AnswerActivity.ANSWER_PHOTO}_${questionID}_${subQuestionID}",
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
                                    false,
                                    item.checkboxOptions[i]
                                )
                            )
                        }
                        checkboxMultipleAdapter =
                            CheckboxMultipleSurveyAdapter(checkboxMultipleItem, "checkbox", context)
                        multipleCheckboxAnswerRv.visibility = View.VISIBLE
                        multipleCheckboxAnswerRv.setItemViewCacheSize(1000)
                        multipleCheckboxAnswerRv.adapter = checkboxMultipleAdapter
                        multipleCheckboxAnswerRv.layoutManager =
                            LinearLayoutManager(context)
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
                                    false,
                                    item.checkboxOptions[i]
                                )
                            )
                        }
                        checkboxMultipleAdapter =
                            CheckboxMultipleSurveyAdapter(checkboxMultipleItem, "multiple", context)
                        multipleCheckboxAnswerRv.visibility = View.VISIBLE
                        multipleCheckboxAnswerRv.setItemViewCacheSize(1000)
                        multipleCheckboxAnswerRv.adapter = checkboxMultipleAdapter
                        multipleCheckboxAnswerRv.layoutManager =
                            LinearLayoutManager(context)
                        multipleCheckboxAnswerRv.setHasFixedSize(true)
                    }

                    else -> {
                        answerFieldLayout.visibility = View.VISIBLE
                        takePhotoButton.visibility = View.GONE
                        multipleCheckboxAnswerRv.visibility = View.GONE
                        imageAnswer.visibility = View.GONE
                        val answer =
                            SharedPreferencesHelper.getSharedPreferences(context).getString(
                                "${AnswerActivity.ANSWER_TEXT}_${item.iD}_0",
                                ""
                            )
                        if (questionID == item.iD) {
                            answerField.setText(answer)
                        }
                    }
                }

                answerField.addTextChangedListener { editable ->
                    val answer = editable.toString()
                    SharedPreferencesHelper.getSharedPreferences(context).edit {
                        putInt(
                            "${AnswerActivity.ANSWER_QUESTION_ID}_${item.iD}",
                            item.iD!!
                        )
                        putInt("${AnswerActivity.ANSWER_SUBQUESTION_ID}_0", 0)
                        putString("${AnswerActivity.ANSWER_TEXT}_${item.iD}_0", answer)
                    }
                }

                takePhotoButton.setOnClickListener {
                    onQuestionItemActionClickListener.onTakePhotoButtonClick(
                        position,
                        item.iD!!.toInt(),
                        null
                    )
                }

                if (item.subQuestions != null) {
                    subQuestionsRv.visibility = View.VISIBLE
                    adapter = SubQuestionsSurveyAdapter(item.subQuestions, context).also {
                        with(it) {
                            setOnSubQuestionItemActionClickListener(this@QuestionSurveyAdapter)
                        }
                    }
                    subQuestionsRv.adapter = adapter
                    subQuestionsRv.layoutManager = LinearLayoutManager(context)
                    subQuestionsRv.setItemViewCacheSize(1000)
                    subQuestionsRv.setHasFixedSize(true)
                } else {
                    subQuestionsRv.visibility = View.GONE
                }
            }
        }
    }

    fun setOnQuestionItemActionClickListener(onQuestionItemActionClickListener: OnQuestionItemActionClickListener) {
        this.onQuestionItemActionClickListener = onQuestionItemActionClickListener
    }

    interface OnQuestionItemActionClickListener {
        fun onTakePhotoButtonClick(position: Int, questionID: Int, subQuestionID: Int?)
    }

    override fun onTakePhotoButtonClick(position: Int, questionID: Int, subQuestionID: Int?) {
        onQuestionItemActionClickListener.onTakePhotoButtonClick(
            position,
            questionID,
            subQuestionID
        )
    }
}