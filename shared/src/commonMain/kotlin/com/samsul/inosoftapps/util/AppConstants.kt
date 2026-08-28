package com.samsul.inosoftapps.util

/**
 * Centralized application constants and default configurations.
 * Prevents magic values and provides single point of configuration.
 */
object AppConstants {

    // Database Configuration
    const val DATABASE_NAME: String = "news_reader.db"

    // Network & Timeout Configuration
    const val NETWORK_TIMEOUT_MILLIS: Long = 15_000L
    const val DEFAULT_PAGE_SIZE: Int = 7
    const val INITIAL_PAGE: Int = 1

    // API Country Codes
    const val DEFAULT_COUNTRY: String = "id"
    const val FALLBACK_COUNTRY: String = "us"

    // Category Keys (NewsAPI query parameters)
    const val CATEGORY_BUSINESS = "business"
    const val CATEGORY_TECHNOLOGY = "technology"
    const val CATEGORY_SPORTS = "sports"
    const val CATEGORY_HEALTH = "health"
    const val CATEGORY_SCIENCE = "science"
    const val CATEGORY_ENTERTAINMENT = "entertainment"

    // News Categories (Key to Display Name mapping)
    val CATEGORIES: List<Pair<String?, String>> = listOf(
        null to AppStrings.CATEGORY_ALL_LABEL,
        CATEGORY_BUSINESS to AppStrings.CATEGORY_BUSINESS_LABEL,
        CATEGORY_TECHNOLOGY to AppStrings.CATEGORY_TECHNOLOGY_LABEL,
        CATEGORY_SPORTS to AppStrings.CATEGORY_SPORTS_LABEL,
        CATEGORY_HEALTH to AppStrings.CATEGORY_HEALTH_LABEL,
        CATEGORY_SCIENCE to AppStrings.CATEGORY_SCIENCE_LABEL,
        CATEGORY_ENTERTAINMENT to AppStrings.CATEGORY_ENTERTAINMENT_LABEL
    )

    // API Constants & Endpoints
    const val API_STATUS_OK: String = "ok"
    const val ENDPOINT_TOP_HEADLINES: String = "top-headlines"
    const val ENDPOINT_EVERYTHING: String = "everything"

    // API Query Parameters & Headers
    const val PARAM_COUNTRY: String = "country"
    const val PARAM_CATEGORY: String = "category"
    const val PARAM_PAGE: String = "page"
    const val PARAM_PAGE_SIZE: String = "pageSize"
    const val PARAM_API_KEY: String = "apiKey"
    const val PARAM_QUERY: String = "q"
    const val PARAM_SORT_BY: String = "sortBy"
    const val SORT_BY_PUBLISHED_AT: String = "publishedAt"
    const val HEADER_API_KEY: String = "X-Api-Key"

    const val REMOVED_ARTICLE_TITLE: String = "[Removed]"

    // Known network error substrings for mapping exceptions
    val NETWORK_ERROR_KEYWORDS: List<String> = listOf(
        "Unable to resolve host",
        "No address associated",
        "ConnectException",
        "Network is unreachable",
        "connection abort"
    )
}
