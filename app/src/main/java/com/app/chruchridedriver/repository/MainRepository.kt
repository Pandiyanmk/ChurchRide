package com.app.chruchridedriver.repository

import android.util.Log
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
import com.app.chruchridedriver.data.network.RetrofitClientAndEndPoints
import com.app.chruchridedriver.util.NetworkState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.io.File


class MainRepository {


    /* API CALL To Get Login Response */
    suspend fun getLoginResponse(
        mobileNumber: String
    ): Flow<NetworkState<SendOTResponse>> {
        try {
            val response = RetrofitClientAndEndPoints.getInstance().getLoginResponse(mobileNumber)

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    /* API CALL To Get Accept Response */
    suspend fun getAcceptResponse(
        ride_id: String, driverId: String
    ): Flow<NetworkState<AcceptRideResponse>> {
        try {
            val response =
                RetrofitClientAndEndPoints.getInstance().getAcceptResponse(ride_id, driverId)

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    /* API CALL To Get Church Response */
    suspend fun getChurchResponse(
    ): Flow<NetworkState<ChurchDetails>> {
        try {
            val response = RetrofitClientAndEndPoints.getInstance().getChurchResponse()

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    /* API CALL To Get Document Response */
    suspend fun getDocumentResponse(
    ): Flow<NetworkState<DocumentsResponse>> {
        try {
            val response = RetrofitClientAndEndPoints.getInstance().getDocumentResponse()

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    /* API CALL To Get Document Response */
    suspend fun registerDriverDetails(
        params: Map<String, String>
    ): Flow<NetworkState<DriverRegisterationResponse>> {
        try {
            val response = RetrofitClientAndEndPoints.getInstance().registerDetails(params)

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    fun uploadImage(imagePath: String) {
        val file = File(imagePath)
        val requestBody = RequestBody.create(
            MediaType.parse("image/*"), file
        )
        val imagePart = MultipartBody.Part.createFormData(
            "image", file.name, requestBody
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val response = RetrofitClientAndEndPoints.getInstance().uploadImage(imagePart)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val uploadResponse = response.body()
                        if (uploadResponse?.status == true) {
                            val imageUrl =
                                RetrofitClientAndEndPoints.baseUrl + "uploads/${uploadResponse.image}"
                            Log.d("IMAGE_URL", imageUrl)
                            EventBus.getDefault().post(imageUrl)
                        }
                    } else {
                        EventBus.getDefault().post("Failed to upload image retry")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                EventBus.getDefault().post("Failed$e")
            }
        }
    }

    /* API CALL To Get Document Response */
    suspend fun updateDocument(
        documentId: String, imageUrl: String
    ): Flow<NetworkState<UploadedDocumentImage>> {
        try {
            val response =
                RetrofitClientAndEndPoints.getInstance().updateDocument(documentId, imageUrl)

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    /* API CALL To Get Document Response */
    suspend fun getUploadedDocumentResponse(
        driverId: String
    ): Flow<NetworkState<UploadedDocument>> {
        try {
            val response = RetrofitClientAndEndPoints.getInstance().uploadeddocumentstatus(driverId)

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    /* API CALL To Get Login Response */
    suspend fun getDriverId(
        mobileNumber: String, token: String
    ): Flow<NetworkState<DriverDetailsIdResponse>> {
        try {
            val response = RetrofitClientAndEndPoints.getInstance().getDriverId(mobileNumber, token)

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    /* API CALL To Get Registered Driver Response */
    suspend fun getRegisteredDriverRecent(
        sortBy: String
    ): Flow<NetworkState<RegisteredDriver>> {
        try {
            val response =
                RetrofitClientAndEndPoints.getInstance().getRegisteredDriverRecent(sortBy)

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    /* API CALL To update Document Status */
    suspend fun updateDocStatus(
        documentId: String, docStatus: String, comment: String
    ): Flow<NetworkState<UploadedDocStatus>> {
        try {
            val response = RetrofitClientAndEndPoints.getInstance()
                .updateDocStatus(documentId, docStatus, comment)

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    /* API CALL To Get Document Response */
    suspend fun getDriverVerifiedStatus(
        driverId: String, verified: String
    ): Flow<NetworkState<VerifiedStatus>> {
        try {
            val response = RetrofitClientAndEndPoints.getInstance().getVerified(driverId, verified)

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    /* API CALL To Get Document Response */
    suspend fun updateCurrentLocation(
        driverId: String, latitude: String, longitude: String, activestatus: String
    ): Flow<NetworkState<LocationUpdatedData>> {
        try {
            val response = RetrofitClientAndEndPoints.getInstance()
                .updateCurrentLocation(driverId, latitude, longitude, activestatus)

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }

    suspend fun getRideDetails(
        driverId: String
    ): Flow<NetworkState<RideDetails>> {
        try {
            val response = RetrofitClientAndEndPoints.getInstance().getRideDetails(driverId)

            return if (response.isSuccessful) {
                val responseBody = response.body()
                if (responseBody != null) {
                    flow {
                        emit(NetworkState.Success(responseBody))
                    }
                } else {
                    flow {
                        emit(NetworkState.Error(response.message()))
                    }
                }
            } else {
                flow {
                    emit(NetworkState.Error(response.message()))
                }
            }
        } catch (e: Exception) {
            return flow {
                emit(NetworkState.Error(e.toString()))
            }
        }
    }
}