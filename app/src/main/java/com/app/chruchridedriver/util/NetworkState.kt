package com.app.chruchridedriver.util

/* Contain Success And Error Response */
sealed class NetworkState<out T> {
    data class Success<out T>(val data: T) : NetworkState<T>()
    data class Error<T>(val errorMessage: String) : NetworkState<T>()
}