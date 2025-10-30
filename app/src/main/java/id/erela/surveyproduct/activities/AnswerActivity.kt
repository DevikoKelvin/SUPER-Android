package id.erela.surveyproduct.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
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
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.Insets
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import id.erela.surveyproduct.R
import id.erela.surveyproduct.activities.CheckInActivity.Companion.CHECK_IN_UPLOADED
import id.erela.surveyproduct.adapters.recycler_view.QuestionSurveyAdapter
import id.erela.surveyproduct.databinding.ActivityAnswerBinding
import id.erela.surveyproduct.dialogs.LoadingDialog
import id.erela.surveyproduct.helpers.Generic
import id.erela.surveyproduct.helpers.PermissionHelper
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.AnswerCheckResponse
import id.erela.surveyproduct.objects.InsertAnswerResponse
import id.erela.surveyproduct.objects.SurveyAnswer
import id.erela.surveyproduct.objects.SurveyListResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
    private lateinit var dialog: LoadingDialog
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
                    // Find the position of the item with this questionID
                    val position =
                        CheckInActivity.surveyQuestionsList.indexOfFirst { it.iD == questionID }
                    if (position != -1) {
                        adapter.notifyItemChanged(position)
                    }
                }
            }
        }
    }
    private var emptiedAnswer: ArrayList<Int> = ArrayList()

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

        fun start(context: Context) {
            context.startActivity(
                Intent(context, AnswerActivity::class.java)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

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
            dialog = LoadingDialog(this@AnswerActivity)
            backButton.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
            val isAnswerUploaded = sharedPreferences.getBoolean(ANSWER_UPLOADED, false)

            if (isAnswerUploaded) {
                CheckOutActivity.start(
                    this@AnswerActivity
                )
                finish()
            } else {
                AppAPI.superEndpoint.checkAnswer(
                    sharedPreferences.getInt(
                        CheckInActivity.ANSWER_GROUP_ID,
                        0
                    )
                ).enqueue(object : Callback<AnswerCheckResponse> {
                    override fun onResponse(
                        call: Call<AnswerCheckResponse?>,
                        response: Response<AnswerCheckResponse?>
                    ) {
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                val result = response.body()
                                when (result?.code) {
                                    1 -> {
                                        sharedPreferences.edit {
                                            putBoolean(
                                                ANSWER_UPLOADED,
                                                true
                                            )
                                        }
                                        CheckOutActivity.start(
                                            this@AnswerActivity
                                        )
                                        finish()
                                    }

                                    0 -> {
                                        sharedPreferences.edit {
                                            putBoolean(
                                                ANSWER_UPLOADED,
                                                false
                                            )
                                        }
                                    }
                                }
                            } else {
                                sharedPreferences.edit {
                                    putBoolean(
                                        ANSWER_UPLOADED,
                                        false
                                    )
                                }
                                Log.e("ERROR", "Check Answer response body is null")
                            }
                        } else {
                            sharedPreferences.edit {
                                putBoolean(
                                    ANSWER_UPLOADED,
                                    false
                                )
                            }
                            Log.e(
                                "ERROR",
                                "Check Answer response is not successful. ${response.code()}: ${response.message()}"
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<AnswerCheckResponse?>,
                        t: Throwable
                    ) {
                        sharedPreferences.edit {
                            putBoolean(
                                ANSWER_UPLOADED,
                                false
                            )
                        }
                        t.printStackTrace()
                        Log.e("ERROR", "Check Answer failure. ${t.message}")
                    }
                })
            }

            adapter = QuestionSurveyAdapter(
                CheckInActivity.surveyQuestionsList,
                this@AnswerActivity
            ).also {
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
                    if (dialog.window != null)
                        dialog.show()
                    try {
                        val map = mutableListOf<MultipartBody.Part>()
                        with(map) {
                            for (i in 0 until answers.size) {
                                add(
                                    MultipartBody.Part.createFormData(
                                        "Answers[${'$'}i][QuestionID]",
                                        answers[i].QuestionID.toString()
                                    )
                                )
                                if (answers[i].Answer != null)
                                    add(
                                        MultipartBody.Part.createFormData(
                                            "Answers[${'$'}i][Answer]",
                                            answers[i].Answer!!
                                        )
                                    )
                                if (answers[i].CheckboxID != null)
                                    add(
                                        MultipartBody.Part.createFormData(
                                            "Answers[${'$'}i][CheckboxID]",
                                            answers[i].CheckboxID.toString()
                                        )
                                    )
                                if (answers[i].MultipleID != null)
                                    add(
                                        MultipartBody.Part.createFormData(
                                            "Answers[${'$'}i][MultipleID]",
                                            answers[i].MultipleID.toString()
                                        )
                                    )
                                if (answers[i].SubQuestionID != null)
                                    add(
                                        MultipartBody.Part.createFormData(
                                            "Answers[${'$'}i][SubQuestionID]",
                                            answers[i].SubQuestionID.toString()
                                        )
                                    )
                                if (answers[i].Photo != null) {
                                    add(
                                        createMultipartBody(
                                            answers[i].Photo!!.toUri(),
                                            "Answers[${'$'}i][Photo]"
                                        )!!
                                    )
                                }
                            }
                        }
                        val answerGroupIdPart =
                            createPartFromString(
                                sharedPreferences.getInt(
                                    CheckInActivity.ANSWER_GROUP_ID,
                                    0
                                ).toString()
                            )

                        AppAPI.superEndpoint.insertAnswer(
                            answerGroupIdPart!!, map
                        ).enqueue(object : Callback<InsertAnswerResponse> {
                            override fun onResponse(
                                call: Call<InsertAnswerResponse?>,
                                response: Response<InsertAnswerResponse?>
                            ) {
                                dialog.dismiss()
                                if (response.isSuccessful) {
                                    if (response.body() != null) {
                                        val result = response.body()
                                        when (result?.code) {
                                            1 -> {
                                                CustomToast(applicationContext)
                                                    .setMessage(
                                                        if (getString(R.string.language) == "en") "Survey Answer Successfully Submitted!"
                                                        else "Jawaban Survei Berhasil Dikirim!"
                                                    )
                                                    .setBackgroundColor(
                                                        getColor(R.color.custom_toast_background_success)
                                                    )
                                                    .setFontColor(
                                                        getColor(R.color.custom_toast_font_success)
                                                    ).show()
                                                sharedPreferences.edit {
                                                    putBoolean(
                                                        ANSWER_UPLOADED,
                                                        true
                                                    )
                                                }
                                                with(intent) {
                                                    CheckOutActivity.start(
                                                        this@AnswerActivity
                                                    )
                                                }
                                                finish()
                                            }

                                            0 -> {
                                                CustomToast(applicationContext)
                                                    .setMessage(
                                                        if (getString(R.string.language) == "en") "Survey Answer Submission Failed! ${'$'}{result.message}"
                                                        else "Pengiriman Jawaban Survei Gagal! ${'$'}{result.message}"
                                                    )
                                                    .setBackgroundColor(
                                                        getColor(R.color.custom_toast_background_failed)
                                                    )
                                                    .setFontColor(
                                                        getColor(R.color.custom_toast_font_failed)
                                                    ).show()
                                                sharedPreferences.edit {
                                                    putBoolean(
                                                        ANSWER_UPLOADED,
                                                        false
                                                    )
                                                }
                                                Generic.crashReport(Exception("Survey Answer Submission Failed! ${'$'}{result.message}"))
                                            }
                                        }
                                    } else {
                                        CustomToast(applicationContext)
                                            .setMessage(
                                                if (getString(R.string.language) == "en") "Survey Answer Submission Failed!"
                                                else "Pengiriman Jawaban Survei Gagal!"
                                            )
                                            .setBackgroundColor(
                                                getColor(R.color.custom_toast_background_failed)
                                            )
                                            .setFontColor(
                                                getColor(R.color.custom_toast_font_failed)
                                            ).show()
                                        sharedPreferences.edit {
                                            putBoolean(
                                                ANSWER_UPLOADED,
                                                false
                                            )
                                        }
                                        Log.e(
                                            "ERROR",
                                            "Survey Answer Submission response body is null"
                                        )
                                        Generic.crashReport(Exception("Survey Answer Submission response body is null"))
                                    }
                                } else {
                                    CustomToast(applicationContext)
                                        .setMessage(
                                            if (getString(R.string.language) == "en") "Survey Answer Submission Failed!"
                                            else "Pengiriman Jawaban Survei Gagal!"
                                        )
                                        .setBackgroundColor(
                                            getColor(R.color.custom_toast_background_failed)
                                        )
                                        .setFontColor(
                                            getColor(R.color.custom_toast_font_failed)
                                        ).show()
                                    sharedPreferences.edit {
                                        putBoolean(
                                            ANSWER_UPLOADED,
                                            false
                                        )
                                    }
                                    Log.e(
                                        "ERROR",
                                        "Survey Answer Submission response is not successful. ${'$'}{response.code()}: ${'$'}{response.message()}"
                                    )
                                    Generic.crashReport(Exception("Survey Answer Submission response is not successful. ${'$'}{response.code()}: ${'$'}{response.message()}"))
                                }
                            }

                            override fun onFailure(
                                call: Call<InsertAnswerResponse?>,
                                throwable: Throwable
                            ) {
                                dialog.dismiss()
                                sharedPreferences.edit {
                                    putBoolean(
                                        ANSWER_UPLOADED,
                                        false
                                    )
                                }
                                throwable.printStackTrace()
                                Log.e(
                                    "ERROR",
                                    "Survey Answer Submission failure. ${'$'}{throwable.message}"
                                )
                                CustomToast(applicationContext)
                                    .setMessage(
                                        if (getString(R.string.language) == "en") "Survey Answer Submission Failed!"
                                        else "Pengiriman Jawaban Survei Gagal!"
                                    )
                                    .setBackgroundColor(
                                        getColor(R.color.custom_toast_background_failed)
                                    )
                                    .setFontColor(
                                        getColor(R.color.custom_toast_font_failed)
                                    ).show()
                                Generic.crashReport(Exception("Survey Answer Submission failure. ${'$'}{throwable.message}"))
                            }

                        })
                    } catch (e: Exception) {
                        dialog.dismiss()
                        sharedPreferences.edit {
                            putBoolean(
                                ANSWER_UPLOADED,
                                false
                            )
                        }
                        e.printStackTrace()
                        Log.e("ERROR", "Survey Answer Submission Exception: ${'$'}{e.message}")
                        CustomToast(applicationContext)
                            .setMessage(
                                if (getString(R.string.language) == "en") "Survey Answer Submission Failed!"
                                else "Pengiriman Jawaban Survei Gagal!"
                            )
                            .setBackgroundColor(
                                getColor(R.color.custom_toast_background_failed)
                            )
                            .setFontColor(
                                getColor(R.color.custom_toast_font_failed)
                            ).show()
                        Generic.crashReport(Exception("Survey Answer Submission Exception: ${'$'}{e.message}"))
                    }
                } else {
                    CustomToast.getInstance(applicationContext)
                        .setMessage(
                            if (getString(R.string.language) == "en") "Please answer all questions. Question number ${'$'}{emptiedAnswer[0]} is empty."
                            else "Tolong jawab semua pertanyaan. Pertanyaan nomor ${'$'}{emptiedAnswer[0]} kosong"
                        )
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
                                            if (result.data == null) {
                                                CustomToast.getInstance(applicationContext)
                                                    .setMessage(
                                                        if (getString(R.string.language) == "en") "There's no survey has been set. Please contact the administrator!"
                                                        else "Belum ada survei yang ditetapkan. Silakan hubungi administrator!"
                                                    )
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
                                                finish()
                                                if (CheckInActivity.activity != null)
                                                    CheckInActivity.activity?.finish()
                                                CheckInActivity.clearCheckInData(this@AnswerActivity)
                                                CheckInActivity.clearAnswerData(this@AnswerActivity)
                                                CheckOutActivity.clearCheckOutData(this@AnswerActivity)
                                            } else {
                                                CheckInActivity.surveyQuestionsList.clear()
                                                CheckInActivity.questionIdArray.clear()
                                                CheckInActivity.subQuestionIdArray.clear()
                                                for (item in result.data) {
                                                    CheckInActivity.surveyQuestionsList.add(item!!)
                                                    if (item.subQuestions != null) {
                                                        for (subItem in item.subQuestions) {
                                                            CheckInActivity.questionIdArray.add(item.iD!!)
                                                            CheckInActivity.subQuestionIdArray.add(
                                                                subItem?.iD
                                                            )
                                                        }
                                                    } else {
                                                        CheckInActivity.questionIdArray.add(item.iD!!)
                                                        CheckInActivity.subQuestionIdArray.add(0)
                                                    }
                                                }
                                                adapter.notifyDataSetChanged()
                                            }
                                        }

                                        0 -> {
                                            CustomToast.getInstance(applicationContext)
                                                .setMessage(
                                                    if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                                                    else "Terjadi kesalahan, silakan coba lagi nanti"
                                                )
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
                                            finish()
                                            if (CheckInActivity.activity != null)
                                                CheckInActivity.activity?.finish()
                                            CheckInActivity.clearCheckInData(this@AnswerActivity)
                                            CheckInActivity.clearAnswerData(this@AnswerActivity)
                                            CheckOutActivity.clearCheckOutData(this@AnswerActivity)
                                            Generic.crashReport(Exception("Get Survey List response code 0"))
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
                            CustomToast.getInstance(applicationContext)
                                .setMessage(
                                    if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                                    else "Terjadi kesalahan, silakan coba lagi nanti"
                                )
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
                            Generic.crashReport(Exception(throwable.toString()))
                        }
                    })
            } catch (jsonException: JSONException) {
                Log.e("ERROR", jsonException.toString())
                jsonException.printStackTrace()
                CustomToast.getInstance(applicationContext)
                    .setMessage(
                        if (getString(R.string.language) == "en") "Something went wrong, please try again later"
                        else "Terjadi kesalahan, silakan coba lagi nanti"
                    )
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
                Generic.crashReport(Exception(jsonException.toString()))
            }
        }
    }

    private fun validateAnswer(context: Context): Boolean {
        val sharedPreferences = SharedPreferencesHelper.getSharedPreferences(context)
        answers.clear()
        emptiedAnswer.clear()

        CheckInActivity.surveyQuestionsList.forEach { question ->
            val questionId = question.iD ?: return false

            if (question.subQuestions.isNullOrEmpty()) {
                // Validate main question
                when (question.questionType) {
                    "photo" -> {
                        val photoUri = sharedPreferences.getString(
                            "${'$'}{ANSWER_PHOTO}_${'$'}{questionId}_0",
                            null
                        )
                        if (photoUri == null) {
                            Log.e("Photo [${'$'}questionId][0]", "Empty")
                            emptiedAnswer.add(questionId + 1)
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
                            "${'$'}{ANSWER_TEXT}_${'$'}{questionId}_0",
                            null
                        )
                        if (text.isNullOrBlank()) {
                            Log.e("Essay [${'$'}questionId][0]", "Empty")
                            emptiedAnswer.add(questionId + 1)
                            return false
                        } else {
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

                    "scale" -> {
                        val text = sharedPreferences.getString(
                            "${'$'}{ANSWER_TEXT}_${'$'}{questionId}_0",
                            null
                        )
                        if (text.isNullOrBlank()) {
                            Log.e("Scale [${'$'}questionId][0]", "Empty")
                            emptiedAnswer.add(questionId + 1)
                            return false
                        } else {
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
                                "${'$'}{ANSWER_CHECKBOX_MULTIPLE}_${'$'}{questionId}_0_${'$'}{i}",
                                false
                            )
                            if (isAnswered)
                                answeredCount++
                        }
                        if (answeredCount == 0) {
                            Log.e("Checkbox [${'$'}questionId][0]", "Empty")
                            emptiedAnswer.add(questionId + 1)
                            return false
                        } else {
                            for (i in 0 until question.checkboxOptions.size) {
                                val isAnswered = sharedPreferences.getBoolean(
                                    "${'$'}{ANSWER_CHECKBOX_MULTIPLE}_${'$'}{questionId}_0_${'$'}{i}",
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
                                "${'$'}{ANSWER_CHECKBOX_MULTIPLE}_${'$'}{questionId}_0_${'$'}{i}",
                                false
                            )
                            if (isAnswered)
                                answeredCount++
                        }
                        if (answeredCount == 0) {
                            Log.e("Multiple [${'$'}questionId][0]", "Empty")
                            emptiedAnswer.add(questionId + 1)
                            return false
                        } else {
                            for (i in 0 until question.multipleOptions.size) {
                                val isAnswered = sharedPreferences.getBoolean(
                                    "${'$'}{ANSWER_CHECKBOX_MULTIPLE}_${'$'}{questionId}_0_${'$'}{i}",
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
                                "${'$'}{ANSWER_PHOTO}_${'$'}{questionId}_${'$'}{subQuestionId}",
                                null
                            )
                            if (photoUri == null) {
                                Log.e("Photo [${'$'}questionId][${'$'}subQuestionId]", "Empty")
                                emptiedAnswer.add(questionId + 1)
                                return false
                            } else {
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
                                "${'$'}{ANSWER_TEXT}_${'$'}{questionId}_${'$'}{subQuestionId}",
                                null
                            )
                            if (text.isNullOrBlank()) {
                                Log.e("Essay [${'$'}questionId][${'$'}subQuestionId]", "Empty")
                                emptiedAnswer.add(questionId + 1)
                                return false
                            } else {
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

                        "scale" -> {
                            val text = sharedPreferences.getString(
                                "${'$'}{ANSWER_TEXT}_${'$'}{questionId}_${'$'}{subQuestionId}",
                                null
                            )
                            if (text.isNullOrBlank()) {
                                Log.e("Scale [${'$'}questionId][${'$'}subQuestionId]", "Empty")
                                emptiedAnswer.add(questionId + 1)
                                return false
                            } else {
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
                                    "${'$'}{ANSWER_CHECKBOX_MULTIPLE}_${'$'}{questionId}_${'$'}{subQuestionId}_${'$'}{i}",
                                    false
                                )
                                if (isAnswered)
                                    answeredCount++
                            }
                            if (answeredCount == 0) {
                                Log.e("Checkbox [${'$'}questionId][${'$'}subQuestionId]", "Empty")
                                emptiedAnswer.add(questionId + 1)
                                return false
                            } else {
                                for (i in 0 until question.checkboxOptions.size) {
                                    val isAnswered = sharedPreferences.getBoolean(
                                        "${'$'}{ANSWER_CHECKBOX_MULTIPLE}_${'$'}{questionId}_0_${'$'}{i}",
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
                                    "${'$'}{ANSWER_CHECKBOX_MULTIPLE}_${'$'}{questionId}_${'$'}{subQuestionId}_${'$'}{i}",
                                    false
                                )
                                if (isAnswered)
                                    answeredCount++
                            }
                            if (answeredCount == 0) {
                                Log.e("Multiple [${'$'}questionId][${'$'}subQuestionId]", "Empty")
                                emptiedAnswer.add(questionId + 1)
                                return false
                            } else {
                                for (i in 0 until question.multipleOptions.size) {
                                    val isAnswered = sharedPreferences.getBoolean(
                                        "${'$'}{ANSWER_CHECKBOX_MULTIPLE}_${'$'}{questionId}_0_${'$'}{i}",
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
        cameraCaptureFileName = "Super_Answer_${'$'}{questionID}-${'$'}{subQuestionID}_${'$'}{timeStamp}.jpg"
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

    private fun createPartFromString(stringData: String?): RequestBody? {
        return stringData?.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun createMultipartBody(uri: Uri, name: String): MultipartBody.Part? {
        return try {
            val file = File(getRealPathFromURI(uri)!!)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(name, file.name, requestBody)
        } catch (e: Exception) {
            Log.e("createMultipartBody", "Error creating MultipartBody.Part", e)
            null
        }
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                val fileName = cursor.getString(displayNameIndex)
                cursor.close()
                return fileName
            }
        }
        cursor?.close()
        return null
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val contentResolver = contentResolver
        val fileName = getFileName(contentResolver!!, uri)

        if (fileName != null) {
            val file = File(cacheDir, fileName)
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                val buffer = ByteArray(4 * 1024)
                var read: Int

                while (inputStream!!.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                return file.absolutePath
            } catch (e: IOException) {
                Log.e("getRealPathFromURI", "Error: ${'$'}{e.message}")
            }
        }

        return null
    }
}
