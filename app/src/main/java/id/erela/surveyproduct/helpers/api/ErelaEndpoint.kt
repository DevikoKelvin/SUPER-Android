package id.erela.surveyproduct.helpers.api

import id.erela.surveyproduct.objects.LoginResponse
import id.erela.surveyproduct.objects.UserListResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ErelaEndpoint {
    @POST("users/check")
    @FormUrlEncoded
    fun login(
       @Field("username") username: String,
       @Field("password") password: String
    ): Call<LoginResponse>

    /*@POST("users/list")
    @FormUrlEncoded
    fun getUserList(
        @Field("rowstart") rowStart: Int,
        @Field("rowdatas") rowDatas: Int
    ): Call<UserListResponse>*/
}