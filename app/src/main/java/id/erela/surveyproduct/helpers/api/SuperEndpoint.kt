package id.erela.surveyproduct.helpers.api

import id.erela.surveyproduct.objects.SurveyListResponse
import id.erela.surveyproduct.objects.UserDetailResponse
import id.erela.surveyproduct.objects.UserListResponse
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

    @POST("user")
    fun showAllUsers(): Call<UserListResponse>

    @POST("survey/getActive")
    fun showAllSurveys(): Call<SurveyListResponse>
}