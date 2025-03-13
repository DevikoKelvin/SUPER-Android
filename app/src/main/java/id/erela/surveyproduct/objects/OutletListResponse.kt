package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName
import java.io.Serializable

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
    val village: String? = null,
    @field:SerializedName("CreatedAt")
    val createdAt: String? = null,
    @field:SerializedName("Latitude")
    val latitude: Double? = null,
    @field:SerializedName("Longitude")
    val longitude: Double? = null,
    @field:SerializedName("UpdatedAt")
    val updatedAt: String? = null,
    @field:SerializedName("Province")
    val province: String? = null,
    @field:SerializedName("Name")
    val name: String? = null,
    @field:SerializedName("Type")
    val type: String? = null,
    @field:SerializedName("SubDistrict")
    val subDistrict: String? = null,
    @field:SerializedName("CityRegency")
    val cityRegency: String? = null,
    @field:SerializedName("ID")
    val iD: Int? = null
): Serializable
