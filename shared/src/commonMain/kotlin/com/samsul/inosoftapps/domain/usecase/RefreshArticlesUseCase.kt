package com.samsul.inosoftapps.domain.usecase

import com.samsul.inosoftapps.domain.repository.ArticleRepository

/**
 * UseCase to trigger remote sync of articles.
 */
class RefreshArticlesUseCase(
    private val repository: ArticleRepository
) {
    suspend operator fun invoke(category: String? = null): Result<Unit> {
        return repository.refreshArticles(category)
    }
}
