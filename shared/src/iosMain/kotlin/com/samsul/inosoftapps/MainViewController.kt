package com.samsul.inosoftapps

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.samsul.inosoftapps.di.initKoinIos
import com.samsul.inosoftapps.presentation.screen.ArticleDetailContent
import com.samsul.inosoftapps.presentation.screen.ArticleListContent
import com.samsul.inosoftapps.presentation.theme.NewsReaderTheme
import com.samsul.inosoftapps.presentation.util.SampleData
import com.samsul.inosoftapps.presentation.viewmodel.ArticleDetailUiState
import com.samsul.inosoftapps.presentation.viewmodel.ArticleListUiState

private var isKoinInitialized = false

/**
 * Initializes Koin once on iOS.
 */
fun initKoin() {
    if (!isKoinInitialized) {
        initKoinIos()
        isKoinInitialized = true
    }
}

/**
 * UIViewController entry point for iOS application.
 * Automatically initializes Koin before rendering the Compose UI.
 */
fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}

/**
 * Testable UIViewController entry point mirroring the androidTest Compose test setup.
 * Used for deterministic, hermetic UI testing on iOS without flaky network dependencies.
 */
fun TestViewController(isOffline: Boolean = false) = ComposeUIViewController {
    NewsReaderTheme {
        var selectedArticleId by remember { mutableStateOf<String?>(null) }

        if (selectedArticleId == null) {
            ArticleListContent(
                uiState = ArticleListUiState(
                    articles = SampleData.sampleArticles,
                    isLoading = false,
                    isOffline = isOffline
                ),
                isSearchActive = false,
                onSearchToggled = {},
                onSearchQueryChanged = {},
                onClearSearch = {},
                onCategorySelected = {},
                onRefresh = {},
                onArticleClick = { selectedArticleId = it }
            )
        } else {
            ArticleDetailContent(
                uiState = ArticleDetailUiState(
                    article = SampleData.sampleArticles.find { it.id == selectedArticleId } ?: SampleData.sampleArticle,
                    isLoading = false
                ),
                onBackClick = { selectedArticleId = null },
                onRetry = {}
            )
        }
    }
}