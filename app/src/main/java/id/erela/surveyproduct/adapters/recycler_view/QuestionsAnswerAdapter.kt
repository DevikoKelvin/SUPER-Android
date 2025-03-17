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
import id.erela.surveyproduct.databinding.ListItemQuestionsBinding
import id.erela.surveyproduct.objects.QuestionAnswerItem

class QuestionsAnswerAdapter(
    private val questions: ArrayList<QuestionAnswerItem>,
    private val context: Context
) :
    RecyclerView.Adapter<QuestionsAnswerAdapter.ViewHolder>() {
    private lateinit var adapter: SubQuestionsAnswerAdapter

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListItemQuestionsBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        ListItemQuestionsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        ).root
    )

    override fun getItemCount(): Int = questions.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = questions[position]

        with(holder) {
            binding.apply {
                questionNumbers.text = "Question ${position + 1}"
                questions.text = item.question

                if (item.answer != null) {
                    answerContainer.visibility = View.VISIBLE
                    answer.text = item.answer
                    if (item.questionType != "photo") {
                        answer.visibility = View.VISIBLE
                        imageAnswer.visibility = View.GONE
                    } else {
                        answer.visibility = View.GONE
                        imageAnswer.visibility = View.VISIBLE
                        Glide.with(context)
                            .load(BuildConfig.IMAGE_URL + item.answer)
                            .into(imageAnswer)
                    }
                } else {
                    answerContainer.visibility = View.GONE
                }

                if (item.subQuestions != null) {
                    subQuestionsRv.visibility = View.VISIBLE
                    adapter = SubQuestionsAnswerAdapter(context, item.subQuestions)
                    subQuestionsRv.adapter = adapter
                    subQuestionsRv.layoutManager = LinearLayoutManager(context)
                    subQuestionsRv.setHasFixedSize(true)
                } else {
                    subQuestionsRv.visibility = View.GONE
                }
            }
        }
    }
}