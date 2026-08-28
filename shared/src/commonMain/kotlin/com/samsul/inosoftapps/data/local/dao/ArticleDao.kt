package com.samsul.inosoftapps.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.samsul.inosoftapps.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local article caching operations.
 */
@Dao
interface ArticleDao {

    /**
     * Observes all articles ordered by publication date descending.
     * Optionally filters by [category].
     */
    @Query(
        """
        SELECT * FROM articles 
        WHERE (:category IS NULL OR category = :category) 
        ORDER BY publishedAt DESC, cachedAt DESC, id ASC
        """
    )
    fun getArticles(category: String? = null): Flow<List<ArticleEntity>>

    /**
     * Observes a specific article by [id].
     */
    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    fun getArticleById(id: String): Flow<ArticleEntity?>

    /**
     * Searches articles by query against title, description, or content.
     */
    @Query(
        """
        SELECT * FROM articles 
        WHERE title LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%' 
        ORDER BY publishedAt DESC
        """
    )
    fun searchArticles(query: String): Flow<List<ArticleEntity>>

    /**
     * Inserts or updates articles in the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    /**
     * Updates bookmark status for a specific article.
     */
    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean)

    /**
     * Deletes all articles matching a specific category.
     */
    @Query("DELETE FROM articles WHERE category = :category")
    suspend fun deleteArticlesByCategory(category: String)

    /**
     * Deletes all articles with no category (the 'Semua' feed).
     */
    @Query("DELETE FROM articles WHERE category IS NULL")
    suspend fun deleteArticlesWithNoCategory()

    /**
     * Deletes all cached articles.
     */
    @Query("DELETE FROM articles")
    suspend fun deleteAllArticles()

    /**
     * Atomically clears and inserts articles.
     * Preserves other categories when clearing the default 'Semua' category.
     */
    @Transaction
    suspend fun clearAndInsert(articles: List<ArticleEntity>, category: String? = null) {
        if (category != null) {
            deleteArticlesByCategory(category)
        } else {
            deleteArticlesWithNoCategory()
        }
        insertArticles(articles)
    }
}
