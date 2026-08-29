package com.samsul.inosoftapps.data.repository

import app.cash.turbine.test
import com.samsul.inosoftapps.data.local.FakeArticleDao
import com.samsul.inosoftapps.data.local.entity.ArticleEntity
import com.samsul.inosoftapps.data.remote.FakeNewsApiService
import com.samsul.inosoftapps.data.remote.dto.ArticleDto
import com.samsul.inosoftapps.data.remote.dto.SourceDto
import com.samsul.inosoftapps.domain.model.DomainError
import com.samsul.inosoftapps.domain.model.DomainException
import io.ktor.client.network.sockets.SocketTimeoutException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArticleRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeApiService: FakeNewsApiService
    private lateinit var fakeDao: FakeArticleDao
    private lateinit var repository: ArticleRepositoryImpl

    private val sampleEntity = ArticleEntity(
        id = "test-1",
        title = "Room KMP Offline-First Architecture",
        description = "Building robust multiplatform apps with Room.",
        content = "Detailed content on Room KMP.",
        author = "Samsul Arifin",
        url = "https://example.com/kmp",
        imageUrl = "https://example.com/image.png",
        publishedAt = "2026-08-27T10:00:00Z",
        sourceName = "Inosoft Tech",
        category = "technology",
        isBookmarked = false,
        cachedAt = 1756285200000L
    )

    @BeforeTest
    fun setUp() {
        fakeApiService = FakeNewsApiService()
        fakeDao = FakeArticleDao()
        repository = ArticleRepositoryImpl(
            articleDao = fakeDao,
            newsApiService = fakeApiService,
            ioDispatcher = testDispatcher
        )
    }

    /**
     * Tests that [ArticleRepository.getArticles] returns a reactive [Flow] of cached articles
     * emitted directly from the Room DAO Single Source of Truth.
     */
    @Test
    fun getArticles_emitsFromLocalDao_asSingleSourceOfTruth() = runTest(testDispatcher) {
        fakeDao.setInitialEntities(listOf(sampleEntity))

        repository.getArticles("technology").test {
            val initialList = awaitItem()
            assertEquals(1, initialList.size)
            assertEquals("Room KMP Offline-First Architecture", initialList.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Tests that successful remote synchronization fetches articles via API service and persists them into Room DB.
     */
    @Test
    fun refreshArticles_onSuccess_fetchesRemoteAndCachesToRoom() = runTest(testDispatcher) {
        fakeApiService.fakeArticles = listOf(
            ArticleDto(
                source = SourceDto(id = "1", name = "Kompas"),
                author = "Budi",
                title = "Jetpack Compose Multiplatform 1.7",
                description = "Compose Multiplatform is production ready.",
                url = "https://kompas.com/compose",
                urlToImage = "https://picsum.photos/300",
                publishedAt = "2026-08-27T12:00:00Z",
                content = "Full content."
            )
        )

        val result = repository.refreshArticles(category = "technology", page = 1)
        assertTrue(result.isSuccess)

        repository.getArticles("technology").test {
            val articles = awaitItem()
            assertEquals(1, articles.size)
            assertEquals("Jetpack Compose Multiplatform 1.7", articles.first().title)
            assertEquals("Kompas", articles.first().sourceName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Tests that requesting page 2 (load more) appends new articles to existing cached items in Room DB.
     */
    @Test
    fun refreshArticles_pageGreaterThanOne_appendsDataToRoomCache() = runTest(testDispatcher) {
        fakeDao.setInitialEntities(listOf(sampleEntity))

        fakeApiService.fakeArticles = listOf(
            ArticleDto(
                source = SourceDto(id = "2", name = "Detik"),
                author = "Andi",
                title = "Kotlin 2.1 Released",
                description = "New features in Kotlin 2.1.",
                url = "https://detik.com/kotlin-21",
                urlToImage = "https://picsum.photos/400",
                publishedAt = "2026-08-27T14:00:00Z",
                content = "Kotlin 2.1 content."
            )
        )

        val result = repository.refreshArticles(category = "technology", page = 2)
        assertTrue(result.isSuccess)

        repository.getArticles("technology").test {
            val articles = awaitItem()
            assertEquals(2, articles.size)
            assertTrue(articles.any { it.title == "Room KMP Offline-First Architecture" })
            assertTrue(articles.any { it.title == "Kotlin 2.1 Released" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Tests that network failures during refresh preserve the offline cache and return [DomainError.NoInternet].
     */
    @Test
    fun refreshArticles_onOfflineFailure_preservesLocalCacheAndReturnsNoInternetError() = runTest(testDispatcher) {
        // Prepare existing cache
        fakeDao.setInitialEntities(listOf(sampleEntity))

        // Simulate network failure
        fakeApiService.shouldThrowException = IOException("Network connection lost")

        val result = repository.refreshArticles(category = "technology", page = 1)
        assertTrue(result.isFailure)

        val exception = result.exceptionOrNull() as? DomainException
        assertNotNull(exception)
        assertTrue(exception.error is DomainError.NoInternet)

        // Ensure cached article was NOT deleted or lost
        repository.getArticles("technology").test {
            val cachedList = awaitItem()
            assertEquals(1, cachedList.size)
            assertEquals("Room KMP Offline-First Architecture", cachedList.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Tests that socket timeout exceptions during refresh return a typed [DomainError.Timeout].
     */
    @Test
    fun refreshArticles_onTimeoutFailure_returnsTimeoutError() = runTest(testDispatcher) {
        fakeApiService.shouldThrowException = SocketTimeoutException("Socket timed out")

        val result = repository.refreshArticles(category = null, page = 1)
        assertTrue(result.isFailure)

        val exception = result.exceptionOrNull() as? DomainException
        assertNotNull(exception)
        assertTrue(exception.error is DomainError.Timeout)
    }

    /**
     * Tests edge case when both remote network and local cache are unavailable, returning failure without crashing.
     */
    @Test
    fun refreshArticles_whenBothRemoteAndLocalAreUnavailable_returnsFailureAndEmptyArticles() = runTest(testDispatcher) {
        // No local cache in Room
        fakeDao.setInitialEntities(emptyList())

        // Remote network failure
        fakeApiService.shouldThrowException = IOException("No internet connection")

        val result = repository.refreshArticles(page = 1)
        assertTrue(result.isFailure)

        val exception = result.exceptionOrNull() as? DomainException
        assertNotNull(exception)
        assertEquals(DomainError.NoInternet, exception.error)

        // Room DB remains empty
        repository.getArticles().test {
            val emptyList = awaitItem()
            assertTrue(emptyList.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Tests observing a single article by ID, returning the matching article or null if not found.
     */
    @Test
    fun getArticleById_returnsCorrectArticleOrNull() = runTest(testDispatcher) {
        fakeDao.setInitialEntities(listOf(sampleEntity))

        repository.getArticleById("test-1").test {
            val article = awaitItem()
            assertNotNull(article)
            assertEquals("test-1", article.id)
            cancelAndIgnoreRemainingEvents()
        }

        repository.getArticleById("non-existent-id").test {
            val notFound = awaitItem()
            assertNull(notFound)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Tests searching articles locally by title or description query.
     */
    @Test
    fun searchArticles_returnsMatchingResultsFromLocalDb() = runTest(testDispatcher) {
        val article1 = sampleEntity.copy(id = "1", title = "Belajar Kotlin KMP")
        val article2 = sampleEntity.copy(id = "2", title = "Resep Masakan Enak", description = "Cara membuat rendang")
        fakeDao.setInitialEntities(listOf(article1, article2))

        repository.searchArticles("rendang").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals("Resep Masakan Enak", results.first().title)
            cancelAndIgnoreRemainingEvents()
        }

        repository.searchArticles("Kotlin").test {
            val results = awaitItem()
            assertEquals(1, results.size)
            assertEquals("Belajar Kotlin KMP", results.first().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Tests that refreshing one category does not overwrite or corrupt cached articles belonging to other categories.
     */
    @Test
    fun refreshArticles_differentCategories_doesNotOverwriteOtherCategoryCache() = runTest(testDispatcher) {
        val sharedUrl = "https://cnbc.com/sp500-futures"
        val businessArticleDto = ArticleDto(
            source = SourceDto(id = "cnbc", name = "CNBC"),
            author = "Lee Ying Shan",
            title = "S&P 500 futures are little changed",
            description = "Investors count down to Warsh's address",
            url = sharedUrl,
            urlToImage = "https://cnbc.com/image.jpg",
            publishedAt = "2026-08-28T11:17:00Z",
            content = "Content"
        )

        // 1. Refresh "business" tab
        fakeApiService.fakeArticles = listOf(businessArticleDto)
        val businessResult = repository.refreshArticles(category = "business", page = 1)
        assertTrue(businessResult.isSuccess)

        // 2. Refresh "technology" tab with tech article
        val techArticleDto = ArticleDto(
            title = "Apple Unveils New Device",
            url = "https://theverge.com/apple",
            publishedAt = "2026-08-28T12:00:00Z"
        )
        fakeApiService.fakeArticles = listOf(techArticleDto)
        val techResult = repository.refreshArticles(category = "technology", page = 1)
        assertTrue(techResult.isSuccess)

        // 3. Refresh "all" tab (category = null) which also contains the same S&P 500 article
        fakeApiService.fakeArticles = listOf(businessArticleDto, techArticleDto)
        val allResult = repository.refreshArticles(category = null, page = 1)
        assertTrue(allResult.isSuccess)

        // 4. Verify that "business" category STILL has the S&P 500 article
        repository.getArticles("business").test {
            val businessArticles = awaitItem()
            assertEquals(1, businessArticles.size)
            assertEquals("S&P 500 futures are little changed", businessArticles.first().title)
            assertEquals("business", businessArticles.first().category)
            cancelAndIgnoreRemainingEvents()
        }

        // 5. Verify that "technology" category STILL has the Apple article
        repository.getArticles("technology").test {
            val techArticles = awaitItem()
            assertEquals(1, techArticles.size)
            assertEquals("Apple Unveils New Device", techArticles.first().title)
            assertEquals("technology", techArticles.first().category)
            cancelAndIgnoreRemainingEvents()
        }

        // 6. Verify that "all" category has both articles
        repository.getArticles(null).test {
            val allArticles = awaitItem()
            assertEquals(2, allArticles.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
