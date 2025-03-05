package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @field:SerializedName("error")
    val error: Int? = null,
    @field:SerializedName("message")
    val message: String? = null,
    @field:SerializedName("users")
    val users: Users? = null
)

data class Users(
    @field:SerializedName("userteam")
    val userteam: String? = null,
    @field:SerializedName("usertype")
    val usertype: String? = null,
    @field:SerializedName("id")
    val id: Int? = null,
    @field:SerializedName("fullname")
    val fullname: String? = null,
    @field:SerializedName("usermail")
    val usermail: String? = null,
    @field:SerializedName("usercode")
    val usercode: String? = null,
    @field:SerializedName("branch")
    val branch: String? = null,
    @field:SerializedName("username")
    val username: String? = null
)
