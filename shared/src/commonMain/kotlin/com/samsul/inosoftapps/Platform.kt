package com.samsul.inosoftapps

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform