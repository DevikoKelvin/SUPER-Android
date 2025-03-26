package id.erela.surveyproduct.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import id.erela.surveyproduct.adapters.recycler_view.QuestionsAnswerAdapter
import id.erela.surveyproduct.databinding.ActivitySurveyDetailBinding
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.objects.AnswerHistoryResponse
import id.erela.surveyproduct.objects.CheckInOutHistoryItem
import id.erela.surveyproduct.objects.QuestionAnswersItem
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SurveyDetailActivity : AppCompatActivity() {
    private val binding: ActivitySurveyDetailBinding by lazy {
        ActivitySurveyDetailBinding.inflate(layoutInflater)
    }
    private lateinit var surveyItem: CheckInOutHistoryItem
    private lateinit var adapter: QuestionsAnswerAdapter
    private val questionsAnswerList = ArrayList<QuestionAnswersItem>()

    companion object {
        private const val DATA = "DATA"

        fun start(context: Context, item: CheckInOutHistoryItem) {
            context.startActivity(
                Intent(
                    context, SurveyDetailActivity::class.java
                ).also {
                    with(it) {
                        putExtra(DATA, item)
                    }
                })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        init()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            surveyItem =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getSerializableExtra(
                    DATA,
                    CheckInOutHistoryItem::class.java
                )!!
                else intent.getSerializableExtra(DATA) as CheckInOutHistoryItem

            surveyId.text = "Survey ${surveyItem.surveyID}"
            outletName.text = surveyItem.outletName
            outletAddress.text = surveyItem.outletAddress

            adapter = QuestionsAnswerAdapter(questionsAnswerList, this@SurveyDetailActivity)
            answeredQuestionRv.setItemViewCacheSize(1000)
            answeredQuestionRv.adapter = adapter
            answeredQuestionRv.setHasFixedSize(true)
            answeredQuestionRv.layoutManager = LinearLayoutManager(this@SurveyDetailActivity)

            getAnswerListHistory()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAnswerListHistory() {
        binding.apply {
            try {
                AppAPI.superEndpoint.showAnswerHistory(
                    surveyItem.answerGroupID!!
                ).enqueue(object : Callback<AnswerHistoryResponse> {
                    override fun onResponse(
                        call: Call<AnswerHistoryResponse>, response: Response<AnswerHistoryResponse>
                    ) {
                        questionsAnswerList.clear()
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                val result = response.body()!!
                                when (result.code) {
                                    1 -> {
                                        if (result.data != null) {
                                            for (i in result.data.indices) {
                                                questionsAnswerList.add(
                                                    QuestionAnswersItem(
                                                        result.data[i]?.answer,
                                                        result.data[i]?.questionID,
                                                        result.data[i]?.questionType,
                                                        result.data[i]?.question,
                                                        result.data[i]?.subQuestions
                                                    )
                                                )
                                            }
                                            adapter.notifyDataSetChanged()
                                        } else {
                                            answeredQuestionRv.visibility = View.GONE
                                        }
                                    }

                                    0 -> {
                                        answeredQuestionRv.visibility = View.GONE
                                    }
                                }
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<AnswerHistoryResponse>, throwable: Throwable
                    ) {
                        Log.e("onFailure", throwable.message.toString())
                        answeredQuestionRv.visibility = View.GONE
                        throwable.printStackTrace()
                    }
                })
            } catch (jsonException: JSONException) {
                jsonException.printStackTrace()
            }
        }
    }
}