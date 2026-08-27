package com.samsul.inosoftapps.domain.usecase

import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow

/**
 * UseCase to observe details of a specific article by ID.
 */
class GetArticleDetailUseCase(
    private val repository: ArticleRepository
) {
    operator fun invoke(id: String): Flow<Article?> {
        return repository.getArticleById(id)
    }
}
