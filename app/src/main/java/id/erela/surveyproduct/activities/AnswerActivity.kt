package id.erela.surveyproduct.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import id.erela.surveyproduct.R
import id.erela.surveyproduct.adapters.recycler_view.QuestionSurveyAdapter
import id.erela.surveyproduct.databinding.ActivityAnswerBinding
import id.erela.surveyproduct.helpers.PermissionHelper
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.SurveyAnswer
import id.erela.surveyproduct.objects.SurveyListResponse
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("NotifyDataSetChanged")
class AnswerActivity : AppCompatActivity(),
    QuestionSurveyAdapter.OnQuestionItemActionClickListener {
    private val binding: ActivityAnswerBinding by lazy {
        ActivityAnswerBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: QuestionSurveyAdapter
    private var cameraCaptureFileName: String = ""
    private var imageUri: Uri? = null
    private var questionID: Int? = null
    private var subQuestionID: Int? = null
    private val sharedPreferences: SharedPreferences by lazy {
        SharedPreferencesHelper.getSharedPreferences(applicationContext)
    }
    private val answers: ArrayList<SurveyAnswer> = ArrayList()
    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        with(it) {
            binding.apply {
                if (resultCode == RESULT_OK) {
                    sharedPreferences.edit {
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
        const val ANSWER_UPLOADED = "ANSWER_UPLOADED"
        const val ANSWER_QUESTION_ID = "ANSWER_QUESTION_ID"
        const val ANSWER_SUBQUESTION_ID = "ANSWER_SUBQUESTION_ID"
        const val ANSWER_PHOTO = "ANSWER_PHOTO"
        const val ANSWER_CHECKBOX_MULTIPLE = "ANSWER_CHECKBOX_MULTIPLE"
        const val ANSWER_TEXT = "ANSWER_TEXT"
        const val ANSWER_ARRAY = "ANSWER_ARRAY"
        @SuppressLint("StaticFieldLeak")
        var activity: Activity? = null

        fun start(context: Context, outletID: Int, photoIn: Uri?, latIn: Double, longIn: Double) {
            context.startActivity(
                Intent(context, AnswerActivity::class.java).also {
                    with(it) {
                        putExtra(CheckInActivity.SELECTED_OUTLET, outletID)
                        putExtra(CheckInActivity.IMAGE_URI, photoIn.toString())
                        putExtra(CheckInActivity.LATITUDE, latIn)
                        putExtra(CheckInActivity.LONGITUDE, longIn)
                    }
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        init()
    }

    override fun onResume() {
        super.onResume()
        activity = this
    }

    override fun onDestroy() {
        super.onDestroy()
        activity = null
    }

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
                                this@AnswerActivity,
                                PermissionHelper.CAMERA
                            )
                        ) {
                            openCamera()
                        } else {
                            PermissionHelper.requestPermission(
                                this@AnswerActivity,
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

    private fun init() {
        binding.apply {
            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            val isAnswerUploaded = sharedPreferences.getBoolean(ANSWER_UPLOADED, false)

            if (isAnswerUploaded) {
                CheckOutActivity.start(
                    this@AnswerActivity,
                    intent.getIntExtra(CheckInActivity.SELECTED_OUTLET, 0),
                    intent.getStringExtra(CheckInActivity.IMAGE_URI)?.toUri(),
                    intent.getDoubleExtra(CheckInActivity.LATITUDE, 0.0),
                    intent.getDoubleExtra(CheckInActivity.LONGITUDE, 0.0),
                    answers
                )
                finish()
            }

            adapter = QuestionSurveyAdapter(CheckInActivity.surveyQuestionsList, this@AnswerActivity).also {
                with(it) {
                    setOnQuestionItemActionClickListener(this@AnswerActivity)
                }
            }
            answerFieldRv.adapter = adapter
            answerFieldRv.setItemViewCacheSize(1000)
            answerFieldRv.setHasFixedSize(true)
            answerFieldRv.layoutManager = LinearLayoutManager(applicationContext)

            getSurveyQuestions()

            nextButton.setOnClickListener {
                if (validateAnswer(applicationContext)) {
                    with(intent) {
                        CheckOutActivity.start(
                            this@AnswerActivity,
                            getIntExtra(CheckInActivity.SELECTED_OUTLET, 0),
                            getStringExtra(CheckInActivity.IMAGE_URI)?.toUri(),
                            getDoubleExtra(CheckInActivity.LATITUDE, 0.0),
                            getDoubleExtra(CheckInActivity.LONGITUDE, 0.0),
                            answers
                        )
                    }
                } else {
                    CustomToast.getInstance(applicationContext)
                        .setMessage("Please answer all questions")
                        .setBackgroundColor(
                            ContextCompat.getColor(
                                this@AnswerActivity,
                                R.color.custom_toast_background_failed
                            )
                        )
                        .setFontColor(
                            ContextCompat.getColor(
                                this@AnswerActivity,
                                R.color.custom_toast_font_failed
                            )
                        ).show()
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
                                            CheckInActivity.surveyQuestionsList.clear()
                                            CheckInActivity.questionIdArray.clear()
                                            CheckInActivity.subQuestionIdArray.clear()
                                            for (item in result.data!!) {
                                                CheckInActivity.surveyQuestionsList.add(item!!)
                                                if (item.subQuestions != null) {
                                                    for (subItem in item.subQuestions) {
                                                        CheckInActivity.questionIdArray.add(item.iD!!)
                                                        CheckInActivity.subQuestionIdArray.add(subItem?.iD)
                                                    }
                                                } else {
                                                    CheckInActivity.questionIdArray.add(item.iD!!)
                                                    CheckInActivity.subQuestionIdArray.add(0)
                                                }
                                            }
                                            adapter.notifyDataSetChanged()
                                        }

                                        0 -> {
                                            CustomToast.getInstance(applicationContext)
                                                .setMessage("Something went wrong, please try again later")
                                                .setBackgroundColor(
                                                    ContextCompat.getColor(
                                                        this@AnswerActivity,
                                                        R.color.custom_toast_background_failed
                                                    )
                                                )
                                                .setFontColor(
                                                    ContextCompat.getColor(
                                                        this@AnswerActivity,
                                                        R.color.custom_toast_font_failed
                                                    )
                                                ).show()
                                            if (CheckInActivity.activity != null)
                                                CheckInActivity.activity?.finish()
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
                            if (CheckInActivity.activity != null)
                                CheckInActivity.activity?.finish()
                        }
                    })
            } catch (jsonException: JSONException) {
                Log.e("ERROR", jsonException.toString())
                jsonException.printStackTrace()
                if (CheckInActivity.activity != null)
                    CheckInActivity.activity?.finish()
            }
        }
    }

    private fun validateAnswer(context: Context): Boolean {
        val sharedPreferences = SharedPreferencesHelper.getSharedPreferences(context)
        answers.clear()

        CheckInActivity.surveyQuestionsList.forEach { question ->
            val questionId = question.iD ?: return false

            if (question.subQuestions.isNullOrEmpty()) {
                // Validate main question
                when (question.questionType) {
                    "photo" -> {
                        val photoUri = sharedPreferences.getString(
                            "${ANSWER_PHOTO}_${questionId}_0",
                            null
                        )
                        if (photoUri == null) {
                            Log.e("Empty", "")
                            return false
                        } else {
                            answers.add(
                                SurveyAnswer(
                                    questionId,
                                    null,
                                    null,
                                    null,
                                    null,
                                    photoUri
                                )
                            )
                        }
                    }

                    "essay" -> {
                        val text = sharedPreferences.getString(
                            "${ANSWER_TEXT}_${questionId}_0",
                            null
                        )
                        if (text.isNullOrBlank()) return false else {
                            answers.add(
                                SurveyAnswer(
                                    questionId,
                                    text,
                                    null,
                                    null,
                                    null,
                                    null
                                )
                            )
                        }
                    }

                    "checkbox" -> {
                        var answeredCount = 0
                        for (i in 0 until question.checkboxOptions?.size!!) {
                            val isAnswered = sharedPreferences.getBoolean(
                                "${ANSWER_CHECKBOX_MULTIPLE}_${questionId}_0_${i}",
                                false
                            )
                            if (isAnswered)
                                answeredCount++
                        }
                        if (answeredCount == 0) return false else {
                            for (i in 0 until question.checkboxOptions.size) {
                                val isAnswered = sharedPreferences.getBoolean(
                                    "${ANSWER_CHECKBOX_MULTIPLE}_${questionId}_0_${i}",
                                    false
                                )
                                answers.add(
                                    SurveyAnswer(
                                        questionId,
                                        if (isAnswered) "1" else "0",
                                        question.checkboxOptions[i]?.iD,
                                        null,
                                        null,
                                        null
                                    )
                                )
                            }
                        }
                    }

                    "multiple" -> {
                        var answeredCount = 0
                        for (i in 0 until question.multipleOptions?.size!!) {
                            val isAnswered = sharedPreferences.getBoolean(
                                "${ANSWER_CHECKBOX_MULTIPLE}_${questionId}_0_${i}",
                                false
                            )
                            if (isAnswered)
                                answeredCount++
                        }
                        if (answeredCount == 0) return false else {
                            for (i in 0 until question.multipleOptions.size) {
                                val isAnswered = sharedPreferences.getBoolean(
                                    "${ANSWER_CHECKBOX_MULTIPLE}_${questionId}_0_${i}",
                                    false
                                )
                                answers.add(
                                    SurveyAnswer(
                                        questionId,
                                        if (isAnswered) "1" else "0",
                                        null,
                                        question.multipleOptions[i]?.iD,
                                        null,
                                        null
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // Validate sub questions
                question.subQuestions.forEach { subQuestion ->
                    val subQuestionId = subQuestion?.iD ?: return false

                    when (subQuestion.questionType) {
                        "photo" -> {
                            val photoUri = sharedPreferences.getString(
                                "${ANSWER_PHOTO}_${questionId}_${subQuestionId}",
                                null
                            )
                            if (photoUri == null) return false else {
                                answers.add(
                                    SurveyAnswer(
                                        questionId,
                                        null,
                                        null,
                                        null,
                                        subQuestionId,
                                        photoUri
                                    )
                                )
                            }
                        }

                        "essay" -> {
                            val text = sharedPreferences.getString(
                                "${ANSWER_TEXT}_${questionId}_${subQuestionId}",
                                null
                            )
                            if (text.isNullOrBlank()) return false else {
                                answers.add(
                                    SurveyAnswer(
                                        questionId,
                                        text,
                                        null,
                                        null,
                                        subQuestionId,
                                        null
                                    )
                                )
                            }
                        }

                        "checkbox" -> {
                            var answeredCount = 0
                            for (i in 0 until question.checkboxOptions?.size!!) {
                                val isAnswered = sharedPreferences.getBoolean(
                                    "${ANSWER_CHECKBOX_MULTIPLE}_${questionId}_${subQuestionId}_${i}",
                                    false
                                )
                                Log.e("Is Answered", "$isAnswered")
                                if (isAnswered)
                                    answeredCount++
                            }
                            if (answeredCount == 0) return false else {
                                for (i in 0 until question.checkboxOptions.size) {
                                    val isAnswered = sharedPreferences.getBoolean(
                                        "${ANSWER_CHECKBOX_MULTIPLE}_${questionId}_0_${i}",
                                        false
                                    )
                                    answers.add(
                                        SurveyAnswer(
                                            questionId,
                                            if (isAnswered) "1" else "0",
                                            question.checkboxOptions[i]?.iD,
                                            null,
                                            null,
                                            null
                                        )
                                    )
                                }
                            }
                        }

                        "multiple" -> {
                            var answeredCount = 0
                            for (i in 0 until question.multipleOptions?.size!!) {
                                val isAnswered = sharedPreferences.getBoolean(
                                    "${ANSWER_CHECKBOX_MULTIPLE}_${questionId}_${subQuestionId}_${i}",
                                    false
                                )
                                Log.e("Is Answered", "$isAnswered")
                                if (isAnswered)
                                    answeredCount++
                            }
                            if (answeredCount == 0) return false else {
                                for (i in 0 until question.multipleOptions.size) {
                                    val isAnswered = sharedPreferences.getBoolean(
                                        "${ANSWER_CHECKBOX_MULTIPLE}_${questionId}_0_${i}",
                                        false
                                    )
                                    answers.add(
                                        SurveyAnswer(
                                            questionId,
                                            if (isAnswered) "1" else "0",
                                            null,
                                            question.multipleOptions[i]?.iD,
                                            null,
                                            null
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        return true
    }

    private fun openCamera() {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.forLanguageTag("id-ID")).format(Date())
        cameraCaptureFileName = "Super_Answer_${questionID}-${subQuestionID}_${timeStamp}.jpg"
        imageUri = contentResolver?.insert(
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

    override fun onTakePhotoButtonClick(position: Int, questionID: Int, subQuestionID: Int?) {
        this.questionID = questionID
        this.subQuestionID = subQuestionID
        if (PermissionHelper.isPermissionGranted(
                this@AnswerActivity,
                PermissionHelper.CAMERA
            )
        ) {
            openCamera()
        } else {
            PermissionHelper.requestPermission(
                this@AnswerActivity,
                arrayOf(PermissionHelper.CAMERA),
                PermissionHelper.REQUEST_CODE_CAMERA
            )
        }
    }
}