package com.app.chruchridedriver.data.network

import com.app.chruchridedriver.data.model.SendOTResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface RetrofitClientAndEndPoints {


    @GET("sendMessage.php")
    suspend fun getLoginResponse(
        @Query("mobileNumber") mobileNumber: String
    ): Response<SendOTResponse>

    /* Building Retrofit with Base URL */
    companion object {

        private var retrofitService: RetrofitClientAndEndPoints? = null
        fun getInstance(): RetrofitClientAndEndPoints {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder().baseUrl("http://192.168.0.104/Dentz/churchride/")
                    .addConverterFactory(GsonConverterFactory.create()).build()
                retrofitService = retrofit.create(RetrofitClientAndEndPoints::class.java)
            }
            return retrofitService!!
        }

    }
}