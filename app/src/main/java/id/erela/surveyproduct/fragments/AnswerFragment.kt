package id.erela.surveyproduct.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import id.erela.surveyproduct.R
import id.erela.surveyproduct.activities.StartSurveyActivity
import id.erela.surveyproduct.adapters.recycler_view.QuestionSurveyAdapter
import id.erela.surveyproduct.databinding.FragmentAnswerBinding
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.QuestionsItem
import id.erela.surveyproduct.objects.SurveyListResponse
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnswerFragment : Fragment(), QuestionSurveyAdapter.OnQuestionItemActionClickListener {
    private var binding: FragmentAnswerBinding? = null
    private val activity: StartSurveyActivity by lazy {
        requireActivity() as StartSurveyActivity
    }
    private val surveyQuestionsList = ArrayList<QuestionsItem>()
    private lateinit var adapter: QuestionSurveyAdapter
    private var isInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnswerBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            adapter = QuestionSurveyAdapter(surveyQuestionsList, requireContext())
            answerFieldRv.adapter = adapter
            answerFieldRv.setHasFixedSize(true)
            answerFieldRv.layoutManager = LinearLayoutManager(context)

            if (!isInitialized)
                getSurveyQuestions()
        }
    }

    fun saveState(outState: Bundle) {
        outState.apply {
            putParcelableArrayList("surveyQuestions", ArrayList(surveyQuestionsList))
            binding?.answerFieldRv?.layoutManager?.let { layoutManager ->
                if (layoutManager is LinearLayoutManager) {
                    putInt("scrollPosition", layoutManager.findFirstVisibleItemPosition())
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun restoreState(savedInstanceState: Bundle) {
        savedInstanceState.getParcelableArrayList<QuestionsItem>("surveyQuestions")?.let {
            surveyQuestionsList.clear()
            surveyQuestionsList.addAll(it)
            if (::adapter.isInitialized) {
                adapter.notifyDataSetChanged()
            }
        }

        // Restore scroll position after view is created
        view?.post {
            savedInstanceState.getInt("scrollPosition", 0).let { position ->
                binding?.answerFieldRv?.layoutManager?.scrollToPosition(position)
            }
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun getSurveyQuestions() {
        binding.apply {
            try {
                AppAPI.superEndpoint.showAllSurveys()
                    .enqueue(object : Callback<SurveyListResponse> {
                        override fun onResponse(
                            call: Call<SurveyListResponse>,
                            response: Response<SurveyListResponse>
                        ) {
                            isInitialized = true
                            if (response.isSuccessful) {
                                if (response.body() != null) {
                                    val result = response.body()
                                    when (result?.code) {
                                        1 -> {
                                            for (item in result.data!!) {
                                                surveyQuestionsList.add(item!!)
                                            }
                                            adapter.notifyDataSetChanged()
                                        }

                                        0 -> {
                                            CustomToast.getInstance(requireContext())
                                                .setMessage("Something went wrong, please try again later")
                                                .setBackgroundColor(
                                                    ContextCompat.getColor(
                                                        requireContext(),
                                                        R.color.custom_toast_background_failed
                                                    )
                                                )
                                                .setFontColor(
                                                    ContextCompat.getColor(
                                                        requireContext(),
                                                        R.color.custom_toast_font_failed
                                                    )
                                                ).show()
                                        }
                                    }
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call<SurveyListResponse>,
                            throwable: Throwable
                        ) {
                            isInitialized = false
                            Log.e("ERROR", throwable.toString())
                            throwable.printStackTrace()
                            activity.finish()
                        }
                    })
            } catch (jsonException: JSONException) {
                isInitialized = false
                Log.e("ERROR", jsonException.toString())
                jsonException.printStackTrace()
                activity.finish()
            }
        }
    }

    override fun onTakePhotoButtonClick(position: Int) {
    }
}