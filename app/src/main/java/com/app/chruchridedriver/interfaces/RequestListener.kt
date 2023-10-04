package com.app.chruchridedriver.interfaces

interface RequestListener {
    fun start(id: Int)
    fun stop(id: Int, currentMs: Long)
    fun reset(id: Int)
    fun delete(id: String)
}