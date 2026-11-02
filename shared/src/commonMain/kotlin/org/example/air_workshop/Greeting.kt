package org.example.air_workshop

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }

    fun getCurrentTime(): Long {
        return getCurrentEpochMillis()
    }
}