package com.samsul.inosoftapps.data.remote.mapper

import com.samsul.inosoftapps.data.remote.dto.ArticleDto
import com.samsul.inosoftapps.domain.model.Article

/**
 * Maps an [ArticleDto] to the domain [Article] entity.
 * Filters out removed or invalid articles without URL or title.
 */
fun ArticleDto.toDomain(category: String? = null): Article? {
    val articleUrl = url?.trim()
    val articleTitle = title?.trim()

    // Discard articles with missing essential info or marked as removed by NewsAPI
    if (articleUrl.isNullOrBlank() || articleTitle.isNullOrBlank() || articleTitle == "[Removed]") {
        return null
    }

    // Generate stable unique identifier based on URL hash
    val stableId = articleUrl.hashCode().toString()

    return Article(
        id = stableId,
        title = articleTitle,
        description = description?.trim(),
        content = content?.trim(),
        author = author?.trim(),
        url = articleUrl,
        imageUrl = urlToImage?.trim(),
        publishedAt = publishedAt?.trim().orEmpty(),
        sourceName = source?.name?.trim(),
        category = category,
        isBookmarked = false
    )
}

/**
 * Maps a list of [ArticleDto] to a list of valid domain [Article] entities.
 */
fun List<ArticleDto>?.toDomainList(category: String? = null): List<Article> {
    return this?.mapNotNull { it.toDomain(category) } ?: emptyList()
}
