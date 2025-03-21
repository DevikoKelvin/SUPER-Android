package id.erela.surveyproduct.objects

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class SurveyListResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: List<QuestionsItem?>? = null,
    @field:SerializedName("message")
    val message: String? = null
)

@Parcelize
data class QuestionsItem(
    @field:SerializedName("QuestionType")
    val questionType: String? = null,
    @field:SerializedName("Question")
    val question: String? = null,
    @field:SerializedName("ID")
    val iD: Int? = null,
    @field:SerializedName("MultipleOptions")
    val multipleOptions: List<CheckboxMultipleOptionsItem?>? = null,
    @field:SerializedName("CheckboxOptions")
    val checkboxOptions: List<CheckboxMultipleOptionsItem?>? = null,
    @field:SerializedName("SubQuestions")
    val subQuestions: List<SubQuestionsItem?>? = null
) : Parcelable

@Parcelize
data class SubQuestionsItem(
    @field:SerializedName("QuestionID")
    val questionID: Int? = null,
    @field:SerializedName("QuestionType")
    val questionType: String? = null,
    @field:SerializedName("Question")
    val question: String? = null,
    @field:SerializedName("ID")
    val iD: Int? = null,
    @field:SerializedName("MultipleOptions")
    val multipleOptions: List<CheckboxMultipleOptionsItem?>? = null,
    @field:SerializedName("CheckboxOptions")
    val checkboxOptions: List<CheckboxMultipleOptionsItem?>? = null
) : Parcelable

@Parcelize
data class CheckboxMultipleOptionsItem(
    @field:SerializedName("Options")
    val options: String? = null,
    @field:SerializedName("QuestionID")
    val questionID: Int? = null,
    @field:SerializedName("ID")
    val iD: Int? = null,
    @field:SerializedName("SubQuestionID")
    val subQuestionID: Int? = null
) : Parcelable

data class CheckboxMultipleItem(
    var isChecked: Boolean = false,
    val checkboxMultipleOptions: CheckboxMultipleOptionsItem? = null
)