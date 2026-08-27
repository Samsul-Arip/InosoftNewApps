package com.samsul.inosoftapps.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.samsul.inosoftapps.domain.model.Article

/**
 * Room database entity representing an article stored in the local SQLite database.
 */
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val content: String?,
    val author: String?,
    val url: String,
    val imageUrl: String?,
    val publishedAt: String,
    val sourceName: String?,
    val category: String?,
    val isBookmarked: Boolean = false,
    val cachedAt: Long = 0L
)

/**
 * Converts [ArticleEntity] to Domain [Article] model.
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
        publishedAt = publishedAt,
        sourceName = sourceName,
        category = category,
        isBookmarked = isBookmarked
    )
}

/**
 * Converts Domain [Article] model to [ArticleEntity].
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
 * Converts a list of [ArticleEntity] to a list of Domain [Article] models.
 */
fun List<ArticleEntity>.toDomainList(): List<Article> {
    return map { it.toDomain() }
}

/**
 * Converts a list of Domain [Article] models to a list of [ArticleEntity].
 */
fun List<Article>.toEntityList(cachedAt: Long = 0L): List<ArticleEntity> {
    return map { it.toEntity(cachedAt) }
}
