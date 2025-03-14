package id.erela.surveyproduct.helpers.api

import id.erela.surveyproduct.objects.OutletCategoryResponse
import id.erela.surveyproduct.objects.OutletListResponse
import id.erela.surveyproduct.objects.ProvinceListResponse
import id.erela.surveyproduct.objects.RegionListResponse
import id.erela.surveyproduct.objects.SurveyListResponse
import id.erela.surveyproduct.objects.UserDetailResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SuperEndpoint {
    @POST("user/getUserByUsername")
    @FormUrlEncoded
    fun getUserByUsername(
        @Field("username") username: String?
    ): Call<UserDetailResponse>

    @POST("survey/getActive")
    fun showAllSurveys(): Call<SurveyListResponse>

    @POST("outlet")
    fun showAllOutlets(): Call<OutletListResponse>

    @POST("outlet/category")
    fun showAllOutletCategories(): Call<OutletCategoryResponse>

    @POST("outlet/provinceList")
    fun showAllProvinces(): Call<ProvinceListResponse>

    @POST("outlet/regionList")
    @FormUrlEncoded
    fun showRegionList(
        @Field("provinces_id") provincesId: Int,
        @Field("cities_id") citiesId: Int?,
        @Field("districts_id") districtsId: Int?
    ): Call<RegionListResponse>
}