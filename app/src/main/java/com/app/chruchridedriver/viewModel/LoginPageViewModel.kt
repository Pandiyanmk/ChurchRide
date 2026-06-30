package com.app.chruchridedriver.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.chruchridedriver.data.model.DriverDetailsIdResponse
import com.app.chruchridedriver.data.model.SendOTResponse
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class LoginPageViewModel constructor(private val authCheckRepository: MainRepository) :
    ViewModel() {


    val loading = MutableLiveData<Boolean>()

    private val _responseContent = MutableLiveData<SendOTResponse>()
    val responseContent: LiveData<SendOTResponse>
        get() = _responseContent

    private val _driverContent = MutableLiveData<DriverDetailsIdResponse>()
    val driverContent: LiveData<DriverDetailsIdResponse>
        get() = _driverContent


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    /* Get Login Content From Api */
    fun getLoginResponse(mobileNumber: String) {
        viewModelScope.launch {
            authCheckRepository.getLoginResponse(mobileNumber).flowOn(Dispatchers.IO).catch { }
                .collect { response ->
                    stopLoader()
                    when (response) {
                        is NetworkState.Success -> {
                            _responseContent.value = response.data!!
                        }

                        is NetworkState.Error -> {
                            _errorMessage.value = response.errorMessage
                        }
                    }
                }
        }
    }

    /* Get Login Content From Api */
    fun getDriverId(mobileNumber: String, token: String) {
        viewModelScope.launch {
            authCheckRepository.getDriverId(mobileNumber,token).flowOn(Dispatchers.IO).catch { }
                .collect { response ->
                    stopLoader()
                    when (response) {
                        is NetworkState.Success -> {
                            _driverContent.value = response.data!!
                        }

                        is NetworkState.Error -> {
                            _errorMessage.value = response.errorMessage
                        }
                    }
                }
        }
    }


    private fun stopLoader() {
        loading.value = false
    }
}