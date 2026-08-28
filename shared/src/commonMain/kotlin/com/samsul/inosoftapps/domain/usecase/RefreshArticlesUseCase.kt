package com.samsul.inosoftapps.domain.usecase

import com.samsul.inosoftapps.domain.model.RefreshResult
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
     * @return Result containing [RefreshResult] with pagination and count info.
     */
    suspend operator fun invoke(category: String? = null, page: Int = 1): Result<RefreshResult> {
        return repository.refreshArticles(category = category, page = page)
    }
}
