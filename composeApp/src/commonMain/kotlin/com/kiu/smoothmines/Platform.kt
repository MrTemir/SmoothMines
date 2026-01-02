package com.kiu.smoothmines

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform