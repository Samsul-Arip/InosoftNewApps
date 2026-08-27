package com.samsul.inosoftapps.domain.model

/**
 * Pure domain representation of a news article.
 * Free of any database annotations or serialization concerns.
 */
data class Article(
    val id: String,
    val title: String,
    val description: String?,
    val content: String?,
    val author: String?,
    val url: String,
    val imageUrl: String?,
    val publishedAt: String,
    val sourceName: String?,
    val category: String? = null,
    val isBookmarked: Boolean = false
)
