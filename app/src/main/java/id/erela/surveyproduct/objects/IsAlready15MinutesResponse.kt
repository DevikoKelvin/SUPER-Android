package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class IsAlready15MinutesResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: TimeData? = null,
    @field:SerializedName("message")
    val message: String? = null
)

data class TimeData(
    @field:SerializedName("timeDiff")
    val timeDiff: Int? = null,
    @field:SerializedName("remaining")
    val remaining: Float? = null
)
