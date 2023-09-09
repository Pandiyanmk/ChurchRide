package com.app.chruchridedriver.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.chruchridedriver.data.model.ChurchDetails
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class DriverDetailPageViewModel constructor(private val authCheckRepository: MainRepository) :
    ViewModel() {


    val loading = MutableLiveData<Boolean>()

    private val _responseContent = MutableLiveData<ChurchDetails>()
    val responseContent: LiveData<ChurchDetails>
        get() = _responseContent


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    /* Get Church Content From Api */
    fun getChurchResponse() {
        viewModelScope.launch {
            authCheckRepository.getChurchResponse().flowOn(Dispatchers.IO).catch { }
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

    private fun stopLoader() {
        loading.value = false
    }
}