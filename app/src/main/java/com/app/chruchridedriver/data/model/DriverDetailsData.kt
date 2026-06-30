package com.app.chruchridedriver.data.model

import java.io.Serializable

class DriverDetailsData(
    var imageUrl: String,
    var name: String,
    var dob: String,
    var emailAddress: String,
    var address: String,
    var city: String,
    var churchName: String,
    var zipCode: String,
    var mobileNumber: String,
    var gender: String,
    var state: String
) : Serializable