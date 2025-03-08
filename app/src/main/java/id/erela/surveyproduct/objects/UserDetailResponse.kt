package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class UserDetailResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val usersSuper: UsersSuper? = null,
    @field:SerializedName("message")
    val message: String? = null
)
