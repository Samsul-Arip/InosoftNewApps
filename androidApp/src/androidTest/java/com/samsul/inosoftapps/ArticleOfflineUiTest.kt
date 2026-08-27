package com.samsul.inosoftapps

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.samsul.inosoftapps.presentation.screen.ArticleListContent
import com.samsul.inosoftapps.presentation.theme.NewsReaderTheme
import com.samsul.inosoftapps.presentation.util.SampleData
import com.samsul.inosoftapps.presentation.viewmodel.ArticleListUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArticleOfflineUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysOfflineBanner_whenOfflineWithCachedArticles() {
        composeTestRule.setContent {
            NewsReaderTheme {
                ArticleListContent(
                    uiState = ArticleListUiState(
                        articles = SampleData.sampleArticles,
                        isLoading = false,
                        isOffline = true
                    ),
                    isSearchActive = false,
                    onSearchToggled = {},
                    onSearchQueryChanged = {},
                    onClearSearch = {},
                    onCategorySelected = {},
                    onRefresh = {},
                    onArticleClick = {}
                )
            }
        }

        // Verify offline banner is displayed
        composeTestRule.onNodeWithText("Mode Offline — Menampilkan berita yang tersimpan").assertIsDisplayed()

        // Verify cached articles are still rendered
        composeTestRule.onNodeWithText(SampleData.sampleArticle.title).assertIsDisplayed()
    }

    @Test
    fun displaysEmptyViewAndHandlesRetry_whenArticlesEmpty() {
        var retryClicked by mutableStateOf(false)

        composeTestRule.setContent {
            NewsReaderTheme {
                ArticleListContent(
                    uiState = ArticleListUiState(
                        articles = emptyList(),
                        isLoading = false,
                        isOffline = false
                    ),
                    isSearchActive = false,
                    onSearchToggled = {},
                    onSearchQueryChanged = {},
                    onClearSearch = {},
                    onCategorySelected = {},
                    onRefresh = { retryClicked = true },
                    onArticleClick = {}
                )
            }
        }

        // Verify empty view message is displayed
        composeTestRule.onNodeWithText("Tidak ada berita").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coba Lagi").assertIsDisplayed()

        // Perform click on retry button
        composeTestRule.onNodeWithText("Coba Lagi").performClick()
        assertTrue(retryClicked)
    }
}
