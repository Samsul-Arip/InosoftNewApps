package com.samsul.inosoftapps.domain

import app.cash.turbine.test
import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.model.DomainError
import com.samsul.inosoftapps.domain.model.ResultState
import com.samsul.inosoftapps.domain.model.dataOrNull
import com.samsul.inosoftapps.domain.model.isLoading
import com.samsul.inosoftapps.domain.model.isSuccess
import com.samsul.inosoftapps.domain.model.map
import com.samsul.inosoftapps.domain.model.onError
import com.samsul.inosoftapps.domain.model.onSuccess
import com.samsul.inosoftapps.domain.repository.ArticleRepository
import com.samsul.inosoftapps.domain.usecase.GetArticleDetailUseCase
import com.samsul.inosoftapps.domain.usecase.GetArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.RefreshArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.SearchArticlesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeArticleRepository : ArticleRepository {
    val articlesFlow = MutableStateFlow<List<Article>>(emptyList())
    var shouldFailRefresh = false

    override fun getArticles(category: String?): Flow<List<Article>> {
        return articlesFlow.map { list ->
            if (category != null) list.filter { it.category == category } else list
        }
    }

    override suspend fun refreshArticles(category: String?): Result<Unit> {
        return if (shouldFailRefresh) {
            Result.failure(Exception("Network error"))
        } else {
            Result.success(Unit)
        }
    }

    override fun getArticleById(id: String): Flow<Article?> {
        return articlesFlow.map { list -> list.find { it.id == id } }
    }

    override fun searchArticles(query: String): Flow<List<Article>> {
        return articlesFlow.map { list ->
            list.filter {
                it.title.contains(query, ignoreCase = true) ||
                        (it.description?.contains(query, ignoreCase = true) == true)
            }
        }
    }
}

class ArticleUseCasesTest {

    private val sampleArticle = Article(
        id = "1",
        title = "Kotlin Multiplatform 2026",
        description = "KMP is amazing",
        content = "Detailed content about KMP",
        author = "JetBrains",
        url = "https://kotlinlang.org",
        imageUrl = "https://kotlinlang.org/image.png",
        publishedAt = "2026-08-27T10:00:00Z",
        sourceName = "Kotlin Blog",
        category = "technology",
        isBookmarked = false
    )

    private val sampleArticle2 = Article(
        id = "2",
        title = "Global Tech News",
        description = "Tech developments around the world",
        content = "Content about tech",
        author = "Tech Author",
        url = "https://tech.org",
        imageUrl = null,
        publishedAt = "2026-08-27T11:00:00Z",
        sourceName = "Tech World",
        category = "business",
        isBookmarked = true
    )

    @Test
    fun getArticlesUseCase_emitsArticlesFromRepository() = runTest {
        val fakeRepo = FakeArticleRepository()
        val useCase = GetArticlesUseCase(fakeRepo)

        fakeRepo.articlesFlow.value = listOf(sampleArticle, sampleArticle2)

        useCase().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("1", result[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getArticlesUseCase_filtersByCategoryCorrectly() = runTest {
        val fakeRepo = FakeArticleRepository()
        val useCase = GetArticlesUseCase(fakeRepo)

        fakeRepo.articlesFlow.value = listOf(sampleArticle, sampleArticle2)

        useCase(category = "technology").test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("technology", result[0].category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshArticlesUseCase_returnsSuccess() = runTest {
        val fakeRepo = FakeArticleRepository()
        val useCase = RefreshArticlesUseCase(fakeRepo)

        val result = useCase()
        assertTrue(result.isSuccess)
    }

    @Test
    fun refreshArticlesUseCase_returnsFailureOnNetworkError() = runTest {
        val fakeRepo = FakeArticleRepository().apply { shouldFailRefresh = true }
        val useCase = RefreshArticlesUseCase(fakeRepo)

        val result = useCase()
        assertTrue(result.isFailure)
    }

    @Test
    fun getArticleDetailUseCase_emitsMatchingArticle() = runTest {
        val fakeRepo = FakeArticleRepository()
        val useCase = GetArticleDetailUseCase(fakeRepo)

        fakeRepo.articlesFlow.value = listOf(sampleArticle, sampleArticle2)

        useCase("1").test {
            val article = awaitItem()
            assertEquals("1", article?.id)
            assertEquals("Kotlin Multiplatform 2026", article?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getArticleDetailUseCase_emitsNullWhenNotFound() = runTest {
        val fakeRepo = FakeArticleRepository()
        val useCase = GetArticleDetailUseCase(fakeRepo)

        fakeRepo.articlesFlow.value = listOf(sampleArticle)

        useCase("999").test {
            val article = awaitItem()
            assertNull(article)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun searchArticlesUseCase_filtersByQuery() = runTest {
        val fakeRepo = FakeArticleRepository()
        val useCase = SearchArticlesUseCase(fakeRepo)

        fakeRepo.articlesFlow.value = listOf(sampleArticle, sampleArticle2)

        useCase("Multiplatform").test {
            val articles = awaitItem()
            assertEquals(1, articles.size)
            assertEquals("1", articles[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun resultState_extensionsWorkCorrectly() {
        val successState: ResultState<String> = ResultState.Success("Hello")
        val errorState: ResultState<String> = ResultState.Error(DomainError.NoInternet)
        val loadingState: ResultState<String> = ResultState.Loading

        assertTrue(successState.isSuccess)
        assertFalse(successState.isLoading)
        assertEquals("Hello", successState.dataOrNull())

        assertTrue(loadingState.isLoading)
        assertFalse(loadingState.isSuccess)
        assertNull(loadingState.dataOrNull())

        var onSuccessCalled = false
        successState.onSuccess {
            onSuccessCalled = true
            assertEquals("Hello", it)
        }
        assertTrue(onSuccessCalled)

        var onErrorCalled = false
        errorState.onError {
            onErrorCalled = true
            assertEquals(DomainError.NoInternet, it)
        }
        assertTrue(onErrorCalled)

        val mapped = successState.map { it.length }
        assertEquals(5, mapped.dataOrNull())
    }
}
