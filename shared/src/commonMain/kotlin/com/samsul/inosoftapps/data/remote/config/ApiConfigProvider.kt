package com.samsul.inosoftapps.data.remote.config

import com.samsul.inosoftapps.config.BuildKonfig

/**
 * Interface defining dynamic API and environment configuration properties.
 */
interface ApiConfigProvider {
    val baseUrl: String
    val apiKey: String
    val defaultCountry: String
    val fallbackCountry: String
}

/**
 * Default implementation backed by generated [BuildKonfig] with safe fallbacks.
 */
class DefaultApiConfigProvider(
    override val baseUrl: String = BuildKonfig.BASE_URL,
    override val apiKey: String = BuildKonfig.API_KEY,
    override val defaultCountry: String = BuildKonfig.DEFAULT_COUNTRY,
    override val fallbackCountry: String = "us"
) : ApiConfigProvider
