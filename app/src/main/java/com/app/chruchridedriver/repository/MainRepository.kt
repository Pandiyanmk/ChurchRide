package com.app.chruchridedriver.repository

import com.app.chruchridedriver.data.model.ChurchDetails
import com.app.chruchridedriver.data.model.SendOTResponse
import com.app.chruchridedriver.data.network.RetrofitClientAndEndPoints
import com.app.chruchridedriver.util.NetworkState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


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


}