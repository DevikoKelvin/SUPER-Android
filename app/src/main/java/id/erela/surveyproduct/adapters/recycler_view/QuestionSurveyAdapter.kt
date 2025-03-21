package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.databinding.ListItemQuestionsBinding
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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questionsArrayList[position]

        with(holder) {
            binding.apply {
                questionNumbers.text = "Question ${position + 1}"
                questions.text = item.question

                answerContainer.visibility = View.VISIBLE
                answer.visibility = View.GONE

                when (item.questionType) {
                    "sub" -> {
                        answerFieldLayout.visibility = View.GONE
                        takePhotoButton.visibility = View.GONE
                    }

                    "photo" -> {
                        answerFieldLayout.visibility = View.GONE
                        takePhotoButton.visibility = View.VISIBLE
                    }

                    "checkbox" -> {
                        answerFieldLayout.visibility = View.GONE
                        takePhotoButton.visibility = View.GONE
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
                            CheckboxMultipleSurveyAdapter(checkboxMultipleItem, "checkbox")
                        multipleCheckboxAnswerRv.visibility = View.VISIBLE
                        multipleCheckboxAnswerRv.adapter = checkboxMultipleAdapter
                        multipleCheckboxAnswerRv.layoutManager =
                            LinearLayoutManager(context)
                        multipleCheckboxAnswerRv.setHasFixedSize(true)
                    }

                    "multiple" -> {
                        answerFieldLayout.visibility = View.GONE
                        takePhotoButton.visibility = View.GONE
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
                            CheckboxMultipleSurveyAdapter(checkboxMultipleItem, "multiple")
                        multipleCheckboxAnswerRv.visibility = View.VISIBLE
                        multipleCheckboxAnswerRv.adapter = checkboxMultipleAdapter
                        multipleCheckboxAnswerRv.layoutManager =
                            LinearLayoutManager(context)
                        multipleCheckboxAnswerRv.setHasFixedSize(true)
                    }

                    else -> {
                        answerFieldLayout.visibility = View.VISIBLE
                        takePhotoButton.visibility = View.GONE
                    }
                }

                takePhotoButton.setOnClickListener {
                    onQuestionItemActionClickListener.onTakePhotoButtonClick(position)
                }

                if (item.subQuestions != null) {
                    subQuestionsRv.visibility = View.VISIBLE
                    adapter = SubQuestionsSurveyAdapter(item.subQuestions, context)
                    subQuestionsRv.adapter = adapter
                    subQuestionsRv.layoutManager = LinearLayoutManager(context)
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
        fun onTakePhotoButtonClick(position: Int)
    }

    override fun onTakePhotoButtonClick(position: Int) {
        onQuestionItemActionClickListener.onTakePhotoButtonClick(position)
    }
}