package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @field:SerializedName("error")
    val error: Int? = null,
    @field:SerializedName("message")
    val message: String? = null,
    @field:SerializedName("users")
    val usersErela: UsersErela? = null
)
