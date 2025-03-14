package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class OutletCategoryResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: List<OutletCategoryItem?>? = null,
    @field:SerializedName("message")
    val message: String? = null
)

data class OutletCategoryItem(
    @field:SerializedName("Type")
    val type: String? = null,
    @field:SerializedName("TypeCode")
    val typeCode: String? = null,
    @field:SerializedName("ID")
    val iD: Int? = null
)
