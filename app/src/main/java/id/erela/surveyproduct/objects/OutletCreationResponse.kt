package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class OutletCreationResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: OutletData? = null,
    @field:SerializedName("message")
    val message: String? = null
)

data class OutletData(
    @field:SerializedName("OutletID")
    val outletID: String? = null,
    @field:SerializedName("Address")
    val address: String? = null,
    @field:SerializedName("PICNumber")
    val picNumber: String? = null,
    @field:SerializedName("PhoneNumber")
    val phoneNumber: String? = null,
    @field:SerializedName("Village")
    val village: String? = null,
    @field:SerializedName("created_at")
    val createdAt: String? = null,
    @field:SerializedName("Latitude")
    val latitude: Any? = null,
    @field:SerializedName("Longitude")
    val longitude: Any? = null,
    @field:SerializedName("Province")
    val province: String? = null,
    @field:SerializedName("Name")
    val name: String? = null,
    @field:SerializedName("SubDistrict")
    val subDistrict: String? = null,
    @field:SerializedName("updated_at")
    val updatedAt: String? = null,
    @field:SerializedName("CityRegency")
    val cityRegency: String? = null,
    @field:SerializedName("OutletType")
    val outletType: String? = null,
    @field:SerializedName("ID")
    val iD: Int? = null,
    @field:SerializedName("id")
    val id: Int? = null
)
