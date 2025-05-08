package id.erela.surveyproduct.helpers.api

import id.erela.surveyproduct.objects.AnswerHistoryResponse
import id.erela.surveyproduct.objects.CheckInOutHistoryListResponse
import id.erela.surveyproduct.objects.CheckInResponse
import id.erela.surveyproduct.objects.CheckOutResponse
import id.erela.surveyproduct.objects.InsertAnswerResponse
import id.erela.surveyproduct.objects.OutletCategoryResponse
import id.erela.surveyproduct.objects.OutletCreationResponse
import id.erela.surveyproduct.objects.OutletListResponse
import id.erela.surveyproduct.objects.OutletResponse
import id.erela.surveyproduct.objects.ProvinceListResponse
import id.erela.surveyproduct.objects.RegionListResponse
import id.erela.surveyproduct.objects.SurveyListResponse
import id.erela.surveyproduct.objects.UserDetailResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap

interface SuperEndpoint {
    // Users
    @FormUrlEncoded
    @POST("user/byUsername")
    fun getUserByUsername(
        @Field("UserName") username: String?
    ): Call<UserDetailResponse>

    // Surveys
    @POST("survey/active")
    fun showAllSurveys(): Call<SurveyListResponse>

    @FormUrlEncoded
    @POST("survey/history")
    fun showAnswerHistory(
        @Field("AnswerGroupID") answerGroupID: Int
    ): Call<AnswerHistoryResponse>

    @Multipart
    @POST("survey/insert")
    fun insertAnswer(
        @Part("AnswerGroupID") answerGroupId: RequestBody,
        @Part answers: List<MultipartBody.Part>
    ): Call<InsertAnswerResponse>

    // Outlets
    @POST("outlet")
    fun showAllOutlets(): Call<OutletListResponse>

    @FormUrlEncoded
    @POST("outlet/byId")
    fun showOutletById(
        @Field("id") id: Int
    ): Call<OutletResponse>

    @POST("outlet/category")
    fun showAllOutletCategories(): Call<OutletCategoryResponse>

    @POST("outlet/provinceList")
    fun showAllProvinces(): Call<ProvinceListResponse>

    @FormUrlEncoded
    @POST("outlet/regionList")
    fun showRegionList(
        @Field("provinces_id") provincesId: Int,
        @Field("cities_id") citiesId: Int?,
        @Field("districts_id") districtsId: Int?
    ): Call<RegionListResponse>

    @FormUrlEncoded
    @POST("outlet/store")
    fun outletCreation(
        @Field("BranchID") branchID: Int,
        @Field("Name") outletName: String,
        @Field("OutletType") outletType: Int,
        @Field("UserID") userID: Int,
        @Field("Address") outletAddress: String,
        @Field("Province") outletProvince: Int,
        @Field("CityRegency") outletCityRegency: Int,
        @Field("SubDistrict") outletSubDistrict: Int,
        @Field("Village") outletVillage: Long,
        @Field("Latitude") latitude: Double,
        @Field("Longitude") longitude: Double
    ): Call<OutletCreationResponse>

    // Check
    @FormUrlEncoded
    @POST("check")
    fun showAllCheckInOut(
        @Field("UserID") userID: Int,
        @Field("StartDate") startDate: String?,
        @Field("EndDate") endDate: String?
    ): Call<CheckInOutHistoryListResponse>

    @FormUrlEncoded
    @POST("check/today")
    fun showTodayCheckInOut(
        @Field("UserID") userID: Int
    ): Call<CheckInOutHistoryListResponse>

    @Multipart
    @POST("check/in")
    fun checkIn(
        @PartMap data: MutableMap<String, RequestBody>,
        @Part photoIn: MultipartBody.Part
    ): Call<CheckInResponse>

    @Multipart
    @POST("check/out")
    fun checkOut(
        @PartMap data: MutableMap<String, RequestBody>,
        @Part photoOut: MultipartBody.Part?
    ): Call<CheckOutResponse>

    @Multipart
    @POST("check/out")
    fun checkOutNoPhoto(
        @PartMap data: MutableMap<String, RequestBody>
    ): Call<CheckOutResponse>
}