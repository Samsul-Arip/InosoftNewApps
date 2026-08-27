package com.samsul.inosoftapps.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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
