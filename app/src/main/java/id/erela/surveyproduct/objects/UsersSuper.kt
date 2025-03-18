package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class UsersSuper(
    @field:SerializedName("TypeID")
    val typeID: Int? = null,
    @field:SerializedName("TypeName")
    val typeName: String? = null,
    @field:SerializedName("UserName")
    val userName: String? = null,
    @field:SerializedName("PhotoProfile")
    val photoProfile: String? = null,
    @field:SerializedName("UserCode")
    val userCode: String? = null,
    @field:SerializedName("created_at")
    val createdAt: String? = null,
    @field:SerializedName("TeamName")
    val teamName: String? = null,
    @field:SerializedName("BranchID")
    val branchID: Int? = null,
    @field:SerializedName("TeamID")
    val teamID: Int? = null,
    @field:SerializedName("updated_at")
    val updatedAt: String? = null,
    @field:SerializedName("UserMail")
    val userMail: String? = null,
    @field:SerializedName("FullName")
    val fullName: String? = null,
    @field:SerializedName("ID")
    val iD: Int? = null,
    @field:SerializedName("BranchName")
    val branchName: String? = null,
    @field:SerializedName("Password")
    val password: String? = null
)
