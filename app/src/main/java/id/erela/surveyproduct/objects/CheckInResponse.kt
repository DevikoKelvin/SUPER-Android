package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class CheckInResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: CheckInData? = null,
    @field:SerializedName("message")
    val message: String? = null
)

data class CheckInData(
    @field:SerializedName("OutletID")
    val outletID: String? = null,
    @field:SerializedName("LongIn")
    val longIn: String? = null,
    @field:SerializedName("SurveyID")
    val surveyID: String? = null,
    @field:SerializedName("UserID")
    val userID: String? = null,
    @field:SerializedName("LatIn")
    val latIn: String? = null,
    @field:SerializedName("AnswerGroupID")
    val answerGroupID: Int? = null,
    @field:SerializedName("PhotoIn")
    val photoIn: String? = null,
    @field:SerializedName("ID")
    val iD: Int? = null,
    @field:SerializedName("CheckInTime")
    val checkInTime: String? = null
)
