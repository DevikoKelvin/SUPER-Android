package id.erela.surveyproduct.repository

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import id.erela.surveyproduct.R
import id.erela.surveyproduct.activities.StartSurveyActivity
import id.erela.surveyproduct.activities.StartSurveyActivity.Companion.ANSWER_UPLOADED
import id.erela.surveyproduct.activities.StartSurveyActivity.Companion.CHECK_IN_UPLOADED
import id.erela.surveyproduct.fragments.CheckInFragment
import id.erela.surveyproduct.fragments.CheckOutFragment
import id.erela.surveyproduct.helpers.SharedPreferencesHelper
import id.erela.surveyproduct.helpers.UserDataHelper
import id.erela.surveyproduct.helpers.api.AppAPI
import id.erela.surveyproduct.helpers.customs.CustomToast
import id.erela.surveyproduct.objects.CheckInResponse
import id.erela.surveyproduct.objects.CheckOutResponse
import id.erela.surveyproduct.objects.SurveyAnswer
import id.erela.surveyproduct.objects.UsersSuper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class SurveyRepository(private val context: Context, private val activity: StartSurveyActivity) {
    private val userData: UsersSuper by lazy {
        UserDataHelper(context).getData()
    }
    private val sharedPreferences =
        SharedPreferencesHelper.getSharedPreferences(context)
    private var answerGroupId by Delegates.notNull<Int>()

    suspend fun uploadData(
        answers: List<SurveyAnswer>,
        photoCheckIn: MultipartBody.Part,
        photoCheckOut: MultipartBody.Part?
    ) {
        val isCheckInUploaded = sharedPreferences.getBoolean(CHECK_IN_UPLOADED, false)
        val isSurveyUploaded = sharedPreferences.getBoolean(ANSWER_UPLOADED, false)

        if (isCheckInUploaded && !isSurveyUploaded) {
            answerGroupId = sharedPreferences.getInt(CheckInFragment.ANSWER_GROUP_ID, 0)
            uploadSurveyData(answerGroupId, answers, photoCheckOut)
        } else if (!isCheckInUploaded && !isSurveyUploaded) {
            uploadCheckIn(answers, photoCheckIn, photoCheckOut)
        } else if (isCheckInUploaded && isSurveyUploaded) {
            uploadCheckOut(photoCheckOut)
        }
    }

    private suspend fun uploadCheckIn(
        answers: List<SurveyAnswer>,
        photoCheckIn: MultipartBody.Part,
        photoCheckOut: MultipartBody.Part?
    ) {
        val data: MutableMap<String, RequestBody> = mutableMapOf()
        with(data) {
            put("UserID", createPartFromString(userData.iD.toString())!!)
            put(
                "OutletID",
                createPartFromString(
                    sharedPreferences.getInt(
                        CheckInFragment.SELECTED_OUTLET,
                        0
                    ).toString()
                )!!
            )
            put(
                "LatIn",
                createPartFromString(
                    sharedPreferences.getFloat(CheckInFragment.LATITUDE, 0f).toString()
                )!!
            )
            put(
                "LongIn",
                createPartFromString(
                    sharedPreferences.getFloat(CheckInFragment.LONGITUDE, 0f).toString()
                )!!
            )
        }
        AppAPI.superEndpoint.checkIn(data, photoCheckIn)
            .enqueue(object : Callback<CheckInResponse> {
                override fun onResponse(
                    call: Call<CheckInResponse>,
                    response: Response<CheckInResponse>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            val result = response.body()
                            when (result?.code) {
                                1 -> {
                                    sharedPreferences.edit {
                                        putInt(
                                            CheckInFragment.CHECK_IN_ID,
                                            result.data?.iD!!
                                        )
                                        putInt(
                                            CheckInFragment.ANSWER_GROUP_ID,
                                            result.data.answerGroupID!!
                                        )
                                        putBoolean(
                                            CHECK_IN_UPLOADED,
                                            true
                                        )
                                    }
                                }

                                0 -> {
                                    sharedPreferences.edit {
                                        putBoolean(
                                            CHECK_IN_UPLOADED,
                                            false
                                        )
                                    }
                                }
                            }
                        } else {
                            sharedPreferences.edit {
                                putBoolean(
                                    CHECK_IN_UPLOADED,
                                    false
                                )
                            }
                        }
                    } else {
                        sharedPreferences.edit {
                            putBoolean(
                                CHECK_IN_UPLOADED,
                                false
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<CheckInResponse>, throwable: Throwable) {
                    throwable.printStackTrace()
                    sharedPreferences.edit {
                        putBoolean(
                            CHECK_IN_UPLOADED,
                            false
                        )
                    }
                }
            })
        val isCheckInUploaded = sharedPreferences.getBoolean(CHECK_IN_UPLOADED, false)
        answerGroupId = sharedPreferences.getInt(CheckInFragment.ANSWER_GROUP_ID, 0)
        if (isCheckInUploaded)
            uploadSurveyData(answerGroupId, answers, photoCheckOut)
    }

    private suspend fun uploadSurveyData(
        answerGroupId: Int,
        answers: List<SurveyAnswer>,
        photoCheckOut: MultipartBody.Part?
    ) {
        try {
            // Create answer group ID part
            val answerGroupIdPart = answerGroupId.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            // Create parts for each answer
            val answerParts = answers.mapIndexed { index, answer ->
                val parts = mutableListOf<MultipartBody.Part>()
                // Add QuestionID
                parts.add(
                    MultipartBody.Part.createFormData(
                        "Answers[$index][QuestionID]",
                        answer.QuestionID.toString()
                    )
                )
                // Add Answer if not null
                answer.Answer.let {
                    parts.add(
                        MultipartBody.Part.createFormData(
                            "Answers[$index][Answer]",
                            it!!
                        )
                    )
                }
                // Add CheckboxID if not null
                answer.CheckboxID?.let {
                    parts.add(
                        MultipartBody.Part.createFormData(
                            "Answers[$index][CheckboxID]",
                            it.toString()
                        )
                    )
                }
                // Add MultipleID if not null
                answer.MultipleID?.let {
                    parts.add(
                        MultipartBody.Part.createFormData(
                            "Answers[$index][MultipleID]",
                            it.toString()
                        )
                    )
                }
                // Add SubQuestionID if not null
                answer.SubQuestionID?.let {
                    parts.add(
                        MultipartBody.Part.createFormData(
                            "Answers[$index][SubQuestionID]",
                            it.toString()
                        )
                    )
                }
                // Add Photo if exists
                answer.Photo?.let {
                    parts.add(
                        MultipartBody.Part.createFormData(
                            "Answers[$index][Photo]",
                            it.body.contentType().toString(),
                            it.body
                        )
                    )
                }
                parts
            }.flatten()
            val response = AppAPI.superEndpoint.insertAnswer(
                answerGroupId = answerGroupIdPart,
                answers = answerParts
            )

            if (!response.isSuccessful) {
                sharedPreferences.edit {
                    putBoolean(
                        ANSWER_UPLOADED,
                        false
                    )
                }
                throw Exception("API call failed with code: ${response.code()}")
            } else {
                sharedPreferences.edit {
                    putBoolean(
                        ANSWER_UPLOADED,
                        true
                    )
                }
                uploadCheckOut(photoCheckOut)
            }
        } catch (e: Exception) {
            sharedPreferences.edit {
                putBoolean(
                    ANSWER_UPLOADED,
                    false
                )
            }
            throw Exception("Failed to submit survey: ${e.message}")
        }
    }

    private fun uploadCheckOut(
        photoCheckOut: MultipartBody.Part?
    ) {
        val data: MutableMap<String, RequestBody> = mutableMapOf()
        with(data) {
            put(
                "ID",
                createPartFromString(
                    sharedPreferences.getInt(CheckInFragment.CHECK_IN_ID, 0).toString()
                )!!
            )
            put(
                "LatIn",
                createPartFromString(
                    sharedPreferences.getFloat(CheckOutFragment.LATITUDE, 0f).toString()
                )!!
            )
            put(
                "LongIn",
                createPartFromString(
                    sharedPreferences.getFloat(CheckOutFragment.LONGITUDE, 0f).toString()
                )!!
            )
        }
        AppAPI.superEndpoint.checkOut(data, photoCheckOut).enqueue(
            object : Callback<CheckOutResponse> {
                override fun onResponse(
                    call: Call<CheckOutResponse>,
                    response: Response<CheckOutResponse>
                ) {
                    if (response.isSuccessful) {
                        if (response.body() != null) {
                            val result = response.body()
                            when (result?.code) {
                                1 -> {
                                    CustomToast(context)
                                        .setMessage("Survey Successfully Submitted!")
                                        .setBackgroundColor(
                                            context.getColor(R.color.custom_toast_background_success)
                                        )
                                        .setFontColor(
                                            context.getColor(R.color.custom_toast_font_success)
                                        ).show()
                                }

                                0 -> {
                                    CustomToast(context)
                                        .setMessage("Survey Submission Failed!")
                                        .setBackgroundColor(
                                            context.getColor(R.color.custom_toast_background_failed)
                                        )
                                        .setFontColor(
                                            context.getColor(R.color.custom_toast_font_failed)
                                        ).show()
                                }
                            }
                        } else {
                            Log.e("SurveyRepository", "Response body is null")
                        }
                    } else {
                        Log.e("SurveyRepository", "API call failed with code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<CheckOutResponse>, throwable: Throwable) {
                    throwable.printStackTrace()
                    Log.e("SurveyRepository", "API call failed: ${throwable.message}")
                }
            }
        )
    }

    private fun createPartFromString(stringData: String?): RequestBody? {
        return stringData?.toRequestBody("text/plain".toMediaTypeOrNull())
    }
}