package com.app.chruchridedriver.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.chruchridedriver.repository.MainRepository

@Suppress("UNCHECKED_CAST")
class LoginPageViewModelFactory constructor(private val authCheckRepository: MainRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(LoginPageViewModel::class.java)) {
            LoginPageViewModel(this.authCheckRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}