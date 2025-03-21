package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.databinding.ListItemSubquestionsBinding
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

                when (item?.questionType) {
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
                                    false, item.checkboxOptions[i]
                                )
                            )
                        }
                        checkboxMultipleAdapter =
                            CheckboxMultipleSurveyAdapter(checkboxMultipleItem, "checkbox")
                        multipleCheckboxAnswerRv.adapter = checkboxMultipleAdapter
                        multipleCheckboxAnswerRv.layoutManager = LinearLayoutManager(context)
                        multipleCheckboxAnswerRv.setHasFixedSize(true)
                    }

                    "multiple" -> {
                        answerFieldLayout.visibility = View.GONE
                        takePhotoButton.visibility = View.GONE
                        val checkboxMultipleItem = ArrayList<CheckboxMultipleItem>()
                        for (i in item.checkboxOptions!!.indices) {
                            checkboxMultipleItem.add(
                                CheckboxMultipleItem(
                                    false, item.checkboxOptions[i]
                                )
                            )
                        }
                        checkboxMultipleAdapter =
                            CheckboxMultipleSurveyAdapter(checkboxMultipleItem, "multiple")
                        multipleCheckboxAnswerRv.adapter = checkboxMultipleAdapter
                        multipleCheckboxAnswerRv.layoutManager = LinearLayoutManager(context)
                        multipleCheckboxAnswerRv.setHasFixedSize(true)
                    }

                    else -> {
                        answerFieldLayout.visibility = View.VISIBLE
                        takePhotoButton.visibility = View.GONE
                    }
                }

                takePhotoButton.setOnClickListener {
                    onSubQuestionItemActionClickListener.onTakePhotoButtonClick(position)
                }
            }
        }
    }

    fun setOnSubQuestionItemActionClickListener(onSubQuestionItemActionClickListener: OnSubQuestionItemActionClickListener) {
        this.onSubQuestionItemActionClickListener = onSubQuestionItemActionClickListener
    }

    interface OnSubQuestionItemActionClickListener {
        fun onTakePhotoButtonClick(position: Int)
    }
}