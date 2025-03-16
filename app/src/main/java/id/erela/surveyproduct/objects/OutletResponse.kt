package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class OutletResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: OutletItem? = null,
    @field:SerializedName("message")
    val message: String? = null
)
