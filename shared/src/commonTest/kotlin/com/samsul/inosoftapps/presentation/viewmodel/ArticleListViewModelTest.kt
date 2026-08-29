package com.samsul.inosoftapps.presentation.viewmodel

import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.model.DomainError
import com.samsul.inosoftapps.domain.model.DomainException
import com.samsul.inosoftapps.domain.model.RefreshResult
import com.samsul.inosoftapps.domain.repository.ArticleRepository
import com.samsul.inosoftapps.domain.usecase.GetArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.RefreshArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.SearchArticlesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeRepo : ArticleRepository {
    val articlesFlow = MutableStateFlow<List<Article>>(emptyList())
    var shouldFailRefresh = false
    var hasMorePages = true
    var lastRequestedPage = 1

    override fun getArticles(category: String?): Flow<List<Article>> {
        return articlesFlow.map { list ->
            if (category != null) list.filter { it.category == category } else list
        }
    }

    override suspend fun refreshArticles(category: String?, page: Int): Result<RefreshResult> {
        lastRequestedPage = page
        return if (shouldFailRefresh) {
            Result.failure(DomainException(DomainError.NoInternet))
        } else {
            val count = if (category != null) {
                articlesFlow.value.count { it.category == category }
            } else {
                articlesFlow.value.size
            }
            Result.success(RefreshResult(hasMore = hasMorePages, articleCount = count))
        }
    }

    override fun getArticleById(id: String): Flow<Article?> {
        return articlesFlow.map { list -> list.find { it.id == id } }
    }

    override fun searchArticles(query: String): Flow<List<Article>> {
        return articlesFlow.map { list ->
            list.filter { it.title.contains(query, ignoreCase = true) }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeRepo
    private lateinit var getArticlesUseCase: GetArticlesUseCase
    private lateinit var refreshArticlesUseCase: RefreshArticlesUseCase
    private lateinit var searchArticlesUseCase: SearchArticlesUseCase

    private val sampleArticle = Article(
        id = "1",
        title = "KMP Architecture",
        description = "Deep dive into multiplatform",
        content = "Content",
        author = "Author",
        url = "https://example.com/kmp",
        imageUrl = null,
        publishedAt = "27 Agu 2026, 10:00",
        sourceName = "Tech",
        category = "technology",
        isBookmarked = false
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeRepo()
        getArticlesUseCase = GetArticlesUseCase(fakeRepo)
        refreshArticlesUseCase = RefreshArticlesUseCase(fakeRepo)
        searchArticlesUseCase = SearchArticlesUseCase(fakeRepo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Tests that [ArticleListViewModel] initializes by observing cached articles from the repository
     * and populates [ArticleListUiState.articles].
     */
    @Test
    fun init_observesArticlesFromRepository() = runTest(testDispatcher) {
        fakeRepo.articlesFlow.value = listOf(sampleArticle)

        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )

        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.articles.size)
        assertEquals("1", viewModel.uiState.value.articles[0].id)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    /**
     * Tests that selecting a category filter updates [ArticleListUiState.selectedCategory]
     * and filters emitted articles accordingly.
     */
    @Test
    fun selectCategory_filtersArticlesByCategory() = runTest(testDispatcher) {
        val businessArticle = sampleArticle.copy(id = "2", category = "business", title = "Market trends")
        fakeRepo.articlesFlow.value = listOf(sampleArticle, businessArticle)

        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.articles.size)

        viewModel.selectCategory("business")
        advanceUntilIdle()

        assertEquals("business", viewModel.uiState.value.selectedCategory)
        assertEquals(1, viewModel.uiState.value.articles.size)
        assertEquals("2", viewModel.uiState.value.articles[0].id)
    }

    /**
     * Tests searching articles by query updates [ArticleListUiState.searchQuery]
     * and filters matching results from local database.
     */
    @Test
    fun searchArticles_filtersMatchingArticles() = runTest(testDispatcher) {
        val otherArticle = sampleArticle.copy(id = "3", title = "Cooking Pasta")
        fakeRepo.articlesFlow.value = listOf(sampleArticle, otherArticle)

        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )
        advanceUntilIdle()

        viewModel.searchArticles("Pasta")
        advanceUntilIdle()

        assertEquals("Pasta", viewModel.uiState.value.searchQuery)
        assertEquals(1, viewModel.uiState.value.articles.size)
        assertEquals("3", viewModel.uiState.value.articles[0].id)
    }

    /**
     * Tests that failed refresh requests set a user-friendly error message in [ArticleListUiState]
     * and [ArticleListViewModel.clearError] clears it.
     */
    @Test
    fun refreshArticles_onFailure_setsUserFriendlyErrorMessage() = runTest(testDispatcher) {
        fakeRepo.shouldFailRefresh = true

        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("koneksi internet", ignoreCase = true))

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    /**
     * Tests that network errors occurring when cached articles already exist update [ArticleListUiState.isOffline] to true
     * while retaining cached articles in the UI list.
     */
    @Test
    fun refreshArticles_onNetworkErrorWithCachedArticles_setsIsOfflineTrue() = runTest(testDispatcher) {
        fakeRepo.articlesFlow.value = listOf(sampleArticle)
        fakeRepo.shouldFailRefresh = true

        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOffline)
        assertEquals(1, viewModel.uiState.value.articles.size)
    }

    /**
     * Tests infinite scroll pagination: [ArticleListViewModel.loadMoreArticles] increments [ArticleListUiState.currentPage]
     * and requests the next page from repository.
     */
    @Test
    fun loadMoreArticles_incrementsPageAndUpdatesPaginationState() = runTest(testDispatcher) {
        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentPage)
        assertTrue(viewModel.uiState.value.canLoadMore)

        viewModel.loadMoreArticles()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.currentPage)
        assertEquals(2, fakeRepo.lastRequestedPage)
        assertFalse(viewModel.uiState.value.isLoadingMore)
    }

    /**
     * Tests that when [ArticleListUiState.canLoadMore] is false (last page reached),
     * [ArticleListViewModel.loadMoreArticles] does not trigger redundant network requests.
     */
    @Test
    fun loadMoreArticles_whenCanLoadMoreIsFalse_doesNotTriggerRefresh() = runTest(testDispatcher) {
        fakeRepo.hasMorePages = false

        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canLoadMore)

        // Attempt to load more
        viewModel.loadMoreArticles()
        advanceUntilIdle()

        // Page should still be 1 because canLoadMore was false
        assertEquals(1, viewModel.uiState.value.currentPage)
        assertEquals(1, fakeRepo.lastRequestedPage)
    }

    /**
     * Tests that pagination is ignored while search mode is active to prevent mixing global search with paginated feeds.
     */
    @Test
    fun loadMoreArticles_whenSearchActive_doesNotTriggerPagination() = runTest(testDispatcher) {
        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )
        advanceUntilIdle()

        viewModel.searchArticles("Kotlin")
        advanceUntilIdle()

        viewModel.loadMoreArticles()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentPage)
        assertEquals(1, fakeRepo.lastRequestedPage)
    }

    /**
     * Tests that selecting a new category resets pagination back to page 1 and restores [ArticleListUiState.canLoadMore].
     */
    @Test
    fun selectCategory_resetsPaginationToPageOne() = runTest(testDispatcher) {
        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )
        advanceUntilIdle()

        viewModel.loadMoreArticles()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.currentPage)

        viewModel.selectCategory("technology")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentPage)
        assertTrue(viewModel.uiState.value.canLoadMore)
    }

    /**
     * Tests UI flickering prevention: on initial startup with empty cache, [ArticleListUiState.isLoading] remains true
     * until network refresh completes to avoid flashing an empty state.
     */
    @Test
    fun init_whenCacheEmpty_maintainsLoadingTrueUntilRefreshCompletes() = runTest(testDispatcher) {
        // Cache is empty
        fakeRepo.articlesFlow.value = emptyList()

        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )

        // Before coroutines finish, isLoading is true
        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isRefreshing)

        advanceUntilIdle()

        // After refresh finishes with empty repo
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(viewModel.uiState.value.articles.isEmpty())
    }

    /**
     * Tests that switching to an un-cached category maintains [ArticleListUiState.isLoading] as true
     * until remote data arrives, preventing flickering.
     */
    @Test
    fun selectCategory_whenCacheEmpty_maintainsLoadingTrue() = runTest(testDispatcher) {
        fakeRepo.articlesFlow.value = listOf(sampleArticle) // sampleArticle has category "technology"

        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.articles.size)

        // Switch to "business" where cache has 0 articles
        viewModel.selectCategory("business")

        // Immediately after switching, isLoading must be true and isRefreshing false (not pull-to-refresh)
        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(viewModel.uiState.value.articles.isEmpty())

        advanceUntilIdle()

        assertEquals("business", viewModel.uiState.value.selectedCategory)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    /**
     * Tests that switching to a category that already has cached articles immediately renders the cached data
     * and sets [ArticleListUiState.isLoading] to false.
     */
    @Test
    fun selectCategory_whenCachedArticlesExist_loadsCachedArticles() = runTest(testDispatcher) {
        val businessArticle = sampleArticle.copy(id = "2", category = "business", title = "Market trends")
        fakeRepo.articlesFlow.value = listOf(sampleArticle, businessArticle)

        val viewModel = ArticleListViewModel(
            getArticlesUseCase,
            refreshArticlesUseCase,
            searchArticlesUseCase
        )
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.articles.size)

        viewModel.selectCategory("business")
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.articles.size)
        assertEquals("business", viewModel.uiState.value.articles[0].category)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
