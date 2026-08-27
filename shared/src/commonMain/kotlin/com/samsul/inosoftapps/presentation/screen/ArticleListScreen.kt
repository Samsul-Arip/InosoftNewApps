package com.samsul.inosoftapps.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samsul.inosoftapps.presentation.component.ArticleCard
import com.samsul.inosoftapps.presentation.component.EmptyView
import com.samsul.inosoftapps.presentation.component.FullScreenImageViewer
import com.samsul.inosoftapps.presentation.component.LoadingView
import com.samsul.inosoftapps.presentation.theme.NewsReaderTheme
import com.samsul.inosoftapps.presentation.util.SampleData
import com.samsul.inosoftapps.presentation.viewmodel.ArticleListUiState
import com.samsul.inosoftapps.presentation.viewmodel.ArticleListViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

private val CATEGORIES = listOf(
    null to "Semua",
    "business" to "Bisnis",
    "technology" to "Teknologi",
    "sports" to "Olahraga",
    "health" to "Kesehatan",
    "science" to "Sains",
    "entertainment" to "Hiburan"
)

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
    onArticleClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    listState: LazyListState = rememberLazyListState()
) {
    var selectedFullImage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!isSearchActive) {
                        Text(
                            text = "News Reader",
                            style = MaterialTheme.typography.titleLarge
                        )
                    } else {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = { Text("Cari berita...") },
                            singleLine = true,
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = onClearSearch) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search")
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
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchToggled) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchActive) "Tutup pencarian" else "Cari berita"
                        )
                    }
                    if (!isSearchActive) {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Muat ulang"
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
            // Category Chips Row (Shown when not searching)
            AnimatedVisibility(visible = !isSearchActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORIES.forEach { (catKey, catLabel) ->
                        val isSelected = uiState.selectedCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelected(catKey) },
                            label = { Text(catLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Main Content Area with PullToRefresh
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.articles.isEmpty() -> {
                        LoadingView(message = "Memuat berita terbaru...")
                    }
                    !uiState.isLoading && uiState.articles.isEmpty() -> {
                        EmptyView(
                            title = if (uiState.searchQuery.isNotEmpty()) "Hasil tidak ditemukan" else "Tidak ada berita",
                            message = if (uiState.searchQuery.isNotEmpty()) "Tidak ada berita dengan kata kunci '${uiState.searchQuery}'" else "Gagal mengambil data atau belum ada berita.",
                            onRetry = onRefresh
                        )
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = uiState.articles,
                                key = { it.id }
                            ) { article ->
                                ArticleCard(
                                    article = article,
                                    onClick = { onArticleClick(article.id) },
                                    onImageClick = { imgUrl -> selectedFullImage = imgUrl }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal FullScreen Image Dialog
    selectedFullImage?.let { imageUrl ->
        FullScreenImageViewer(
            imageUrl = imageUrl,
            onDismiss = { selectedFullImage = null }
        )
    }
}

// ================= PREVIEWS =================

@Preview
@Composable
private fun ArticleListContentPreview_Light() {
    NewsReaderTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ArticleListContent(
                uiState = ArticleListUiState(
                    articles = SampleData.sampleArticles,
                    isLoading = false,
                    isRefreshing = false,
                    selectedCategory = "technology"
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
}

@Preview
@Composable
private fun ArticleListContentPreview_Dark() {
    NewsReaderTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ArticleListContent(
                uiState = ArticleListUiState(
                    articles = SampleData.sampleArticles,
                    isLoading = false,
                    isRefreshing = false,
                    selectedCategory = "technology"
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
}
