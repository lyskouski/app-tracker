package com.tercad.zwyka

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
