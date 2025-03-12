package id.erela.surveyproduct.helpers.api

import com.google.gson.GsonBuilder
import id.erela.surveyproduct.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object AppAPI {
    private val client = OkHttpClient().newBuilder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        })
        .addInterceptor(Interceptor {
            with(it) {
                return@Interceptor proceed(
                    request().newBuilder()
                        .addHeader(BuildConfig.KEY, BuildConfig.VALUE)
                        .build()
                )
            }
        })
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun getInstanceErelaApp(): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.ERELA_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()

    private fun getInstanceSuperApp(): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SUPER_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
        .build()

    val erelaEndpoint: ErelaEndpoint = getInstanceErelaApp().create(ErelaEndpoint::class.java)
    val superEndpoint: SuperEndpoint = getInstanceSuperApp().create(SuperEndpoint::class.java)
}