package id.erela.surveyproduct.fragments

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import id.erela.surveyproduct.R
import id.erela.surveyproduct.activities.StartSurveyActivity
import id.erela.surveyproduct.adapters.recycler_view.QuestionSurveyAdapter
import id.erela.surveyproduct.databinding.FragmentAnswerBinding
import id.erela.surveyproduct.helpers.PermissionHelper
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.QuestionsItem
import id.erela.surveyproduct.objects.SurveyListResponse
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("NotifyDataSetChanged")
class AnswerFragment : Fragment(), QuestionSurveyAdapter.OnQuestionItemActionClickListener {
    private var binding: FragmentAnswerBinding? = null
    private val activity: StartSurveyActivity by lazy {
        requireActivity() as StartSurveyActivity
    }

    /*private val surveyQuestionsList = ArrayList<QuestionsItem>()*/
    private lateinit var adapter: QuestionSurveyAdapter
    private var cameraCaptureFileName: String = ""
    private var imageUri: Uri? = null
    private var questionID: Int? = null
    private var subQuestionID: Int? = null
    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        with(it) {
            binding?.apply {
                if (resultCode == RESULT_OK) {
                    SharedPreferencesHelper.getSharedPreferences(requireContext()).edit {
                        putInt("${ANSWER_QUESTION_ID}_${questionID}", questionID!!)
                        putInt("${ANSWER_SUBQUESTION_ID}_${subQuestionID}", subQuestionID ?: 0)
                        putString(
                            "${ANSWER_PHOTO}_${questionID}_${subQuestionID ?: 0}",
                            imageUri.toString()
                        )
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    companion object {
        const val ANSWER_QUESTION_ID = "ANSWER_QUESTION_ID"
        const val ANSWER_SUBQUESTION_ID = "ANSWER_SUBQUESTION_ID"
        const val ANSWER_PHOTO = "ANSWER_PHOTO"
        const val ANSWER_CHECKBOX_MULTIPLE = "ANSWER_CHECKBOX_MULTIPLE"
        const val ANSWER_TEXT = "ANSWER_TEXT"
        var questionIdArray = ArrayList<Int>()
        var subQuestionIdArray = ArrayList<Int?>()
        val surveyQuestionsList = ArrayList<QuestionsItem>()

        fun clearAnswerData(context: Context) {
            for (id in questionIdArray) {
                for (subId in subQuestionIdArray) {
                    SharedPreferencesHelper.getSharedPreferences(context).edit {
                        remove("${ANSWER_QUESTION_ID}_${id}")
                        remove("${ANSWER_SUBQUESTION_ID}_${subId}")
                        remove("${ANSWER_PHOTO}_${id}_${subId}")
                        remove("${ANSWER_CHECKBOX_MULTIPLE}_${id}_${subId}")
                        remove("${ANSWER_TEXT}_${id}_${subId}")
                    }
                }
            }
        }

        fun validateAnswer(context: Context): Boolean {
            val sharedPreferences = SharedPreferencesHelper.getSharedPreferences(context)
            var allQuestionsAnswered = true

            for (questions in surveyQuestionsList) {
                if (questions.subQuestions != null) {
                    for (subQuestions in questions.subQuestions) {
                        val hasSubPhoto = sharedPreferences.getString(
                            "${ANSWER_PHOTO}_${questions.iD}_${subQuestions?.iD}",
                            null
                        )
                        val hasSubText = sharedPreferences.getString(
                            "${ANSWER_TEXT}_${questions.iD}_${subQuestions?.iD}",
                            null
                        )
                        val hasSubCheckbox = sharedPreferences.getBoolean(
                            "${ANSWER_CHECKBOX_MULTIPLE}_${questions.iD}_${subQuestions?.iD}",
                            false
                        )

                        if (hasSubPhoto == null && hasSubText.isNullOrBlank() && !hasSubCheckbox) {
                            allQuestionsAnswered = false
                            break
                        }
                    }
                }
            }

            return allQuestionsAnswered
        }
    }

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
            adapter = QuestionSurveyAdapter(surveyQuestionsList, requireContext()).also {
                with(it) {
                    setOnQuestionItemActionClickListener(this@AnswerFragment)
                }
            }
            answerFieldRv.adapter = adapter
            answerFieldRv.setItemViewCacheSize(1000)
            answerFieldRv.setHasFixedSize(true)
            answerFieldRv.layoutManager = LinearLayoutManager(context)

            getSurveyQuestions()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionHelper.REQUEST_CODE_CAMERA) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PERMISSION_GRANTED) {
                    if (VERSION.SDK_INT <= VERSION_CODES.P) {
                        if (PermissionHelper.isPermissionGranted(
                                requireActivity(),
                                PermissionHelper.CAMERA
                            )
                        ) {
                            openCamera()
                        } else {
                            PermissionHelper.requestPermission(
                                requireActivity(),
                                arrayOf(PermissionHelper.CAMERA),
                                PermissionHelper.REQUEST_CODE_CAMERA
                            )
                        }
                    } else {
                        openCamera()
                    }
                }
            }
        }
    }

    private fun getSurveyQuestions() {
        binding.apply {
            try {
                AppAPI.superEndpoint.showAllSurveys()
                    .enqueue(object : Callback<SurveyListResponse> {
                        override fun onResponse(
                            call: Call<SurveyListResponse>,
                            response: Response<SurveyListResponse>
                        ) {
                            if (response.isSuccessful) {
                                if (response.body() != null) {
                                    val result = response.body()
                                    when (result?.code) {
                                        1 -> {
                                            questionIdArray.clear()
                                            subQuestionIdArray.clear()
                                            for (item in result.data!!) {
                                                surveyQuestionsList.add(item!!)
                                                questionIdArray.add(item.iD!!)
                                                if (item.subQuestions != null) {
                                                    for (subItem in item.subQuestions) {
                                                        subQuestionIdArray.add(subItem?.iD)
                                                    }
                                                } else {
                                                    subQuestionIdArray.add(0)
                                                }
                                            }
                                            adapter.notifyDataSetChanged()
                                            Log.e("Question ID Array", questionIdArray.toString())
                                            Log.e(
                                                "Sub Question ID Array",
                                                subQuestionIdArray.toString()
                                            )
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
                            Log.e("ERROR", throwable.toString())
                            throwable.printStackTrace()
                            activity.finish()
                        }
                    })
            } catch (jsonException: JSONException) {
                Log.e("ERROR", jsonException.toString())
                jsonException.printStackTrace()
                activity.finish()
            }
        }
    }

    override fun onTakePhotoButtonClick(position: Int, questionID: Int, subQuestionID: Int?) {
        this.questionID = questionID
        this.subQuestionID = subQuestionID
        if (PermissionHelper.isPermissionGranted(
                requireActivity(),
                PermissionHelper.CAMERA
            )
        ) {
            openCamera()
        } else {
            PermissionHelper.requestPermission(
                requireActivity(),
                arrayOf(PermissionHelper.CAMERA),
                PermissionHelper.REQUEST_CODE_CAMERA
            )
        }
    }

    private fun openCamera() {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.forLanguageTag("id-ID")).format(Date())
        cameraCaptureFileName = "Super_Answer_${questionID}-${subQuestionID}_${timeStamp}.jpg"
        imageUri = context?.contentResolver?.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().also {
                with(it) {
                    put(MediaStore.Images.Media.TITLE, cameraCaptureFileName)
                    put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera")
                }
            }
        )!!

        cameraLauncher.launch(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                with(it) {
                    putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                }
            }
        )
    }
}