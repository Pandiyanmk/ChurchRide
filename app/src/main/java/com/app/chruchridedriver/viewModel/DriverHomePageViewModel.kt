package com.app.chruchridedriver.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.chruchridedriver.data.model.LocationUpdatedData
import com.app.chruchridedriver.data.model.RideDetails
import com.app.chruchridedriver.repository.MainRepository
import com.app.chruchridedriver.util.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class DriverHomePageViewModel constructor(private val authCheckRepository: MainRepository) :
    ViewModel() {


    private val _responseContent = MutableLiveData<LocationUpdatedData>()
    val responseContent: LiveData<LocationUpdatedData>
        get() = _responseContent

    private val _rideDetailsResponseContent = MutableLiveData<RideDetails>()
    val rideDetailsResponseContent: LiveData<RideDetails>
        get() = _rideDetailsResponseContent


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _rideerrorMessage = MutableLiveData<String>()
    val rideerrorMessage: LiveData<String>
        get() = _rideerrorMessage


    fun updateCurrentLocation(
        driverId: String, latitude: String, longitude: String, activestatus: String
    ) {
        viewModelScope.launch {
            authCheckRepository.updateCurrentLocation(driverId, latitude, longitude, activestatus)
                .flowOn(Dispatchers.IO).catch { }.collect { response ->
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


    fun getRideDetails(
        driverId: String
    ) {
        viewModelScope.launch {
            authCheckRepository.getRideDetails(driverId).flowOn(Dispatchers.IO).catch { }
                .collect { response ->
                    when (response) {
                        is NetworkState.Success -> {
                            _rideDetailsResponseContent.value = response.data!!
                        }

                        is NetworkState.Error -> {
                            _rideerrorMessage.value = response.errorMessage
                        }
                    }
                }
        }
    }
}