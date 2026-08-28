package com.samsul.inosoftapps.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.samsul.inosoftapps.presentation.theme.NewsReaderTheme
import com.samsul.inosoftapps.util.AppConstants
import com.samsul.inosoftapps.util.AppStrings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Dedicated Card component displayed when there is no internet connection and no cached articles.
 */
@Composable
fun NoConnectionView(
    title: String = AppStrings.NO_INTERNET_TITLE,
    message: String = AppStrings.NO_INTERNET_CARD_MESSAGE,
    buttonText: String = AppStrings.RETRY_BUTTON,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(AppConstants.TestTags.NO_CONNECTION_VIEW)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
            ) {
                // Warning / Disconnected Icon Badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Message
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Retry Action Button
                if (onRetry != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onRetry,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag(AppConstants.TestTags.NO_CONNECTION_RETRY_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = buttonText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = buttonText)
                    }
                }
            }
        }
    }
}

// ================= PREVIEWS =================

@Preview
@Composable
private fun NoConnectionViewPreview_Light() {
    NewsReaderTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            NoConnectionView(
                title = AppStrings.NO_INTERNET_TITLE,
                message = AppStrings.NO_INTERNET_CARD_MESSAGE,
                onRetry = {}
            )
        }
    }
}

@Preview
@Composable
private fun NoConnectionViewPreview_Dark() {
    NewsReaderTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            NoConnectionView(
                title = AppStrings.NO_INTERNET_TITLE,
                message = AppStrings.NO_INTERNET_CARD_MESSAGE,
                onRetry = {}
            )
        }
    }
}
