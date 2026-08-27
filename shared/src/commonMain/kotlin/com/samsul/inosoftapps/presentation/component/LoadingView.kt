package com.samsul.inosoftapps.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samsul.inosoftapps.presentation.theme.NewsReaderTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Centered progress indicator with optional loading message.
 */
@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    message: String? = "Memuat berita..."
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(44.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.5.dp
            )
            if (!message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ================= PREVIEWS =================

@Preview
@Composable
private fun LoadingViewPreview_Light() {
    NewsReaderTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            LoadingView(message = "Memuat berita terbaru...")
        }
    }
}

@Preview
@Composable
private fun LoadingViewPreview_Dark() {
    NewsReaderTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            LoadingView(message = "Memuat berita terbaru...")
        }
    }
}
