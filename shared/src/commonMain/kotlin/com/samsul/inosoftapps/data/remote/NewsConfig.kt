package com.samsul.inosoftapps.data.remote

/**
 * Global configuration constants for News API.
 */
object NewsConfig {
    const val BASE_URL = "https://newsapi.org/v2"

    /**
     * Default country to fetch headlines for.
     */
    const val DEFAULT_COUNTRY = "us"

    /**
     * Secondary fallback country if requested country returns empty articles.
     */
    const val FALLBACK_COUNTRY = "us"

    /**
     * Default API key holder. Can be customized or injected via build configuration / runtime.
     */
    var apiKey: String = "YOUR_NEWS_API_KEY_HERE"
}
