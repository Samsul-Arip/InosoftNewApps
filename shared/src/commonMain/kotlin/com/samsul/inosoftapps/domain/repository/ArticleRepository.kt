package com.samsul.inosoftapps.domain.repository

import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.model.RefreshResult
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
     * Fetches articles from remote network API and caches them locally.
     * @param category Optional category filter.
     * @param page Page number for pagination (default 1).
     * @return [Result.success] with [RefreshResult] containing pagination and count info, or [Result.failure] on error.
     */
    suspend fun refreshArticles(category: String? = null, page: Int = 1): Result<RefreshResult>

    /**
     * Observes a single article by its unique ID.
     */
    fun getArticleById(id: String): Flow<Article?>

    /**
     * Searches articles by query matching title or description.
     */
    fun searchArticles(query: String): Flow<List<Article>>
}
