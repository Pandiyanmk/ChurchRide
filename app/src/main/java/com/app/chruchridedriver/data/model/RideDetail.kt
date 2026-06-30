package com.app.chruchridedriver.data.model

data class RideDetail(
    val booking_time: String,
    val drop_address: String,
    val drop_lat: String,
    val drop_long: String,
    val id: String,
    val mobileno: String,
    val pickpup_lat: String,
    val pickup_address: String,
    val pickup_long: String,
    val profilepic: String,
    val rating: String,
    val ride_id: String,
    val trip_status: String,
    val userName: String,
    val userbooked: String
)