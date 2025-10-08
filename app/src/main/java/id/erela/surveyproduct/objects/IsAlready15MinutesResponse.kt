package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class IsAlready15MinutesResponse(
	@SerializedName("code")
	val code: Int? = null,
	@SerializedName("data")
	val data: TimeDiff? = null,
	@SerializedName("message")
	val message: String? = null
)

data class TimeDiff(
	@SerializedName("timeDiff")
	val timeDiff: Int? = null,
	@SerializedName("remaining")
	val remaining: String? = null
)

