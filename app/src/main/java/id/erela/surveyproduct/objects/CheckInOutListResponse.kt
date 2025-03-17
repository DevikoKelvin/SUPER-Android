package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CheckInOutListResponse(
	@field:SerializedName("code")
    val code: Int? = null,
	@field:SerializedName("data")
    val data: List<CheckInOutItem?>? = null,
	@field:SerializedName("message")
    val message: String? = null
)

data class CheckInOutItem(
    @field:SerializedName("OutletID")
    val outletID: Int? = null,
    @field:SerializedName("LongIn")
    val longIn: String? = null,
    @field:SerializedName("CheckOutTime")
    val checkOutTime: String? = null,
    @field:SerializedName("OutletName")
    val outletName: String? = null,
    @field:SerializedName("SurveyID")
    val surveyID: String? = null,
    @field:SerializedName("AnswerGroupID")
    val answerGroupID: Int? = null,
    @field:SerializedName("PhotoIn")
    val photoIn: String? = null,
    @field:SerializedName("LatOut")
    val latOut: String? = null,
    @field:SerializedName("LongOut")
    val longOut: String? = null,
    @field:SerializedName("OutletAddress")
    val outletAddress: String? = null,
    @field:SerializedName("UserID")
    val userID: Int? = null,
    @field:SerializedName("LatIn")
    val latIn: String? = null,
    @field:SerializedName("ID")
    val iD: Int? = null,
    @field:SerializedName("CheckInTime")
    val checkInTime: String? = null
): Serializable
