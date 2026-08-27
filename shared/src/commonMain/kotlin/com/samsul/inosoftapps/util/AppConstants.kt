package com.samsul.inosoftapps.util

/**
 * Centralized application constants and default configurations.
 * Prevents magic values and provides single point of configuration.
 */
object AppConstants {

    // Database Configuration
    const val DATABASE_NAME: String = "news_reader.db"

    // Network Configuration
    const val NETWORK_TIMEOUT_MILLIS: Long = 15_000L
    const val DEFAULT_PAGE_SIZE: Int = 7

    // News Categories (Key to Display Name)
    val CATEGORIES: List<Pair<String?, String>> = listOf(
        null to "Semua",
        "business" to "Bisnis",
        "technology" to "Teknologi",
        "sports" to "Olahraga",
        "health" to "Kesehatan",
        "science" to "Sains",
        "entertainment" to "Hiburan"
    )
}
