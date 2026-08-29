package com.samsul.inosoftapps.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.model.DomainError
import com.samsul.inosoftapps.domain.model.DomainException
import com.samsul.inosoftapps.domain.usecase.GetArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.RefreshArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.SearchArticlesUseCase
import com.samsul.inosoftapps.util.AppConstants
import com.samsul.inosoftapps.util.AppStrings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Article List Screen with pagination support.
 */
data class ArticleListUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val currentPage: Int = AppConstants.INITIAL_PAGE,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true
)

/**
 * ViewModel managing the article feed, categories, search, refresh, and pagination (infinite scroll).
 */
class ArticleListViewModel(
    private val getArticlesUseCase: GetArticlesUseCase,
    private val refreshArticlesUseCase: RefreshArticlesUseCase,
    private val searchArticlesUseCase: SearchArticlesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleListUiState())
    val uiState: StateFlow<ArticleListUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var refreshJob: Job? = null

    init {
        loadArticles()
        refreshArticles()
    }

    /**
     * Selects and filters by category (e.g. 'technology', 'business', etc.).
     */
    fun selectCategory(category: String?) {
        if (_uiState.value.selectedCategory == category && _uiState.value.searchQuery.isEmpty()) return
        _uiState.update {
            it.copy(
                selectedCategory = category,
                searchQuery = "",
                articles = emptyList(),
                isLoading = true,
                isRefreshing = false,
                currentPage = AppConstants.INITIAL_PAGE,
                canLoadMore = true
            )
        }
        refreshArticles(category)
        loadArticles(category)
    }

    /**
     * Observes local articles from Room DB for the current category.
     */
    fun loadArticles(category: String? = _uiState.value.selectedCategory) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            getArticlesUseCase(category)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collectLatest { articles ->
                    _uiState.update { current ->
                        val isNetworkLoading = refreshJob?.isActive == true
                        current.copy(
                            articles = articles,
                            isLoading = if (articles.isNotEmpty()) false else isNetworkLoading
                        )
                    }
                }
        }
    }

    /**
     * Performs a search over articles.
     */
    fun searchArticles(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        observeJob?.cancel()
        refreshJob?.cancel()
        if (query.isBlank()) {
            loadArticles(_uiState.value.selectedCategory)
            return
        }

        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isRefreshing = false) }
            searchArticlesUseCase(query)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collectLatest { articles ->
                    _uiState.update { it.copy(articles = articles, isLoading = false) }
                }
        }
    }

    /**
     * Triggers a remote refresh from Ktor NewsAPI into Room DB for page 1.
     */
    fun refreshArticles(category: String? = _uiState.value.selectedCategory) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val hasCachedArticles = _uiState.value.articles.isNotEmpty()
            _uiState.update {
                it.copy(
                    isLoading = !hasCachedArticles,
                    isRefreshing = hasCachedArticles,
                    errorMessage = null,
                    currentPage = AppConstants.INITIAL_PAGE,
                    canLoadMore = true
                )
            }

            val result = refreshArticlesUseCase(category = category, page = AppConstants.INITIAL_PAGE)

            result.onSuccess { refreshResult ->
                _uiState.update { current ->
                    current.copy(
                        isOffline = false,
                        isRefreshing = false,
                        isLoading = if (refreshResult.articleCount == 0 && current.articles.isEmpty()) {
                            false
                        } else if (current.articles.isNotEmpty()) {
                            false
                        } else {
                            current.isLoading
                        },
                        currentPage = AppConstants.INITIAL_PAGE,
                        canLoadMore = refreshResult.hasMore
                    )
                }
            }

            result.onFailure { exception ->
                val isNetworkIssue = exception is DomainException &&
                        (exception.error is DomainError.NoInternet || exception.error is DomainError.Timeout)

                val errorMsg = mapExceptionToMessage(exception)

                _uiState.update {
                    it.copy(
                        isOffline = isNetworkIssue,
                        errorMessage = errorMsg,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    /**
     * Fetches the next page of articles and appends them to Room DB without triggering full-screen loading.
     */
    fun loadMoreArticles() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isRefreshing || currentState.isLoadingMore || !currentState.canLoadMore || currentState.searchQuery.isNotBlank()) {
            return
        }

        val nextPage = currentState.currentPage + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            val result = refreshArticlesUseCase(category = currentState.selectedCategory, page = nextPage)

            result.onSuccess { refreshResult ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        currentPage = nextPage,
                        canLoadMore = refreshResult.hasMore
                    )
                }
            }

            result.onFailure { exception ->
                val errorMsg = mapExceptionToMessage(exception)
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    private fun mapExceptionToMessage(exception: Throwable): String {
        return exception.message ?: AppStrings.LOAD_FAILED_MESSAGE
    }

    /**
     * Clears error message after being displayed.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
