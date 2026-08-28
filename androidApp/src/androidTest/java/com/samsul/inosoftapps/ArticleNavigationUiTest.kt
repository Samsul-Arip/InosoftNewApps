package com.samsul.inosoftapps

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.samsul.inosoftapps.presentation.screen.ArticleDetailContent
import com.samsul.inosoftapps.presentation.screen.ArticleListContent
import com.samsul.inosoftapps.presentation.theme.NewsReaderTheme
import com.samsul.inosoftapps.presentation.util.SampleData
import com.samsul.inosoftapps.presentation.viewmodel.ArticleDetailUiState
import com.samsul.inosoftapps.presentation.viewmodel.ArticleListUiState
import com.samsul.inosoftapps.util.AppStrings
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArticleNavigationUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickingArticle_navigatesToDetailScreen() {
        var selectedArticleId by mutableStateOf<String?>(null)

        composeTestRule.setContent {
            NewsReaderTheme {
                if (selectedArticleId == null) {
                    ArticleListContent(
                        uiState = ArticleListUiState(
                            articles = SampleData.sampleArticles,
                            isLoading = false
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
                            article = SampleData.sampleArticles.find { it.id == selectedArticleId },
                            isLoading = false
                        ),
                        onBackClick = { selectedArticleId = null },
                        onRetry = {}
                    )
                }
            }
        }

        // Verify that the first article is displayed in the list
        val articleTitle = SampleData.sampleArticle.title
        composeTestRule.onNodeWithText(articleTitle).assertIsDisplayed()

        // Perform click on article item
        composeTestRule.onNodeWithText(articleTitle).performClick()
        composeTestRule.waitForIdle()

        // Verify detail screen is displayed
        composeTestRule.onNodeWithText(AppStrings.ARTICLE_DETAIL_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithText(articleTitle).assertIsDisplayed()

        // Click Back button and verify returning to list
        composeTestRule.onNodeWithContentDescription(AppStrings.BACK_BUTTON_DESC).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(AppStrings.APP_NAME).assertIsDisplayed()
    }
}
