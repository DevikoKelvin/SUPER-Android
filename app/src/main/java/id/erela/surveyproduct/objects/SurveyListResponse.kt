package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class SurveyListResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: List<QuestionsItem?>? = null,
    @field:SerializedName("message")
    val message: String? = null
)

data class QuestionsItem(
    @field:SerializedName("QuestionType")
    val questionType: String? = null,
    @field:SerializedName("Question")
    val question: String? = null,
    @field:SerializedName("ID")
    val iD: Int? = null,
    @field:SerializedName("ActiveStatus")
    val activeStatus: Int? = null,
    @field:SerializedName("SubQuestions")
    val subQuestions: List<SubQuestionsItem?>? = null
)

data class SubQuestionsItem(
    @field:SerializedName("QuestionID")
    val questionID: Int? = null,
    @field:SerializedName("QuestionType")
    val questionType: String? = null,
    @field:SerializedName("Question")
    val question: String? = null,
    @field:SerializedName("ID")
    val iD: Int? = null,
    @field:SerializedName("ActiveStatus")
    val activeStatus: Int? = null
)
