package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class IsAlreadyCheckInResponse(
	@SerializedName("code")
	val code: Int?,
	@SerializedName("message")
	val message: String?,
	@SerializedName("data")
	val data: CheckInData?
)

data class CheckData(
	@SerializedName("AnswerGroupID")
	val answerGroupID: Int? = null,
	@SerializedName("ID")
	val iD: Int? = null
)