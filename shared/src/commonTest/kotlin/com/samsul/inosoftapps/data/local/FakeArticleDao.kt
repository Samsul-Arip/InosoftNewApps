package com.samsul.inosoftapps.data.local

import com.samsul.inosoftapps.data.local.dao.ArticleDao
import com.samsul.inosoftapps.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory Fake implementation of Room ArticleDao for unit testing.
 */
class FakeArticleDao : ArticleDao {

    private val entitiesFlow = MutableStateFlow<List<ArticleEntity>>(emptyList())

    fun setInitialEntities(list: List<ArticleEntity>) {
        entitiesFlow.value = list
    }

    override fun getArticles(category: String?): Flow<List<ArticleEntity>> {
        return entitiesFlow.map { list ->
            if (category == null) list.filter { it.category == null }
            else list.filter { it.category == category }
        }
    }

    override fun getArticleById(id: String): Flow<ArticleEntity?> {
        return entitiesFlow.map { list -> list.find { it.id == id } }
    }

    override fun searchArticles(query: String): Flow<List<ArticleEntity>> {
        return entitiesFlow.map { list ->
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                        (it.description?.contains(query, ignoreCase = true) == true) ||
                        (it.content?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    override suspend fun insertArticles(articles: List<ArticleEntity>) {
        val currentMap = entitiesFlow.value.associateBy { it.id }.toMutableMap()
        articles.forEach { currentMap[it.id] = it }
        entitiesFlow.value = currentMap.values.toList()
    }

    override suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean) {
        entitiesFlow.value = entitiesFlow.value.map {
            if (it.id == id) it.copy(isBookmarked = isBookmarked) else it
        }
    }

    override suspend fun deleteArticlesByCategory(category: String) {
        entitiesFlow.value = entitiesFlow.value.filterNot { it.category == category }
    }

    override suspend fun deleteArticlesWithNoCategory() {
        entitiesFlow.value = entitiesFlow.value.filterNot { it.category == null }
    }

    override suspend fun deleteAllArticles() {
        entitiesFlow.value = emptyList()
    }

    override suspend fun clearAndInsert(articles: List<ArticleEntity>, category: String?) {
        val remaining = if (category.isNullOrBlank()) {
            entitiesFlow.value.filterNot { it.category == null }
        } else {
            entitiesFlow.value.filterNot { it.category == category }
        }
        val combined = (remaining + articles).distinctBy { it.id }
        entitiesFlow.value = combined
    }
}
