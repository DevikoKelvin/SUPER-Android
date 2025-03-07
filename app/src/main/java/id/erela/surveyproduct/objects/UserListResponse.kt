package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class UserListResponse(
    @field:SerializedName("datas")
    val datas: List<Users?>? = null,
    @field:SerializedName("error")
    val error: Int? = null,
    @field:SerializedName("message")
    val message: String? = null
)