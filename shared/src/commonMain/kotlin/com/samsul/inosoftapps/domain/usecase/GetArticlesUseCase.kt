package com.samsul.inosoftapps.domain.usecase

import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase to observe the reactive stream of cached articles.
 */
class GetArticlesUseCase(
    private val repository: ArticleRepository
) {
    operator fun invoke(category: String? = null): Flow<List<Article>> {
        return repository.getArticles(category)
    }
}
