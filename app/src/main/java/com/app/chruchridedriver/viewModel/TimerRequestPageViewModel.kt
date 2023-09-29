package com.app.chruchridedriver.viewModel

import androidx.lifecycle.ViewModel
import com.app.chruchridedriver.repository.MainRepository

class TimerRequestPageViewModel constructor(private val authCheckRepository: MainRepository) :
    ViewModel() {}