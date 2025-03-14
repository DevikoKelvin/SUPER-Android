package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class ProvinceListResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: List<ProvinceItem?>? = null,
    @field:SerializedName("message")
    val message: String? = null
)

data class ProvinceItem(
    @field:SerializedName("name")
    val name: String? = null,
    @field:SerializedName("id")
    val id: Int? = null
)
