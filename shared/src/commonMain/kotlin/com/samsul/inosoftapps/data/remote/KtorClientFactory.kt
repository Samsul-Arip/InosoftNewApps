package com.samsul.inosoftapps.data.remote

import com.samsul.inosoftapps.data.remote.config.ApiConfigProvider
import com.samsul.inosoftapps.data.remote.config.DefaultApiConfigProvider
import com.samsul.inosoftapps.util.AppConstants
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory creating and configuring [HttpClient] instances for multiplatform networking.
 */
object KtorClientFactory {

    /**
     * Builds a configured [HttpClient].
     * @param engine Optional [HttpClientEngine] (e.g. OkHttp, Darwin, or MockEngine for tests).
     * @param configProvider Dynamic [ApiConfigProvider] supplying authorization and environment keys.
     */
    fun createHttpClient(
        engine: HttpClientEngine? = null,
        configProvider: ApiConfigProvider = DefaultApiConfigProvider()
    ): HttpClient {
        val config: HttpClientConfig<*>.() -> Unit = {
            // JSON Serialization
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = false
                        coerceInputValues = true
                        encodeDefaults = true
                    }
                )
            }

            // HTTP Timeouts
            install(HttpTimeout) {
                requestTimeoutMillis = AppConstants.NETWORK_TIMEOUT_MILLIS
                connectTimeoutMillis = AppConstants.NETWORK_TIMEOUT_MILLIS
                socketTimeoutMillis = AppConstants.NETWORK_TIMEOUT_MILLIS
            }

            // Network Logging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }

            // Default Request Configuration (Headers & Base settings)
            defaultRequest {
                val apiKey = configProvider.apiKey
                if (apiKey.isNotBlank()) {
                    header(AppConstants.HEADER_API_KEY, apiKey)
                }
            }
        }

        return if (engine != null) {
            HttpClient(engine, config)
        } else {
            HttpClient(config)
        }
    }
}
