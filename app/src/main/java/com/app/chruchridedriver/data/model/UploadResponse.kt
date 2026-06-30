package com.app.chruchridedriver.data.model

data class UploadResponse(
    val status: Boolean,
    val message: String,
    val image: String?
)