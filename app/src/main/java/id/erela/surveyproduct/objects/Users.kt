package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class Users(
    @field:SerializedName("type_name")
    val typeName: String? = null,
    @field:SerializedName("branch_id")
    val branchId: Int? = null,
    @field:SerializedName("type_id")
    val typeId: Int? = null,
    @field:SerializedName("branch_name")
    val branchName: String? = null,
    @field:SerializedName("id")
    val id: Int? = null,
    @field:SerializedName("fullname")
    val fullname: String? = null,
    @field:SerializedName("usermail")
    val usermail: String? = null,
    @field:SerializedName("team_id")
    val teamId: Int? = null,
    @field:SerializedName("usercode")
    val usercode: String? = null,
    @field:SerializedName("team_name")
    val teamName: String? = null,
    @field:SerializedName("username")
    val username: String? = null
)
