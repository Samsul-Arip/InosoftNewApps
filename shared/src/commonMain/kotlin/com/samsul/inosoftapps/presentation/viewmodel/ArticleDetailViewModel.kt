package com.samsul.inosoftapps.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samsul.inosoftapps.domain.model.Article
import com.samsul.inosoftapps.domain.usecase.GetArticleDetailUseCase
import com.samsul.inosoftapps.util.AppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Article Detail Screen.
 */
data class ArticleDetailUiState(
    val article: Article? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel managing article detail lookup by id.
 */
class ArticleDetailViewModel(
    private val getArticleDetailUseCase: GetArticleDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    /**
     * Loads article detail by [id] from Room database.
     */
    fun loadArticleDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getArticleDetailUseCase(id)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collectLatest { article ->
                    _uiState.update {
                        it.copy(
                            article = article,
                            isLoading = false,
                            errorMessage = if (article == null) AppStrings.ARTICLE_NOT_FOUND else null
                        )
                    }
                }
        }
    }
}
