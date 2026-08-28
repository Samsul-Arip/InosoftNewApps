package com.samsul.inosoftapps.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.samsul.inosoftapps.presentation.component.ArticleCard
import com.samsul.inosoftapps.presentation.component.EmptyView
import com.samsul.inosoftapps.presentation.component.LoadingView
import com.samsul.inosoftapps.presentation.theme.NewsReaderTheme
import com.samsul.inosoftapps.presentation.util.SampleData
import com.samsul.inosoftapps.presentation.viewmodel.ArticleListUiState
import com.samsul.inosoftapps.presentation.viewmodel.ArticleListViewModel
import com.samsul.inosoftapps.util.AppConstants
import com.samsul.inosoftapps.util.AppStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Stateful ArticleListScreen observing ViewModel state.
 */
@Composable
fun ArticleListScreen(
    viewModel: ArticleListViewModel,
    onArticleClick: (articleId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSearchActive by remember { mutableStateOf(false) }

    // Non-blocking Snackbar alert for error notifications
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    ArticleListContent(
        uiState = uiState,
        isSearchActive = isSearchActive,
        snackbarHostState = snackbarHostState,
        onSearchToggled = {
            isSearchActive = !isSearchActive
            if (!isSearchActive) {
                viewModel.searchArticles("")
            }
        },
        onSearchQueryChanged = { viewModel.searchArticles(it) },
        onClearSearch = { viewModel.searchArticles("") },
        onCategorySelected = { viewModel.selectCategory(it) },
        onRefresh = { viewModel.refreshArticles() },
        onLoadMore = { viewModel.loadMoreArticles() },
        onArticleClick = onArticleClick,
        modifier = modifier
    )
}

/**
 * Pure Stateless ArticleListContent composable for flexible testing and Android Studio Previews.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleListContent(
    uiState: ArticleListUiState,
    isSearchActive: Boolean,
    onSearchToggled: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit = {},
    onArticleClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    listState: LazyListState = rememberLazyListState()
) {
    // Scroll to top when category changes
    LaunchedEffect(uiState.selectedCategory) {
        listState.scrollToItem(0)
    }

    // Automatic infinite scroll detection when reaching bottom 3 items
    val isNearBottom by remember(listState) {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 3
        }
    }

    LaunchedEffect(
        isNearBottom,
        uiState.isLoading,
        uiState.isRefreshing,
        uiState.isLoadingMore,
        uiState.canLoadMore,
        uiState.searchQuery
    ) {
        if (isNearBottom &&
            !uiState.isLoading &&
            !uiState.isRefreshing &&
            !uiState.isLoadingMore &&
            uiState.canLoadMore &&
            uiState.searchQuery.isBlank()
        ) {
            onLoadMore()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!isSearchActive) {
                        Text(
                            text = AppStrings.APP_NAME,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.testTag(AppConstants.TestTags.APP_TITLE)
                        )
                    } else {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = { Text(AppStrings.SEARCH_PLACEHOLDER) },
                            singleLine = true,
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = onClearSearch,
                                        modifier = Modifier.testTag(AppConstants.TestTags.CLEAR_SEARCH_BUTTON)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = AppStrings.CLEAR_SEARCH_DESC)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                                .testTag(AppConstants.TestTags.SEARCH_TEXT_FIELD)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSearchToggled,
                        modifier = Modifier.testTag(AppConstants.TestTags.SEARCH_TOGGLE_BUTTON)
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchActive) AppStrings.CLOSE_SEARCH_DESC else AppStrings.SEARCH_BUTTON_DESC
                        )
                    }
                    if (!isSearchActive) {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.testTag(AppConstants.TestTags.REFRESH_BUTTON)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = AppStrings.REFRESH_BUTTON_DESC
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Mode Offline Banner (Animated when offline with cached data)
            AnimatedVisibility(
                visible = uiState.isOffline && uiState.articles.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag(AppConstants.TestTags.OFFLINE_BANNER)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = AppStrings.OFFLINE_BANNER_DESC,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.OFFLINE_BANNER_TEXT,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Category Chips Row (Shown when not searching)
            AnimatedVisibility(visible = !isSearchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag(AppConstants.TestTags.CATEGORY_CHIPS_ROW),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppConstants.CATEGORIES.forEach { (catKey, catLabel) ->
                        val isSelected = uiState.selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelected(catKey) },
                            label = { Text(catLabel) },
                            modifier = Modifier.testTag(AppConstants.TestTags.categoryChip(catKey)),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Main Content Area with Material 3 PullToRefreshBox
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    (uiState.isLoading || uiState.isRefreshing) && uiState.articles.isEmpty() -> {
                        LoadingView(message = AppStrings.LOADING_MESSAGE)
                    }
                    !uiState.isLoading && !uiState.isRefreshing && uiState.articles.isEmpty() -> {
                        EmptyView(
                            title = if (uiState.searchQuery.isNotEmpty()) AppStrings.EMPTY_DATA_ERROR else AppStrings.EMPTY_ARTICLE_TITLE,
                            message = if (uiState.searchQuery.isNotEmpty()) AppStrings.searchEmptyMessage(uiState.searchQuery) else AppStrings.EMPTY_ARTICLE_MESSAGE,
                            onRetry = onRefresh
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(4.dp)) }

                            items(
                                items = uiState.articles,
                                key = { it.id }
                            ) { article ->
                                ArticleCard(
                                    article = article,
                                    onClick = { onArticleClick(article.id) }
                                )
                            }

                            // Infinite scroll loading indicator at bottom of list
                            if (uiState.isLoadingMore) {
                                item(key = "pagination_loading_indicator") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            strokeWidth = 3.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// ================= PREVIEWS =================

@Preview
@Composable
private fun ArticleListPreview_Light() {
    NewsReaderTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ArticleListContent(
                uiState = ArticleListUiState(
                    articles = SampleData.sampleArticles,
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = false,
                    selectedCategory = null
                ),
                isSearchActive = false,
                onSearchToggled = {},
                onSearchQueryChanged = {},
                onClearSearch = {},
                onCategorySelected = {},
                onRefresh = {},
                onLoadMore = {},
                onArticleClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun ArticleListPreview_Dark_Offline() {
    NewsReaderTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ArticleListContent(
                uiState = ArticleListUiState(
                    articles = SampleData.sampleArticles,
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = true,
                    selectedCategory = "technology"
                ),
                isSearchActive = false,
                onSearchToggled = {},
                onSearchQueryChanged = {},
                onClearSearch = {},
                onCategorySelected = {},
                onRefresh = {},
                onLoadMore = {},
                onArticleClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun ArticleListPreview_EmptyState() {
    NewsReaderTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ArticleListContent(
                uiState = ArticleListUiState(
                    articles = emptyList(),
                    isLoading = false,
                    isRefreshing = false,
                    isOffline = false
                ),
                isSearchActive = false,
                onSearchToggled = {},
                onSearchQueryChanged = {},
                onClearSearch = {},
                onCategorySelected = {},
                onRefresh = {},
                onLoadMore = {},
                onArticleClick = {}
            )
        }
    }
}
