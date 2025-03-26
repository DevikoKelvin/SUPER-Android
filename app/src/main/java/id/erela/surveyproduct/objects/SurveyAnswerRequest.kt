package id.erela.surveyproduct.objects

import okhttp3.MultipartBody

data class SurveyAnswerRequest(
    val AnswerGroupID: Int,
    val Answers: List<SurveyAnswer>
)

data class SurveyAnswer(
    val QuestionID: Int,
    val Answer: String?,
    val CheckboxID: Int? = null,
    val MultipleID: Int? = null,
    val SubQuestionID: Int? = null,
    var Photo: MultipartBody.Part? = null
)