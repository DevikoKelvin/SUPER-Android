package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class AnswerHistoryResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: List<AnswerItem?>? = null,
    @field:SerializedName("message")
    val message: String? = null
)

data class AnswerItem(
    @field:SerializedName("Answer")
    val answer: String? = null,
    @field:SerializedName("QuestionID")
    val questionID: Int? = null,
    @field:SerializedName("AnswerGroupID")
    val answerGroupID: Int? = null,
    @field:SerializedName("ID")
    val iD: Int? = null,
    @field:SerializedName("SubQuestionID")
    val subQuestionID: Int? = null
)
