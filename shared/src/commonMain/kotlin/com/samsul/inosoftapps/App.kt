package com.samsul.inosoftapps

import androidx.compose.runtime.Composable
import com.samsul.inosoftapps.presentation.navigation.NavGraph
import com.samsul.inosoftapps.presentation.theme.NewsReaderTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext

/**
 * Root Composable entry point for Android and iOS applications.
 */
@Composable
@Preview
fun App() {
    KoinContext {
        NewsReaderTheme {
            NavGraph()
        }
    }
}