package com.samsul.inosoftapps.domain.model

/**
 * Result data holder for article refresh operation.
 * @property hasMore Indicates if more pages can be loaded for pagination.
 * @property articleCount Number of articles fetched and cached during the refresh.
 */
data class RefreshResult(
    val hasMore: Boolean,
    val articleCount: Int
)
