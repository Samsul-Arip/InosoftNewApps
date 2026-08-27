package com.samsul.inosoftapps.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.model.DomainError
import com.samsul.inosoftapps.domain.model.DomainException
import com.samsul.inosoftapps.domain.usecase.GetArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.RefreshArticlesUseCase
import com.samsul.inosoftapps.domain.usecase.SearchArticlesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for the Article List Screen.
 */
data class ArticleListUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val errorMessage: String? = null
)

/**
 * ViewModel managing the article feed, categories, search, and refresh actions.
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
        _uiState.update { it.copy(selectedCategory = category, searchQuery = "") }
        loadArticles(category)
        refreshArticles()
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
     * Triggers a remote refresh from Ktor NewsAPI into Room DB.
     */
    fun refreshArticles(isInitial: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (isInitial && it.articles.isEmpty()) it.copy(isLoading = true, errorMessage = null)
                else it.copy(isRefreshing = true, errorMessage = null)
            }

            val result = refreshArticlesUseCase(_uiState.value.selectedCategory)

            result.onFailure { exception ->
                val errorMsg = when (exception) {
                    is DomainException -> when (exception.error) {
                        is DomainError.NoInternet -> "Tidak ada koneksi internet. Menampilkan berita tersimpan."
                        is DomainError.Timeout -> "Koneksi time out. Coba beberapa saat lagi."
                        is DomainError.ServerError -> exception.error.message ?: "Terjadi kesalahan pada server."
                        is DomainError.EmptyData -> "Tidak ada berita ditemukan."
                        is DomainError.NotFound -> "Data tidak ditemukan."
                        is DomainError.Unknown -> exception.error.message ?: "Terjadi kesalahan."
                    }
                    else -> exception.message ?: "Gagal memuat berita terbaru."
                }
                _uiState.update { it.copy(errorMessage = errorMsg) }
            }

            _uiState.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    /**
     * Clears error message after being displayed.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
