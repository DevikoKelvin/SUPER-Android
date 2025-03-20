package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class AnswerHistoryResponse(
	@field:SerializedName("code")
    val code: Int? = null,
	@field:SerializedName("data")
    val data: List<QuestionAnswersItem?>? = null,
	@field:SerializedName("message")
    val message: String? = null
)

data class SubQuestionAnswersItem(
    @field:SerializedName("Answer")
    val answer: List<AnswerItem?>? = null,
    @field:SerializedName("QuestionType")
    val questionType: String? = null,
    @field:SerializedName("Question")
    val question: String? = null,
    @field:SerializedName("SubQuestionID")
    val subQuestionID: Int? = null
)

data class QuestionAnswersItem(
    @field:SerializedName("Answer")
    val answer: List<AnswerItem?>? = null,
    @field:SerializedName("QuestionID")
    val questionID: Int? = null,
    @field:SerializedName("QuestionType")
    val questionType: String? = null,
    @field:SerializedName("Question")
    val question: String? = null,
    @field:SerializedName("SubQuestions")
    val subQuestions: List<SubQuestionAnswersItem?>? = null
)

data class AnswerItem(
    @field:SerializedName("Answer")
    val answer: String? = null,
    @field:SerializedName("Option")
    val option: String? = null
)
