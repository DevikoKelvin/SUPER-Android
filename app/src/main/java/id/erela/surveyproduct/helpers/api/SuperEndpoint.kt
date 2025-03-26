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
    @POST("user/getUserByUsername")
    @FormUrlEncoded
    fun getUserByUsername(
        @Field("UserName") username: String?
    ): Call<UserDetailResponse>

    // Surveys
    @POST("survey/getActive")
    fun showAllSurveys(): Call<SurveyListResponse>

    @POST("survey/getTodayCheckInOut")
    @FormUrlEncoded
    fun showTodayCheckInOut(
        @Field("UserID") userID: Int
    ): Call<CheckInOutHistoryListResponse>

    @POST("survey/getAllCheckInOut")
    @FormUrlEncoded
    fun showAllCheckInOut(
        @Field("UserID") userID: Int,
        @Field("StartDate") startDate: String?,
        @Field("EndDate") endDate: String?
    ): Call<CheckInOutHistoryListResponse>

    @POST("survey/getAnswerHistory")
    @FormUrlEncoded
    fun showAnswerHistory(
        @Field("AnswerGroupID") answerGroupID: Int
    ): Call<AnswerHistoryResponse>

    // Outlets
    @POST("outlet")
    fun showAllOutlets(): Call<OutletListResponse>

    @POST("outlet/getOutletById")
    @FormUrlEncoded
    fun showOutletById(
        @Field("id") id: Int
    ): Call<OutletResponse>

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

    @POST("outlet/store")
    @FormUrlEncoded
    fun outletCreation(
        @Field("BranchID") branchID: Int,
        @Field("Name") outletName: String,
        @Field("OutletType") outletType: Int,
        @Field("Address") outletAddress: String,
        @Field("Province") outletProvince: Int,
        @Field("CityRegency") outletCityRegency: Int,
        @Field("SubDistrict") outletSubDistrict: Int,
        @Field("Village") outletVillage: Long,
        @Field("Latitude") latitude: Double,
        @Field("Longitude") longitude: Double
    ): Call<OutletCreationResponse>

    // Answers
    @POST("check/in")
    @Multipart
    fun checkIn(
        @PartMap data: Map<String, RequestBody>,
        @Part photoIn: MultipartBody.Part
    ): Call<CheckInResponse>

    @POST("survey/insertAnswer")
    @Multipart
    suspend fun insertAnswer(
        @Part("AnswerGroupID") answerGroupId: RequestBody,
        @Part answers: List<MultipartBody.Part>
    ): Response<InsertAnswerResponse>

    @POST("check/out")
    @Multipart
    fun checkOut(
        @PartMap data: Map<String, RequestBody>,
        @Part photoOut: MultipartBody.Part?
    ): Call<CheckOutResponse>
}