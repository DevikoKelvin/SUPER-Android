package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class OutletListResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: List<OutletItem?>? = null,
    @field:SerializedName("message")
    val message: String? = null
)

data class OutletItem(
    @field:SerializedName("OutletID")
    val outletID: String? = null,
    @field:SerializedName("Address")
    val address: String? = null,
    @field:SerializedName("Village")
    val village: Long? = null,
    @field:SerializedName("created_at")
    val createdAt: String? = null,
    @field:SerializedName("Latitude")
    val latitude: String? = null,
    @field:SerializedName("Longitude")
    val longitude: String? = null,
    @field:SerializedName("Province")
    val province: Int? = null,
    @field:SerializedName("Name")
    val name: String? = null,
    @field:SerializedName("SubDistrict")
    val subDistrict: Int? = null,
    @field:SerializedName("updated_at")
    val updatedAt: String? = null,
    @field:SerializedName("CityRegency")
    val cityRegency: Int? = null,
    @field:SerializedName("OutletType")
    val outletType: Int? = null,
    @field:SerializedName("ID")
    val iD: Int? = null
)
