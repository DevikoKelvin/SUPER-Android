package id.erela.surveyproduct.adapters.recycler_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.erela.surveyproduct.databinding.ListItemQuestionsBinding
import id.erela.surveyproduct.objects.QuestionsItem

class QuestionsAdapter(
    private val questions: ArrayList<QuestionsItem>,
    private val context: Context
) :
    RecyclerView.Adapter<QuestionsAdapter.ViewHolder>() {
    private lateinit var adapter: SubQuestionsAdapter

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
                if (item.subQuestions != null) {
                    subQuestionsRv.visibility = View.VISIBLE
                    adapter = SubQuestionsAdapter(item.subQuestions)
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