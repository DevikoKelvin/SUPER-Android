package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class OutletEditResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: Data? = null,
    @field:SerializedName("message")
    val message: String? = null
)

data class Data(
    @field:SerializedName("OutletID")
    val outletID: String? = null,
    @field:SerializedName("TypeName")
    val typeName: String? = null,
    @field:SerializedName("Address")
    val address: String? = null,
    @field:SerializedName("Village")
    val village: Long? = null,
    @field:SerializedName("Latitude")
    val latitude: String? = null,
    @field:SerializedName("SubDistrictName")
    val subDistrictName: String? = null,
    @field:SerializedName("Longitude")
    val longitude: String? = null,
    @field:SerializedName("UpdatedAt")
    val updatedAt: String? = null,
    @field:SerializedName("Province")
    val province: Int? = null,
    @field:SerializedName("Name")
    val name: String? = null,
    @field:SerializedName("ProvinceName")
    val provinceName: String? = null,
    @field:SerializedName("Type")
    val type: Int? = null,
    @field:SerializedName("SubDistrict")
    val subDistrict: Int? = null,
    @field:SerializedName("VillageName")
    val villageName: String? = null,
    @field:SerializedName("CityRegencyName")
    val cityRegencyName: String? = null,
    @field:SerializedName("CityRegency")
    val cityRegency: Int? = null,
    @field:SerializedName("ID")
    val iD: Int? = null
)
