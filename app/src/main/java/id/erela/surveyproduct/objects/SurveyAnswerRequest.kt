package id.erela.surveyproduct.objects

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import okhttp3.MultipartBody

/*data class SurveyAnswerRequest(
    val AnswerGroupID: Int,
    val Answers: List<SurveyAnswer>
)*/

@Parcelize
data class SurveyAnswer(
    val QuestionID: Int,
    val Answer: String?,
    val CheckboxID: Int? = null,
    val MultipleID: Int? = null,
    val SubQuestionID: Int? = null,
    var Photo: String? = null
): Parcelable