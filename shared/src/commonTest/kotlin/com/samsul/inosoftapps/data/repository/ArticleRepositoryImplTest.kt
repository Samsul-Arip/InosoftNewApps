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

class FakeArticleDaoImpl : ArticleDao {
    val db = MutableStateFlow<List<ArticleEntity>>(emptyList())
    var clearAndInsertCalled = false
    var insertArticlesCalled = false

    override fun getArticles(category: String?): Flow<List<ArticleEntity>> {
        return db.map { list ->
            if (category == null) list.filter { it.category == null } else list.filter { it.category == category }
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
        insertArticlesCalled = true
        db.value = db.value + articles
    }

    override suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean) {
        db.value = db.value.map {
            if (it.id == id) it.copy(isBookmarked = isBookmarked) else it
        }
    }

    override suspend fun deleteArticlesByCategory(category: String) {
        db.value = db.value.filter { it.category != category }
    }

    override suspend fun deleteArticlesWithNoCategory() {
        db.value = db.value.filter { it.category != null }
    }

    override suspend fun deleteAllArticles() {
        db.value = emptyList()
    }

    override suspend fun clearAndInsert(articles: List<ArticleEntity>, category: String?) {
        clearAndInsertCalled = true
        if (category != null) {
            deleteArticlesByCategory(category)
        } else {
            deleteArticlesWithNoCategory()
        }
        db.value = articles
    }
}

class FakeNewsApiServiceImpl : NewsApiService {
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
        category = null,
        isBookmarked = false,
        cachedAt = 1000L
    )

    /**
     * Tests that [ArticleRepositoryImpl.getArticles] reads from Room DAO as the Single Source of Truth
     * and maps database entities into domain models.
     */
    @Test
    fun getArticles_readsFromDaoAsSingleSourceOfTruth() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDaoImpl()
        val fakeApi = FakeNewsApiServiceImpl()
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

    /**
     * Tests that initial refresh (page 1) atomically clears old category cache and inserts newly fetched articles.
     */
    @Test
    fun refreshArticles_page1_clearsAndInsertsDao() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDaoImpl()
        val fakeApi = FakeNewsApiServiceImpl()
        val repository = ArticleRepositoryImpl(fakeDao, fakeApi, testDispatcher)

        val result = repository.refreshArticles(category = "technology", page = 1)

        assertTrue(result.isSuccess)
        assertTrue(fakeDao.clearAndInsertCalled)
        assertEquals(1, fakeDao.db.value.size)
        assertEquals("Compose Multiplatform Breakthrough", fakeDao.db.value[0].title)
        assertEquals("technology", fakeDao.db.value[0].category)
    }

    /**
     * Tests that pagination requests (page > 1) append new articles to the Room cache rather than clearing existing entries.
     */
    @Test
    fun refreshArticles_pageGreaterThan1_appendsToDao() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDaoImpl()
        val fakeApi = FakeNewsApiServiceImpl()
        val repository = ArticleRepositoryImpl(fakeDao, fakeApi, testDispatcher)

        fakeDao.db.value = listOf(cachedEntity.copy(category = "general"))

        val result = repository.refreshArticles(category = "general", page = 2)

        assertTrue(result.isSuccess)
        assertTrue(fakeDao.insertArticlesCalled)
        assertEquals(2, fakeDao.db.value.size)
    }

    /**
     * Tests pagination boundary calculations to ensure `hasMore` is accurately calculated based on total results and page size.
     */
    @Test
    fun refreshArticles_pagination_calculatesHasMoreAccurately() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDaoImpl()
        val fakeApi = FakeNewsApiServiceImpl()
        val repository = ArticleRepositoryImpl(fakeDao, fakeApi, testDispatcher)

        // Case 1: totalResults = 20, page 1 (pageSize 7) -> 1 * 7 < 20 -> hasMore = true
        fakeApi.mockResponse = fakeApi.mockResponse.copy(
            totalResults = 20,
            articles = listOf(
                ArticleDto(
                    title = "Article 1",
                    url = "https://example.com/1"
                ),
                ArticleDto(
                    title = "[Removed]",
                    url = "https://example.com/2"
                )
            )
        )

        val page1Result = repository.refreshArticles(page = 1)
        assertTrue(page1Result.isSuccess)
        assertEquals(true, page1Result.getOrNull()?.hasMore)
        assertEquals(1, page1Result.getOrNull()?.articleCount)

        // Case 2: totalResults = 10, page 2 (pageSize 7) -> 2 * 7 = 14 >= 10 -> hasMore = false
        fakeApi.mockResponse = fakeApi.mockResponse.copy(
            totalResults = 10,
            articles = listOf(
                ArticleDto(
                    title = "Article 3",
                    url = "https://example.com/3"
                )
            )
        )

        val page2Result = repository.refreshArticles(page = 2)
        assertTrue(page2Result.isSuccess)
        assertEquals(false, page2Result.getOrNull()?.hasMore)
        assertEquals(1, page2Result.getOrNull()?.articleCount)
    }

    /**
     * Tests that network disconnection during refresh does NOT purge local cache and maps to [DomainError.NoInternet].
     */
    @Test
    fun refreshArticles_onNetworkError_doesNotClearCacheAndReturnsNoInternetError() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDaoImpl()
        val fakeApi = FakeNewsApiServiceImpl().apply { shouldThrowNetworkError = true }
        val repository = ArticleRepositoryImpl(fakeDao, fakeApi, testDispatcher)

        // Pre-populate cache in Room DB
        fakeDao.db.value = listOf(cachedEntity)

        val result = repository.refreshArticles(page = 1)

        // Result must be failure
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? DomainException
        assertNotNull(exception)
        assertEquals(DomainError.NoInternet, exception.error)

        // Cache in Room DB MUST NOT be deleted
        assertEquals(1, fakeDao.db.value.size)
        assertEquals("cached-1", fakeDao.db.value[0].id)
    }

    /**
     * Tests that request timeouts preserve existing local cache and return [DomainError.Timeout].
     */
    @Test
    fun refreshArticles_onTimeoutError_doesNotClearCacheAndReturnsTimeoutError() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDaoImpl()
        val fakeApi = FakeNewsApiServiceImpl().apply { shouldThrowTimeoutError = true }
        val repository = ArticleRepositoryImpl(fakeDao, fakeApi, testDispatcher)

        fakeDao.db.value = listOf(cachedEntity)

        val result = repository.refreshArticles(page = 1)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? DomainException
        assertNotNull(exception)
        assertEquals(DomainError.Timeout, exception.error)

        // Room DB cache still preserved
        assertEquals(1, fakeDao.db.value.size)
    }

    /**
     * Tests that backend HTTP errors or invalid API responses are mapped into [DomainError.ServerError].
     */
    @Test
    fun refreshArticles_onServerError_returnsServerError() = runTest(testDispatcher) {
        val fakeDao = FakeArticleDaoImpl()
        val fakeApi = FakeNewsApiServiceImpl().apply { shouldReturnServerError = true }
        val repository = ArticleRepositoryImpl(fakeDao, fakeApi, testDispatcher)

        val result = repository.refreshArticles(page = 1)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull() as? DomainException
        assertNotNull(exception)
        assertTrue(exception.error is DomainError.ServerError)
    }

    /**
     * Tests that ISO-8601 date strings are correctly parsed into localized Indonesian date representations.
     */
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
