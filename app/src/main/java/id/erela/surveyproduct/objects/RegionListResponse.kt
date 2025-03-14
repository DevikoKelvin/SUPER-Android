package id.erela.surveyproduct.objects

import com.google.gson.annotations.SerializedName

data class RegionListResponse(
    @field:SerializedName("code")
    val code: Int? = null,
    @field:SerializedName("data")
    val data: Data? = null,
    @field:SerializedName("message")
    val message: String? = null
)

data class Data(
    @field:SerializedName("cities")
    val cities: List<CitiesItem?>? = null,
    @field:SerializedName("villages")
    val villages: List<VillagesItem?>? = null,
    @field:SerializedName("districts")
    val districts: List<DistrictsItem?>? = null
)

data class CitiesItem(
    @field:SerializedName("name")
    val name: String? = null,
    @field:SerializedName("id")
    val id: Int? = null
)

data class DistrictsItem(
    @field:SerializedName("name")
    val name: String? = null,
    @field:SerializedName("id")
    val id: Int? = null
)

data class VillagesItem(
    @field:SerializedName("name")
    val name: String? = null,
    @field:SerializedName("id")
    val id: Long? = null
)
