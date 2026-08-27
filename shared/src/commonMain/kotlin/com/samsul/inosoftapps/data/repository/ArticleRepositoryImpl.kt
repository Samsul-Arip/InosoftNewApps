package com.samsul.inosoftapps.data.repository

import com.samsul.inosoftapps.data.local.dao.ArticleDao
import com.samsul.inosoftapps.data.mapper.toDomain
import com.samsul.inosoftapps.data.mapper.toDomainList
import com.samsul.inosoftapps.data.mapper.toEntityList
import com.samsul.inosoftapps.data.remote.NewsApiService
import com.samsul.inosoftapps.data.remote.NewsConfig
import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.model.DomainError
import com.samsul.inosoftapps.domain.model.DomainException
import com.samsul.inosoftapps.domain.repository.ArticleRepository
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
 * Concrete implementation of [ArticleRepository] providing Offline-First data flow.
 * UI observes the Room database as the Single Source of Truth (SSOT).
 * Remote refresh writes into Room DB so UI updates automatically.
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

    override suspend fun refreshArticles(category: String?): Result<Unit> = withContext(ioDispatcher) {
        try {
            val response = newsApiService.getTopHeadlines(
                country = NewsConfig.DEFAULT_COUNTRY,
                category = category
            )

            if (response.status == "ok") {
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val entities = response.articles.toEntityList(category = category, cachedAt = currentTime)
                
                if (entities.isNotEmpty()) {
                    articleDao.clearAndInsert(entities, category)
                }
                Result.success(Unit)
            } else {
                val serverError = DomainError.ServerError(
                    code = null,
                    message = response.message ?: "Failed to fetch headlines (Code: ${response.code})"
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
                if (message.contains("Unable to resolve host", ignoreCase = true) ||
                    message.contains("No address associated", ignoreCase = true) ||
                    message.contains("ConnectException", ignoreCase = true) ||
                    message.contains("Network is unreachable", ignoreCase = true) ||
                    message.contains("connection abort", ignoreCase = true)
                ) {
                    DomainError.NoInternet
                } else {
                    DomainError.Unknown(throwable = e, message = e.message)
                }
            }
        }
    }
}
