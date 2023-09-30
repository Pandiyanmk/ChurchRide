package com.app.chruchridedriver.data.model

data class RequestDetails(
    val bookingId: Int,
    var currentMs: Long,
    var isStarted: Boolean,
    var pickupLat: Double,
    var pickupLong: Double,
    var dropLat: Double,
    var dropLong: Double,
    var userName: String,
    var userRating: String,
    var estimatedTime: String,
    var estimatedMiles: String,
    var pickupAddress: String,
    var dropAddress: String
)