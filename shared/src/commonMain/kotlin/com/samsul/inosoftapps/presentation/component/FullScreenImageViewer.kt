package com.samsul.inosoftapps.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import com.samsul.inosoftapps.presentation.theme.NewsReaderTheme
import com.samsul.inosoftapps.presentation.util.SampleData
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Full-screen modal dialog for inspecting article images with dark backdrop and close action.
 */
@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    title: String? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        FullScreenImageContent(
            imageUrl = imageUrl,
            title = title,
            onDismiss = onDismiss
        )
    }
}

/**
 * Stateless content for FullScreenImageViewer so it can be previewed directly.
 */
@Composable
fun FullScreenImageContent(
    imageUrl: String,
    title: String? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = title ?: "Full screen article image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            loading = {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        )

        // Close button top-end
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        ) {
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close full image",
                    tint = Color.White
                )
            }
        }
    }
}

// ================= PREVIEWS =================

@Preview
@Composable
private fun FullScreenImageViewerPreview_Light() {
    NewsReaderTheme(darkTheme = false) {
        FullScreenImageContent(
            imageUrl = SampleData.sampleArticle.imageUrl ?: "",
            title = SampleData.sampleArticle.title,
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun FullScreenImageViewerPreview_Dark() {
    NewsReaderTheme(darkTheme = true) {
        FullScreenImageContent(
            imageUrl = SampleData.sampleArticle.imageUrl ?: "",
            title = SampleData.sampleArticle.title,
            onDismiss = {}
        )
    }
}
