package com.samsul.inosoftapps.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samsul.inosoftapps.presentation.screen.ArticleDetailScreen
import com.samsul.inosoftapps.presentation.screen.ArticleListScreen
import com.samsul.inosoftapps.presentation.viewmodel.ArticleDetailViewModel
import com.samsul.inosoftapps.presentation.viewmodel.ArticleListViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main application navigation graph connecting feed list and article detail destinations.
 */
@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ArticleList.route,
        modifier = modifier
    ) {
        composable(route = Screen.ArticleList.route) {
            val viewModel = koinViewModel<ArticleListViewModel>()
            ArticleListScreen(
                viewModel = viewModel,
                onArticleClick = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                }
            )
        }

        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(
                navArgument(Screen.ArticleDetail.ARG_ARTICLE_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getString(Screen.ArticleDetail.ARG_ARTICLE_ID)
            val articleId = Screen.ArticleDetail.decodeArticleId(rawId)
            val viewModel = koinViewModel<ArticleDetailViewModel>()

            ArticleDetailScreen(
                articleId = articleId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
