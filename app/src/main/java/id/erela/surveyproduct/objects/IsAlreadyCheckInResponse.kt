package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class IsAlreadyCheckInResponse(
	val code: Int? = null,
	val data: CheckData? = null,
	val message: String? = null
)

data class CheckData(
	@SerializedName("AnswerGroupID")
	val answerGroupID: Int? = null,
	@SerializedName("ID")
	val iD: Int? = null
)