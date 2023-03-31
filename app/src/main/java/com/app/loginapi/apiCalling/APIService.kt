package com.app.loginapi.apiCalling


import com.app.loginapi.LoginRequest
import com.app.loginapi.LoginResponse
import com.app.loginapi.MediaUploadResponse
import com.app.loginapi.SocialLoginRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface APIService {

    //Auth
    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse?>

    @POST("auth/socialLogin")
    fun socialLogin(@Body socialLoginRequest: SocialLoginRequest): Call<LoginResponse?>

    // Media Upload
    @Multipart
    @POST("common/mediaUpload")
    fun mediaUpload(
    @Part("langType") fullName: RequestBody?,
    @Part files: MultipartBody.Part,
    ): Call<MediaUploadResponse?>?
//    @Multipart
//    @POST("common/mediaUpload")
//    fun mediaUpload(
//        @Part("langType") fullName: RequestBody?,
//        @Part files: MultipartBody.Part
//    ): Call<MediaUploadResponse?>?
}