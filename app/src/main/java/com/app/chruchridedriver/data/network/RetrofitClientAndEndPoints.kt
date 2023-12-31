package com.app.chruchridedriver.data.network

import com.app.chruchridedriver.data.model.AcceptRideResponse
import com.app.chruchridedriver.data.model.ChurchDetails
import com.app.chruchridedriver.data.model.DocumentsResponse
import com.app.chruchridedriver.data.model.DriverDetailsIdResponse
import com.app.chruchridedriver.data.model.DriverRegisterationResponse
import com.app.chruchridedriver.data.model.LocationUpdatedData
import com.app.chruchridedriver.data.model.RegisteredDriver
import com.app.chruchridedriver.data.model.RideDetails
import com.app.chruchridedriver.data.model.SendOTResponse
import com.app.chruchridedriver.data.model.UploadedDocStatus
import com.app.chruchridedriver.data.model.UploadedDocument
import com.app.chruchridedriver.data.model.UploadedDocumentImage
import com.app.chruchridedriver.data.model.VerifiedStatus
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

    @GET("accepttrip.php")
    suspend fun getAcceptResponse(
        @Query("ride_id") ride_id: String,
        @Query("driverId") driverId: String
    ): Response<AcceptRideResponse>

    @GET("login.php")
    suspend fun getDriverId(
        @Query("mobileNumber") mobileNumber: String, @Query("fcmid") token: String
    ): Response<DriverDetailsIdResponse>

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

    @GET("uploadeddocumentstatus.php")
    suspend fun uploadeddocumentstatus(
        @Query("driverId") driverId: String
    ): Response<UploadedDocument>

    @GET("updatedocumentimage.php")
    suspend fun updateDocument(
        @Query("documentId") documentId: String, @Query("imageUrl") imageUrl: String
    ): Response<UploadedDocumentImage>

    @GET("newregdriverlist.php")
    suspend fun getRegisteredDriverRecent(
        @Query("sortBy") sortBy: String
    ): Response<RegisteredDriver>

    @GET("updatedocstatus.php")
    suspend fun updateDocStatus(
        @Query("documentId") documentId: String,
        @Query("docstatus") docstatus: String,
        @Query("comment") comment: String
    ): Response<UploadedDocStatus>

    @GET("updatedrivertripstatus.php")
    suspend fun getVerified(
        @Query("driverId") driverId: String, @Query("verified") verified: String
    ): Response<VerifiedStatus>


    @GET("insertorupdatelocation.php")
    suspend fun updateCurrentLocation(
        @Query("driverid") driverid: String,
        @Query("latitude") latitude: String,
        @Query("longitude") longitude: String,
        @Query("activestatus") activestatus: String
    ): Response<LocationUpdatedData>

    @GET("bookeddata.php")
    suspend fun getRideDetails(
        @Query("driverId") driverId: String
    ): Response<RideDetails>


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