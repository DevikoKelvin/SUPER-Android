package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class GenericResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("message")
    val message: String? = null
)
