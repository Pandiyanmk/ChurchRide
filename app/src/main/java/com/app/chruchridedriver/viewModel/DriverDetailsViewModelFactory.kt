package com.app.chruchridedriver.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.chruchridedriver.repository.MainRepository

@Suppress("UNCHECKED_CAST")
class DriverDetailsViewModelFactory constructor(private val authCheckRepository: MainRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(DriverDetailPageViewModel::class.java)) {
            DriverDetailPageViewModel(this.authCheckRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}