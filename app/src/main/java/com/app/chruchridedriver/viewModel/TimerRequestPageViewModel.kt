package com.app.chruchridedriver.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.chruchridedriver.data.model.AcceptRideResponse
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class TimerRequestPageViewModel constructor(private val authCheckRepository: MainRepository) :
    ViewModel() {
    private val _responseContent = MutableLiveData<AcceptRideResponse>()
    val responseContent: LiveData<AcceptRideResponse>
        get() = _responseContent

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage


    /* Get Login Content From Api */
    fun getAcceptResponse(ride_id: String, driverId: String) {
        viewModelScope.launch {
            authCheckRepository.getAcceptResponse(ride_id, driverId).flowOn(Dispatchers.IO)
                .catch { }.collect { response ->
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
}