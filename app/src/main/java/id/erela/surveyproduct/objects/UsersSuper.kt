package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class UsersSuper(
    @field:SerializedName("type_name")
    val typeName: String? = null,
    @field:SerializedName("photo_profile")
    val photoProfile: String? = null,
    @field:SerializedName("type_id")
    val typeId: Int? = null,
    @field:SerializedName("created_at")
    val createdAt: String? = null,
    @field:SerializedName("usermail")
    val userMail: String? = null,
    @field:SerializedName("team_id")
    val teamId: Int? = null,
    @field:SerializedName("team_name")
    val teamName: String? = null,
    @field:SerializedName("updated_at")
    val updatedAt: String? = null,
    @field:SerializedName("branch_id")
    val branchId: Int? = null,
    @field:SerializedName("branch_name")
    val branchName: String? = null,
    @field:SerializedName("id")
    val id: Int? = null,
    @field:SerializedName("fullname")
    val fullname: String? = null,
    @field:SerializedName("usercode")
    val usercode: String? = null,
    @field:SerializedName("username")
    val username: String? = null
)
