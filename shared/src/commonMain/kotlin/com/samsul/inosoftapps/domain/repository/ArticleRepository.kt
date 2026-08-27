package com.samsul.inosoftapps.domain.repository

import com.samsul.inosoftapps.domain.model.Article
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining data operations for news articles.
 * Follows the Single Source of Truth (SSOT) pattern where local cache emits data
 * and refresh methods fetch from remote to synchronize cache.
 */
interface ArticleRepository {
    /**
     * Emits the stream of articles from local database (SSOT).
     * @param category Optional category filter (e.g., "business", "technology").
     */
    fun getArticles(category: String? = null): Flow<List<Article>>

    /**
     * Fetches the latest articles from remote network API and caches them locally.
     * @param category Optional category filter.
     * @return [Result.success] when sync succeeds, or [Result.failure] on error.
     */
    suspend fun refreshArticles(category: String? = null): Result<Unit>

    /**
     * Observes a single article by its unique ID.
     */
    fun getArticleById(id: String): Flow<Article?>

    /**
     * Searches articles by query matching title or description.
     */
    fun searchArticles(query: String): Flow<List<Article>>
}
