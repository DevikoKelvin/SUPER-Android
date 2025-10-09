package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class AnswerCheckResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: DataItem? = null,
    @field:SerializedName("message")
    val message: String? = null
)

data class DataItem(
    @field:SerializedName("completionPercentage")
    val completionPercentage: Int? = null,
    @field:SerializedName("totalQuestions")
    val totalQuestions: Int? = null,
    @field:SerializedName("missingAnswers")
    val missingAnswers: List<Any?>? = null,
    @field:SerializedName("answeredQuestions")
    val answeredQuestions: Int? = null,
    @field:SerializedName("details")
    val details: List<DetailsItem?>? = null,
    @field:SerializedName("isComplete")
    val isComplete: Boolean? = null
)

data class DetailsItem(
    @field:SerializedName("questionId")
    val questionId: Int? = null,
    @field:SerializedName("subQuestionId")
    val subQuestionId: Any? = null,
    @field:SerializedName("questionType")
    val questionType: String? = null,
    @field:SerializedName("questionText")
    val questionText: String? = null,
    @field:SerializedName("status")
    val status: String? = null
)
