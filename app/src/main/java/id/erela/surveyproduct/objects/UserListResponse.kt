package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class UserListResponse(
	@field:SerializedName("code")
    val code: Int? = null,
	@field:SerializedName("data")
    val data: List<UsersSuper?>? = null,
	@field:SerializedName("message")
    val message: String? = null
)
