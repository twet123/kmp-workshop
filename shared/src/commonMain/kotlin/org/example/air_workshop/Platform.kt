package org.example.air_workshop

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform