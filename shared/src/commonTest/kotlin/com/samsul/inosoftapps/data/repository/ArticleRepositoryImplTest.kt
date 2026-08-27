package com.samsul.inosoftapps.data.repository

import app.cash.turbine.test
import com.samsul.inosoftapps.data.local.dao.ArticleDao
import com.samsul.inosoftapps.data.local.entity.ArticleEntity
import com.samsul.inosoftapps.data.mapper.formatIsoDate
import com.samsul.inosoftapps.data.remote.NewsApiService
import com.samsul.inosoftapps.data.remote.dto.ArticleDto
import com.samsul.inosoftapps.data.remote.dto.NewsResponseDto
import com.samsul.inosoftapps.data.remote.dto.SourceDto
import com.samsul.inosoftapps.domain.model.DomainError
import com.samsul.inosoftapps.domain.model.DomainException
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeArticleDao : ArticleDao {
    val db = MutableStateFlow<List<ArticleEntity>>(emptyList())
    var clearAndInsertCalled = false

    override fun getArticles(category: String?): Flow<List<ArticleEntity>> {
        return db.map { list ->
            if (category != null) list.filter { it.category == category } else list
        }
    }

    override fun getArticleById(id: String): Flow<ArticleEntity?> {
        return db.map { list -> list.find { it.id == id } }
    }

    override fun searchArticles(query: String): Flow<List<ArticleEntity>> {
        return db.map { list ->
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                        (it.description?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    override suspend fun insertArticles(articles: List<ArticleEntity>) {
        db.value = articles
    }

    override suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean) {
        db.value = db.value.map {
            if (it.id == id) it.copy(isBookmarked = isBookmarked) else it
        }
    }

    override suspend fun deleteArticlesByCategory(category: String) {
        db.value = db.value.filter { it.category != category }
    }

    override suspend fun deleteAllArticles() {
        db.value = emptyList()
    }

    override suspend fun clearAndInsert(articles: List<ArticleEntity>, category: String?) {
        clearAndInsertCalled = true
        if (category != null) {
            deleteArticlesByCategory(category)
        } else {
            deleteAllArticles()
        }
        db.value = articles
    }
}

class FakeNewsApiService : NewsApiService {
    var shouldThrowNetworkError = false
    var shouldThrowTimeoutError = false
    var shouldReturnServerError = false

    var mockResponse = NewsResponseDto(
        status = "ok",
        totalResults = 1,
        articles = listOf(
            ArticleDto(
                source = SourceDto(id = "the-verge", name = "The Verge"),
                author = "Jane Reporter",
                title = "Compose Multiplatform Breakthrough",
                description = "Modern UI on Android and iOS",
                url = "https://theverge.com/kmp",
                urlToImage = "https://theverge.com/image.jpg",
                publishedAt = "2026-08-27T08:00:00Z",
                content = "Full content."
            )
        )
    )

    override suspend fun getTopHeadlines(
        country: String,
        category: String?,
        page: Int,
        pageSize: Int
    ): NewsResponseDto {
        if (shouldThrowNetworkError) throw IOException("No network connection")
        if (shouldThrowTimeoutError) throw SocketTimeoutException("Connection timed out")
        if (shouldReturnServerError) return NewsResponseDto(status = "error", code = "apiKeyInvalid", message = "Your API key is invalid")
        return mockResponse
    }

    override suspend fun searchNews(
        query: String,
        page: Int,
        pageSize: Int
    ): NewsResponseDto {
        if (shouldThrowNetworkError) throw IOException("No network connection")
        return mockResponse
    }
}

class ArticleRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private val cachedEntity = ArticleEntity(
        id = "cached-1",
        title = "Cached News Article",
        description = "This was cached previously",
        content = "Cached content",
        author = "Cached Author",
        url = "https://news.com/cached-1",
        imageUrl = null,
        publishedAt = "2026-08-27T07:00:00Z",
        sourceName = "Cached Source",
        category = "general",
        isBookmarked = false,
        cachedAt = 1000L
    )

    @Test
    fun getArticles_readsFromDaoAsSingleSourceOfTruth() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDao()
        val fakeApi = FakeNewsApiService()
        val repository = ArticleRepositoryImpl(fakeDao, fakeApi, testDispatcher)

        fakeDao.db.value = listOf(cachedEntity)

        repository.getArticles().test {
            val articles = awaitItem()
            assertEquals(1, articles.size)
            assertEquals("cached-1", articles[0].id)
            assertEquals("Cached News Article", articles[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshArticles_fetchesRemoteAndUpdatesDao() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDao()
        val fakeApi = FakeNewsApiService()
        val repository = ArticleRepositoryImpl(fakeDao, fakeApi, testDispatcher)

        val result = repository.refreshArticles(category = "technology")

        assertTrue(result.isSuccess)
        assertTrue(fakeDao.clearAndInsertCalled)
        assertEquals(1, fakeDao.db.value.size)
        assertEquals("Compose Multiplatform Breakthrough", fakeDao.db.value[0].title)
        assertEquals("technology", fakeDao.db.value[0].category)
    }

    @Test
    fun refreshArticles_onNetworkError_doesNotClearCacheAndReturnsNoInternetError() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDao()
        val fakeApi = FakeNewsApiService().apply { shouldThrowNetworkError = true }
        val repository = ArticleRepositoryImpl(fakeDao, fakeApi, testDispatcher)

        // Pre-populate cache in Room DB
        fakeDao.db.value = listOf(cachedEntity)

        val result = repository.refreshArticles()

        // Result must be failure
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? DomainException
        assertNotNull(exception)
        assertEquals(DomainError.NoInternet, exception.error)

        // Cache in Room DB MUST NOT be deleted
        assertEquals(1, fakeDao.db.value.size)
        assertEquals("cached-1", fakeDao.db.value[0].id)
    }

    @Test
    fun refreshArticles_onTimeoutError_doesNotClearCacheAndReturnsTimeoutError() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDao()
        val fakeApi = FakeNewsApiService().apply { shouldThrowTimeoutError = true }
        val repository = ArticleRepositoryImpl(fakeDao, fakeApi, testDispatcher)

        fakeDao.db.value = listOf(cachedEntity)

        val result = repository.refreshArticles()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? DomainException
        assertNotNull(exception)
        assertEquals(DomainError.Timeout, exception.error)

        // Room DB cache still preserved
        assertEquals(1, fakeDao.db.value.size)
    }

    @Test
    fun refreshArticles_onServerError_returnsServerError() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDao()
        val fakeApi = FakeNewsApiService().apply { shouldReturnServerError = true }
        val repository = ArticleRepositoryImpl(fakeDao, fakeApi, testDispatcher)

        val result = repository.refreshArticles()

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? DomainException
        assertNotNull(exception)
        assertTrue(exception.error is DomainError.ServerError)
    }

    @Test
    fun dateFormat_parsesIsoStringsCorrectly() {
        val isoDate = "2026-08-27T08:00:00Z"
        val formatted = formatIsoDate(isoDate)
        assertTrue(formatted.contains("27"))
        assertTrue(formatted.contains("2026"))
        assertTrue(formatted.contains("Agu") || formatted.contains("Aug") || formatted.contains("08"))

        assertEquals("", formatIsoDate(null))
        assertEquals("", formatIsoDate("   "))
    }
}
