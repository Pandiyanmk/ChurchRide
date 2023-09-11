package com.app.chruchridedriver.data.model

import android.net.Uri

data class Document(
    val content: String,
    val id: String,
    val name: String,
    var uploaded: Int = 0,
    var httpImage: String = "",
    var pathOfImage: Uri? = null
)