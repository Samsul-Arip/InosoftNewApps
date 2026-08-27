package com.samsul.inosoftapps.presentation.viewmodel

import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.model.DomainError
import com.samsul.inosoftapps.domain.model.DomainException
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

    override suspend fun refreshArticles(category: String?, page: Int): Result<Boolean> {
        lastRequestedPage = page
        return if (shouldFailRefresh) {
            Result.failure(DomainException(DomainError.NoInternet))
        } else {
            Result.success(hasMorePages)
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
}
