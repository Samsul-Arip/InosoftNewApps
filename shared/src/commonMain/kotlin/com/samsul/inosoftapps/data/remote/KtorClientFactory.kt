package com.samsul.inosoftapps.data.remote

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

    private const val TIMEOUT_MILLIS = 15_000L

    /**
     * Builds a configured [HttpClient].
     * @param engine Optional [HttpClientEngine] (e.g. OkHttp, Darwin, or MockEngine for tests).
     * @param apiKey The NewsAPI authorization key.
     */
    fun createHttpClient(
        engine: HttpClientEngine? = null,
        apiKey: String = NewsConfig.apiKey
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

            // HTTP Timeouts (15 seconds)
            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT_MILLIS
                connectTimeoutMillis = TIMEOUT_MILLIS
                socketTimeoutMillis = TIMEOUT_MILLIS
            }

            // Network Logging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            // Default Request Configuration (Headers & Base settings)
            defaultRequest {
                if (apiKey.isNotBlank() && apiKey != "YOUR_NEWS_API_KEY_HERE") {
                    header("X-Api-Key", apiKey)
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
