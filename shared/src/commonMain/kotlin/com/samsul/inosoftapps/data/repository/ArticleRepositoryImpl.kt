package com.samsul.inosoftapps.data.repository

import com.samsul.inosoftapps.data.local.dao.ArticleDao
import com.samsul.inosoftapps.data.mapper.toDomain
import com.samsul.inosoftapps.data.mapper.toDomainList
import com.samsul.inosoftapps.data.mapper.toEntityList
import com.samsul.inosoftapps.data.remote.NewsApiService
import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.model.DomainError
import com.samsul.inosoftapps.domain.model.DomainException
import com.samsul.inosoftapps.domain.model.RefreshResult
import com.samsul.inosoftapps.domain.repository.ArticleRepository
import com.samsul.inosoftapps.util.AppConstants
import com.samsul.inosoftapps.util.AppStrings
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.io.IOException

/**
 * Concrete implementation of [ArticleRepository] providing Offline-First data flow with Pagination.
 * UI observes the Room database as the Single Source of Truth (SSOT).
 * Remote refresh page 1 clears and syncs cache; subsequent pages append to Room DB.
 * When network fails, existing Room DB cache is preserved and a typed [DomainError] is returned.
 */
class ArticleRepositoryImpl(
    private val articleDao: ArticleDao,
    private val newsApiService: NewsApiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ArticleRepository {

    override fun getArticles(category: String?): Flow<List<Article>> {
        return articleDao.getArticles(category)
            .map { entities -> entities.toDomainList() }
            .flowOn(ioDispatcher)
    }

    override suspend fun refreshArticles(category: String?, page: Int): Result<RefreshResult> = withContext(ioDispatcher) {
        try {
            val response = newsApiService.getTopHeadlines(
                category = category,
                page = page,
                pageSize = AppConstants.DEFAULT_PAGE_SIZE
            )

            if (response.status == AppConstants.API_STATUS_OK) {
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val rawArticles = response.articles.orEmpty()
                val entities = rawArticles.toEntityList(category = category, cachedAt = currentTime)
                val totalResults = response.totalResults ?: 0
                val hasMore = if (totalResults > 0) {
                    (page * AppConstants.DEFAULT_PAGE_SIZE) < totalResults && rawArticles.isNotEmpty()
                } else {
                    rawArticles.size >= AppConstants.DEFAULT_PAGE_SIZE
                }

                if (page == 1) {
                    if (entities.isNotEmpty()) {
                        articleDao.clearAndInsert(entities, category)
                    } else {
                        if (category != null) {
                            articleDao.deleteArticlesByCategory(category)
                        } else {
                            articleDao.deleteArticlesWithNoCategory()
                        }
                    }
                } else {
                    if (entities.isNotEmpty()) {
                        articleDao.insertArticles(entities)
                    }
                }
                Result.success(RefreshResult(hasMore = hasMore, articleCount = entities.size))
            } else {
                val serverError = DomainError.ServerError(
                    code = null,
                    message = AppStrings.formatApiErrorMessage(response.message, response.code)
                )
                Result.failure(DomainException(serverError))
            }
        } catch (e: Exception) {
            // Crucial: Cache in Room is never purged on network failure
            val domainError = mapExceptionToDomainError(e)
            Result.failure(DomainException(domainError))
        }
    }

    override fun getArticleById(id: String): Flow<Article?> {
        return articleDao.getArticleById(id)
            .map { entity -> entity?.toDomain() }
            .flowOn(ioDispatcher)
    }

    override fun searchArticles(query: String): Flow<List<Article>> {
        return articleDao.searchArticles(query)
            .map { entities -> entities.toDomainList() }
            .flowOn(ioDispatcher)
    }

    private fun mapExceptionToDomainError(e: Exception): DomainError {
        return when (e) {
            is HttpRequestTimeoutException,
            is SocketTimeoutException -> DomainError.Timeout

            is ResponseException -> DomainError.ServerError(
                code = e.response.status.value,
                message = e.message
            )

            is IOException,
            is UnresolvedAddressException -> DomainError.NoInternet

            is DomainException -> e.error

            else -> {
                val message = e.message.orEmpty()
                val isNetworkError = AppConstants.NETWORK_ERROR_KEYWORDS.any { keyword ->
                    message.contains(keyword, ignoreCase = true)
                }
                if (isNetworkError) {
                    DomainError.NoInternet
                } else {
                    DomainError.Unknown(throwable = e, message = e.message)
                }
            }
        }
    }
}
