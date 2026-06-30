package com.app.chruchridedriver.data.model

import java.io.Serializable

class VehiclesDetailsData(
    var type: String,
    var make: String,
    var model: String,
    var year: String,
    var color: String,
    var doors: String,
    var seats: String,
) : Serializable