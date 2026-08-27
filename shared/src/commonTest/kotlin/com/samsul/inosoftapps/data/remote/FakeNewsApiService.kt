package com.samsul.inosoftapps.data.remote

import com.samsul.inosoftapps.data.remote.dto.ArticleDto
import com.samsul.inosoftapps.data.remote.dto.NewsResponseDto
import com.samsul.inosoftapps.data.remote.dto.SourceDto

/**
 * Fake implementation of NewsApiService for hermetic offline testing.
 */
class FakeNewsApiService : NewsApiService {

    var shouldThrowException: Exception? = null
    var fakeArticles: List<ArticleDto> = listOf(
        ArticleDto(
            source = SourceDto(id = "1", name = "Kompas"),
            author = "Samsul",
            title = "KMP Room 2.7 Released",
            description = "Kotlin Multiplatform Room 2.7 is now available.",
            url = "https://kompas.com/kmp-room",
            urlToImage = "https://picsum.photos/200",
            publishedAt = "2026-08-27T10:00:00Z",
            content = "Full content of KMP room release."
        )
    )

    override suspend fun getTopHeadlines(
        country: String,
        category: String?,
        page: Int,
        pageSize: Int
    ): NewsResponseDto {
        shouldThrowException?.let { throw it }

        return NewsResponseDto(
            status = "ok",
            totalResults = fakeArticles.size,
            articles = fakeArticles
        )
    }

    override suspend fun searchNews(
        query: String,
        page: Int,
        pageSize: Int
    ): NewsResponseDto {
        shouldThrowException?.let { throw it }

        val filtered = fakeArticles.filter {
            it.title?.contains(query, ignoreCase = true) == true ||
                    it.description?.contains(query, ignoreCase = true) == true
        }

        return NewsResponseDto(
            status = "ok",
            totalResults = filtered.size,
            articles = filtered
        )
    }
}
