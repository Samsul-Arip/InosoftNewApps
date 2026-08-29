package com.samsul.inosoftapps.data.remote.config

import com.samsul.inosoftapps.config.BuildKonfig
import com.samsul.inosoftapps.util.AppConstants

/**
 * Interface defining dynamic API, environment, and storage configuration properties.
 */
interface ApiConfigProvider {
    val baseUrl: String
    val apiKey: String
    val databaseName: String
    val defaultCountry: String
    val fallbackCountry: String
}

/**
 * Default implementation backed by generated [BuildKonfig] for sensitive properties
 * and [AppConstants] for static defaults.
 */
class DefaultApiConfigProvider(
    override val baseUrl: String = BuildKonfig.BASE_URL,
    override val apiKey: String = BuildKonfig.API_KEY,
    override val databaseName: String = BuildKonfig.DATABASE_NAME,
    override val defaultCountry: String = AppConstants.DEFAULT_COUNTRY,
    override val fallbackCountry: String = AppConstants.FALLBACK_COUNTRY
) : ApiConfigProvider
