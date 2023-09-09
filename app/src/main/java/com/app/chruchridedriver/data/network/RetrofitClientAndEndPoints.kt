package com.app.chruchridedriver.data.network

import com.app.chruchridedriver.data.model.ChurchDetails
import com.app.chruchridedriver.data.model.DocumentsResponse
import com.app.chruchridedriver.data.model.DriverRegisterationResponse
import com.app.chruchridedriver.data.model.SendOTResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap


interface RetrofitClientAndEndPoints {


    @GET("sendMessage.php")
    suspend fun getLoginResponse(
        @Query("mobileNumber") mobileNumber: String
    ): Response<SendOTResponse>

    @GET("registerdetails.php")
    suspend fun registerDetails(
        @QueryMap params: Map<String, String>
    ): Response<DriverRegisterationResponse>

    @GET("churchname.php")
    suspend fun getChurchResponse(
    ): Response<ChurchDetails>

    @GET("documentlist.php")
    suspend fun getDocumentResponse(
    ): Response<DocumentsResponse>

    /* Building Retrofit with Base URL */
    companion object {

        private var retrofitService: RetrofitClientAndEndPoints? = null
        fun getInstance(): RetrofitClientAndEndPoints {
            if (retrofitService == null) {
                val retrofit = Retrofit.Builder().baseUrl("https://churchride.000webhostapp.com/")
                    .addConverterFactory(GsonConverterFactory.create()).build()
                retrofitService = retrofit.create(RetrofitClientAndEndPoints::class.java)
            }
            return retrofitService!!
        }

    }
}