package com.app.chruchridedriver.data.model

data class RequestDetails(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean
)