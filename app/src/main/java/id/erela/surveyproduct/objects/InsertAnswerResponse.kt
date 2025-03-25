package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class InsertAnswerResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: Any? = null,
    @field:SerializedName("message")
    val message: String? = null
)
