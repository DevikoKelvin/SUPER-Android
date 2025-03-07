package id.erela.surveyproduct.helpers.api

import id.erela.surveyproduct.objects.UserDetailResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface SuperEndpoint {
    @FormUrlEncoded
    @POST("user/getUserByUsername")
    fun getUserByUsername(
        @Field("username") username: String?
    ): Call<UserDetailResponse>
}