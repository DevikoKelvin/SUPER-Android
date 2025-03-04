package id.erela.surveyproduct.objects.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @field:SerializedName("data")
    val data: Data? = null,
    @field:SerializedName("message")
    val message: String? = null,
    @field:SerializedName("code")
    val code: Int? = null
)

data class Data(
    @field:SerializedName("updated_at")
    val updatedAt: String? = null,
    @field:SerializedName("phone")
    val phone: String? = null,
    @field:SerializedName("photo_profile")
    val photoProfile: String? = null,
    @field:SerializedName("name")
    val name: String? = null,
    @field:SerializedName("created_at")
    val createdAt: String? = null,
    @field:SerializedName("id")
    val id: Int? = null,
    @field:SerializedName("privilege")
    val privilege: Int? = null,
    @field:SerializedName("username")
    val username: String? = null
)
