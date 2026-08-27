package com.samsul.inosoftapps.data.remote

import com.samsul.inosoftapps.data.remote.dto.NewsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Service interface for communicating with the News API.
 */
interface NewsApiService {
    /**
     * Fetches top headlines with smart fallback support.
     * @param country 2-letter ISO 3166-1 code (e.g., "id", "us").
     * @param category Category filter (e.g., "business", "technology").
     * @param page Page number for pagination.
     * @param pageSize Number of results per page (default 20).
     */
    suspend fun getTopHeadlines(
        country: String = NewsConfig.DEFAULT_COUNTRY,
        category: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): NewsResponseDto

    /**
     * Searches articles by keyword query across all sources.
     * @param query Search query text.
     * @param page Page number for pagination.
     * @param pageSize Number of results per page (default 20).
     */
    suspend fun searchNews(
        query: String,
        page: Int = 1,
        pageSize: Int = 20
    ): NewsResponseDto
}

/**
 * Default implementation of [NewsApiService] using Ktor [HttpClient].
 */
class KtorNewsApiService(
    private val client: HttpClient,
    private val baseUrl: String = NewsConfig.BASE_URL
) : NewsApiService {

    override suspend fun getTopHeadlines(
        country: String,
        category: String?,
        page: Int,
        pageSize: Int
    ): NewsResponseDto {
        val primaryResponse = fetchHeadlines(country = country, category = category, page = page, pageSize = pageSize)

        // Smart Fallback: If country is not fallback country (e.g. 'id') and returns empty articles or error,
        // automatically fallback to global/US headlines so the user always receives fresh news.
        if (country != NewsConfig.FALLBACK_COUNTRY && (primaryResponse.articles.isNullOrEmpty() || primaryResponse.status != "ok")) {
            val fallbackResponse = fetchHeadlines(
                country = NewsConfig.FALLBACK_COUNTRY,
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
            if (NewsConfig.apiKey.isNotBlank() && NewsConfig.apiKey != "YOUR_NEWS_API_KEY_HERE") {
                parameter("apiKey", NewsConfig.apiKey)
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
            if (NewsConfig.apiKey.isNotBlank() && NewsConfig.apiKey != "YOUR_NEWS_API_KEY_HERE") {
                parameter("apiKey", NewsConfig.apiKey)
            }
        }.body()
    }
}
