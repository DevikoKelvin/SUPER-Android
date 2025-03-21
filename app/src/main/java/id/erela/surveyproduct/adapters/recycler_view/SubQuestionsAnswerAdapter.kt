package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.erela.surveyproduct.BuildConfig
import id.erela.surveyproduct.databinding.ListItemSubquestionsBinding
import id.erela.surveyproduct.objects.SubQuestionAnswersItem

class SubQuestionsAnswerAdapter(
    private val context: Context,
    private val subQuestions: List<SubQuestionAnswersItem?>,
) : RecyclerView.Adapter<SubQuestionsAnswerAdapter.ViewHolder>() {
    private lateinit var checkboxMultipleAdapter: CheckboxMultipleViewAdapter

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


                if (item?.answer != null) {
                    if (item.questionType == "photo") {
                        answer.visibility = View.GONE
                        imageAnswer.visibility = View.VISIBLE
                        if (item.answer.isNotEmpty()) {
                            Glide.with(context).load(BuildConfig.IMAGE_URL + item.answer[0]?.answer)
                                .into(imageAnswer)
                        }
                    } else {
                        if (item.answer.size > 1) {
                            checkboxMultipleAdapter = CheckboxMultipleViewAdapter(item.answer)
                            multipleCheckboxAnswerRv.adapter = checkboxMultipleAdapter
                            multipleCheckboxAnswerRv.layoutManager = LinearLayoutManager(context)
                            multipleCheckboxAnswerRv.setHasFixedSize(true)
                            answer.visibility = View.GONE
                            imageAnswer.visibility = View.GONE
                            multipleCheckboxAnswerRv.visibility = View.VISIBLE
                        } else {
                            multipleCheckboxAnswerRv.visibility = View.GONE
                            answerContainer.visibility = View.VISIBLE
                            if (item.answer.isNotEmpty()) {
                                answer.visibility = View.VISIBLE
                                answer.text = if (item.answer.size == 1) item.answer[0]?.answer
                                else ""
                            } else {
                                answer.visibility = View.GONE
                            }
                            imageAnswer.visibility = View.GONE
                        }
                    }
                } else {
                    answerContainer.visibility = View.GONE
                }
            }
        }
    }
}