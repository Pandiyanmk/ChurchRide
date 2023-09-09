package com.app.chruchridedriver.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.chruchridedriver.data.model.DocumentsResponse
import com.app.chruchridedriver.data.model.DriverRegisterationResponse
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class DocumentPageViewModel constructor(private val authCheckRepository: MainRepository) :
    ViewModel() {


    val loading = MutableLiveData<Boolean>()

    private val _responseContent = MutableLiveData<DocumentsResponse>()
    val responseContent: LiveData<DocumentsResponse>
        get() = _responseContent


    private val _registerContent = MutableLiveData<DriverRegisterationResponse>()
    val registerContent: LiveData<DriverRegisterationResponse>
        get() = _registerContent


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    /* Get Document Content From Api */
    fun getDocumentResponse() {
        viewModelScope.launch {
            authCheckRepository.getDocumentResponse().flowOn(Dispatchers.IO).catch { }
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

    //Register Driver Details
    fun registerDriverDetails(map: MutableMap<String, String>) {
        viewModelScope.launch {
            authCheckRepository.registerDriverDetails(map).flowOn(Dispatchers.IO).catch { }
                .collect { response ->
                    stopLoader()
                    when (response) {
                        is NetworkState.Success -> {
                            _registerContent.value = response.data!!
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