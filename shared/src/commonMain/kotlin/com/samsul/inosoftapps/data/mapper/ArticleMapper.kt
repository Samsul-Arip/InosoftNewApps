package com.samsul.inosoftapps.data.mapper

import com.samsul.inosoftapps.data.local.entity.ArticleEntity
import com.samsul.inosoftapps.data.remote.dto.ArticleDto
import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.util.AppConstants
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Formats an ISO 8601 timestamp string (e.g. '2026-08-27T08:00:00Z') into a human-readable format,
 * e.g. '27 Agu 2026, 15:00'.
 */
fun formatIsoDate(isoDateString: String?): String {
    if (isoDateString.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(isoDateString.trim())
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
        )
        val day = localDateTime.dayOfMonth.toString().padStart(2, '0')
        val month = monthNames.getOrElse(localDateTime.monthNumber - 1) { localDateTime.month.name }
        val year = localDateTime.year
        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')
        "$day $month $year, $hour:$minute"
    } catch (e: Exception) {
        isoDateString.trim()
    }
}

/**
 * Maps remote [ArticleDto] to local database [ArticleEntity].
 * Discards removed or invalid articles.
 */
fun ArticleDto.toEntity(category: String? = null, cachedAt: Long = 0L): ArticleEntity? {
    val articleUrl = url?.trim()
    val articleTitle = title?.trim()

    if (articleUrl.isNullOrBlank() || articleTitle.isNullOrBlank() || articleTitle == AppConstants.REMOVED_ARTICLE_TITLE) {
        return null
    }

    val stableId = articleUrl.hashCode().toString()

    return ArticleEntity(
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
        isBookmarked = false,
        cachedAt = cachedAt
    )
}

/**
 * Maps a list of [ArticleDto] to [ArticleEntity] list.
 */
fun List<ArticleDto>?.toEntityList(category: String? = null, cachedAt: Long = 0L): List<ArticleEntity> {
    return this?.mapNotNull { it.toEntity(category, cachedAt) } ?: emptyList()
}

/**
 * Maps local database [ArticleEntity] to domain [Article] model with formatted publication date.
 */
fun ArticleEntity.toDomain(): Article {
    return Article(
        id = id,
        title = title,
        description = description,
        content = content,
        author = author,
        url = url,
        imageUrl = imageUrl,
        publishedAt = formatIsoDate(publishedAt),
        sourceName = sourceName,
        category = category,
        isBookmarked = isBookmarked
    )
}

/**
 * Maps a list of [ArticleEntity] to a list of domain [Article] models.
 */
fun List<ArticleEntity>.toDomainList(): List<Article> {
    return map { it.toDomain() }
}

/**
 * Maps remote [ArticleDto] directly to domain [Article] model.
 */
fun ArticleDto.toDomain(category: String? = null): Article? {
    val articleUrl = url?.trim()
    val articleTitle = title?.trim()

    if (articleUrl.isNullOrBlank() || articleTitle.isNullOrBlank() || articleTitle == AppConstants.REMOVED_ARTICLE_TITLE) {
        return null
    }

    val stableId = articleUrl.hashCode().toString()

    return Article(
        id = stableId,
        title = articleTitle,
        description = description?.trim(),
        content = content?.trim(),
        author = author?.trim(),
        url = articleUrl,
        imageUrl = urlToImage?.trim(),
        publishedAt = formatIsoDate(publishedAt),
        sourceName = source?.name?.trim(),
        category = category,
        isBookmarked = false
    )
}

/**
 * Maps a list of [ArticleDto] directly to a list of domain [Article] models.
 */
fun List<ArticleDto>?.toDomainList(category: String? = null): List<Article> {
    return this?.mapNotNull { it.toDomain(category) } ?: emptyList()
}

/**
 * Maps domain [Article] model to [ArticleEntity].
 */
fun Article.toEntity(cachedAt: Long = 0L): ArticleEntity {
    return ArticleEntity(
        id = id,
        title = title,
        description = description,
        content = content,
        author = author,
        url = url,
        imageUrl = imageUrl,
        publishedAt = publishedAt,
        sourceName = sourceName,
        category = category,
        isBookmarked = isBookmarked,
        cachedAt = cachedAt
    )
}

/**
 * Maps a list of domain [Article] models to [ArticleEntity] list.
 */
fun List<Article>.toEntityList(cachedAt: Long = 0L): List<ArticleEntity> {
    return map { it.toEntity(cachedAt) }
}

