package com.app.chruchridedriver.repository

import android.net.Uri
import com.app.chruchridedriver.data.model.ChurchDetails
import com.app.chruchridedriver.data.model.DocumentsResponse
import com.app.chruchridedriver.data.model.DriverDetailsIdResponse
import com.app.chruchridedriver.data.model.DriverRegisterationResponse
import com.app.chruchridedriver.data.model.RegisteredDriver
import com.app.chruchridedriver.data.model.SendOTResponse
import com.app.chruchridedriver.data.model.UploadedDocStatus
import com.app.chruchridedriver.data.model.UploadedDocument
import com.app.chruchridedriver.data.model.UploadedDocumentImage
import com.app.chruchridedriver.data.model.VerifiedStatus
import com.app.chruchridedriver.data.network.RetrofitClientAndEndPoints
import com.app.chruchridedriver.util.NetworkState
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.greenrobot.eventbus.EventBus
import java.util.UUID


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

    /* upload Image To Firebase */
    suspend fun uploadImageToFirebase(
        storageReference: StorageReference, imageUri: Uri
    ) {
        val ref: StorageReference = storageReference.child(
            "images/" + UUID.randomUUID().toString()
        )
        ref.putFile(imageUri).addOnSuccessListener {
            val result = it.metadata!!.reference!!.downloadUrl
            result.addOnSuccessListener { uploadedImageURi ->
                val imageLink = uploadedImageURi.toString()
                EventBus.getDefault().post(imageLink)
            }
        }.addOnFailureListener { e ->
            EventBus.getDefault().post("Failed$e")
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
            EventBus.getDefault().post("isLoading" + progress.toInt() + "%")
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
}