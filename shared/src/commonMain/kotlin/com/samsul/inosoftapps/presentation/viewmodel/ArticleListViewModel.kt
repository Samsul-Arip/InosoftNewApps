package com.samsul.inosoftapps.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.model.DomainError
import com.samsul.inosoftapps.domain.model.DomainException
import com.samsul.inosoftapps.domain.usecase.GetArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.RefreshArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.SearchArticlesUseCase
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
    val currentPage: Int = 1,
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

    init {
        loadArticles()
        refreshArticles(isInitial = true)
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
                currentPage = 1,
                canLoadMore = true
            )
        }
        loadArticles(category)
        refreshArticles(category)
    }

    /**
     * Observes local articles from Room DB for the current category.
     */
    fun loadArticles(category: String? = _uiState.value.selectedCategory) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.articles.isEmpty()) }
            getArticlesUseCase(category)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collectLatest { articles ->
                    _uiState.update { it.copy(articles = articles, isLoading = false) }
                }
        }
    }

    /**
     * Performs a search over articles.
     */
    fun searchArticles(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        observeJob?.cancel()
        if (query.isBlank()) {
            loadArticles(_uiState.value.selectedCategory)
            return
        }

        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
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
    fun refreshArticles(category: String? = _uiState.value.selectedCategory, isInitial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (isInitial && it.articles.isEmpty()) it.copy(isLoading = true, errorMessage = null, currentPage = 1, canLoadMore = true)
                else it.copy(isRefreshing = true, errorMessage = null, currentPage = 1, canLoadMore = true)
            }

            val result = refreshArticlesUseCase(category = category, page = 1)

            result.onSuccess { hasMore ->
                _uiState.update {
                    it.copy(
                        isOffline = false,
                        isLoading = false,
                        isRefreshing = false,
                        currentPage = 1,
                        canLoadMore = hasMore
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

            result.onSuccess { hasMore ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        currentPage = nextPage,
                        canLoadMore = hasMore
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
        return when (exception) {
            is DomainException -> when (exception.error) {
                is DomainError.NoInternet -> AppStrings.NO_INTERNET_MESSAGE
                is DomainError.Timeout -> AppStrings.TIMEOUT_MESSAGE
                is DomainError.ServerError -> exception.error.message ?: AppStrings.SERVER_ERROR_DEFAULT
                is DomainError.EmptyData -> AppStrings.EMPTY_DATA_ERROR
                is DomainError.NotFound -> AppStrings.DATA_NOT_FOUND_ERROR
                is DomainError.Unknown -> exception.error.message ?: AppStrings.UNKNOWN_ERROR
            }
            else -> exception.message ?: AppStrings.LOAD_FAILED_MESSAGE
        }
    }

    /**
     * Clears error message after being displayed.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
