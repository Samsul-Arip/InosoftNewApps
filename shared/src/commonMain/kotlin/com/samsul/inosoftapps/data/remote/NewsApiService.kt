package com.samsul.inosoftapps.data.remote

import com.samsul.inosoftapps.data.remote.config.ApiConfigProvider
import com.samsul.inosoftapps.data.remote.config.DefaultApiConfigProvider
import com.samsul.inosoftapps.data.remote.dto.NewsResponseDto
import com.samsul.inosoftapps.util.AppConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Service interface for communicating with the News API.
 */
interface NewsApiService {
    /**
     * Fetches top headlines from NewsAPI.
     * @param country 2-letter ISO 3166-1 code (e.g., "id", "us"). Defaults to config provider value if blank.
     * @param category Category filter (e.g., "business", "technology").
     * @param page Page number for pagination.
     * @param pageSize Number of results per page (default [AppConstants.DEFAULT_PAGE_SIZE]).
     */
    suspend fun getTopHeadlines(
        country: String = "",
        category: String? = null,
        page: Int = 1,
        pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE
    ): NewsResponseDto

    /**
     * Searches articles by keyword query across all sources.
     * @param query Search query text.
     * @param page Page number for pagination.
     * @param pageSize Number of results per page (default [AppConstants.DEFAULT_PAGE_SIZE]).
     */
    suspend fun searchNews(
        query: String,
        page: Int = 1,
        pageSize: Int = AppConstants.DEFAULT_PAGE_SIZE
    ): NewsResponseDto
}

/**
 * Default implementation of [NewsApiService] using Ktor [HttpClient] and dynamic [ApiConfigProvider].
 */
class KtorNewsApiService(
    private val client: HttpClient,
    private val configProvider: ApiConfigProvider = DefaultApiConfigProvider()
) : NewsApiService {

    private val baseUrl: String get() = configProvider.baseUrl

    override suspend fun getTopHeadlines(
        country: String,
        category: String?,
        page: Int,
        pageSize: Int
    ): NewsResponseDto {
        val targetCountry = if (country.isNotBlank()) country else configProvider.defaultCountry
        val primaryResponse = fetchHeadlines(country = targetCountry, category = category, page = page, pageSize = pageSize)

        // Smart Fallback: If country returns empty articles or error (e.g. NewsAPI 'id' returning 0 articles),
        // automatically fallback to global/US headlines so the user always receives fresh news.
        if (targetCountry != configProvider.fallbackCountry && (primaryResponse.articles.isNullOrEmpty() || primaryResponse.status != "ok")) {
            val fallbackResponse = fetchHeadlines(
                country = configProvider.fallbackCountry,
                category = category,
                page = page,
                pageSize = pageSize
            )
            if (!fallbackResponse.articles.isNullOrEmpty()) {
                return fallbackResponse
            }
        }

        return primaryResponse
    }

    private suspend fun fetchHeadlines(
        country: String,
        category: String?,
        page: Int,
        pageSize: Int
    ): NewsResponseDto {
        return client.get("$baseUrl/top-headlines") {
            parameter("country", country)
            if (!category.isNullOrBlank()) {
                parameter("category", category)
            }
            parameter("page", page)
            parameter("pageSize", pageSize)
            val apiKey = configProvider.apiKey
            if (apiKey.isNotBlank()) {
                parameter("apiKey", apiKey)
            }
        }.body()
    }

    override suspend fun searchNews(
        query: String,
        page: Int,
        pageSize: Int
    ): NewsResponseDto {
        return client.get("$baseUrl/everything") {
            parameter("q", query)
            parameter("sortBy", "publishedAt")
            parameter("page", page)
            parameter("pageSize", pageSize)
            val apiKey = configProvider.apiKey
            if (apiKey.isNotBlank()) {
                parameter("apiKey", apiKey)
            }
        }.body()
    }
}
