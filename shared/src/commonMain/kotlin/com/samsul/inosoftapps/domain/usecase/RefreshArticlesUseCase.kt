package com.samsul.inosoftapps.domain.usecase

import com.samsul.inosoftapps.domain.repository.ArticleRepository

/**
 * UseCase to trigger remote sync of articles with pagination support.
 */
class RefreshArticlesUseCase(
    private val repository: ArticleRepository
) {
    /**
     * @param category Optional category filter.
     * @param page Page index for pagination.
     * @return Result containing boolean flag indicating if more pages are available.
     */
    suspend operator fun invoke(category: String? = null, page: Int = 1): Result<Boolean> {
        return repository.refreshArticles(category = category, page = page)
    }
}
