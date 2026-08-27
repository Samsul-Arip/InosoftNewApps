package com.samsul.inosoftapps.domain.usecase

import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase to search articles matching a query string.
 */
class SearchArticlesUseCase(
    private val repository: ArticleRepository
) {
    operator fun invoke(query: String): Flow<List<Article>> {
        return repository.searchArticles(query)
    }
}
