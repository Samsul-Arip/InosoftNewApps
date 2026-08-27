package com.samsul.inosoftapps.presentation.navigation

import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLQueryComponent

/**
 * Sealed destination routes for application navigation.
 */
sealed class Screen(val route: String) {

    /**
     * Feed list screen route.
     */
    data object ArticleList : Screen("article_list")

    /**
     * Article detail screen route with safe parameter encoding/decoding.
     */
    data object ArticleDetail : Screen("article_detail/{articleId}") {
        const val ARG_ARTICLE_ID = "articleId"

        /**
         * Safely encodes [articleId] into navigation route to prevent crashes from slashes or special characters.
         */
        fun createRoute(articleId: String): String {
            val encodedId = articleId.encodeURLQueryComponent(encodeFull = true)
            return "article_detail/$encodedId"
        }

        /**
         * Decodes route argument back to original [articleId].
         */
        fun decodeArticleId(rawArgument: String?): String {
            if (rawArgument.isNullOrBlank()) return ""
            return try {
                rawArgument.decodeURLQueryComponent()
            } catch (e: Exception) {
                rawArgument
            }
        }
    }
}
