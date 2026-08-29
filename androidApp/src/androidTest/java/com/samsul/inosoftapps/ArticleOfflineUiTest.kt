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
import com.samsul.inosoftapps.util.AppStrings
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArticleOfflineUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * UI Test: Verifies that when the device is offline but cached articles exist,
     * the offline indicator banner is displayed while cached articles remain visible and accessible.
     */
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
        composeTestRule.onNodeWithText(AppStrings.OFFLINE_BANNER_TEXT).assertIsDisplayed()

        // Verify cached articles are still rendered
        composeTestRule.onNodeWithText(SampleData.sampleArticle.title).assertIsDisplayed()
    }

    /**
     * UI Test: Verifies that when the article list is empty (and not in offline error state),
     * a clean EmptyView with explanatory message is rendered.
     */
    @Test
    fun displaysEmptyView_whenArticlesEmpty() {
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
                    onRefresh = {},
                    onArticleClick = {}
                )
            }
        }

        // Verify empty view title and message are displayed
        composeTestRule.onNodeWithText(AppStrings.EMPTY_ARTICLE_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppStrings.EMPTY_ARTICLE_MESSAGE).assertIsDisplayed()
    }

    /**
     * UI Test: Verifies that when offline and no cached articles exist,
     * a dedicated NoConnectionView card with a Retry button is displayed and clicking Retry triggers onRefresh.
     */
    @Test
    fun displaysNoConnectionCard_whenOfflineAndArticlesEmpty() {
        var retryClicked by mutableStateOf(false)

        composeTestRule.setContent {
            NewsReaderTheme {
                ArticleListContent(
                    uiState = ArticleListUiState(
                        articles = emptyList(),
                        isLoading = false,
                        isOffline = true
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

        // Verify NoConnectionView card with title and card message is displayed
        composeTestRule.onNodeWithText(AppStrings.NO_INTERNET_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppStrings.NO_INTERNET_CARD_MESSAGE).assertIsDisplayed()
        composeTestRule.onNodeWithText(AppStrings.RETRY_BUTTON).assertIsDisplayed()

        // Perform click on retry button in NoConnectionView
        composeTestRule.onNodeWithText(AppStrings.RETRY_BUTTON).performClick()
        composeTestRule.waitForIdle()
        assertTrue(retryClicked)
    }
}
