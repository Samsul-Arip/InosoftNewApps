package com.samsul.inosoftapps.presentation.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationRouteTest {

    @Test
    fun createRoute_encodesSpecialCharactersSafely() {
        val rawArticleId = "https://theverge.com/article/kmp?query=1&type=news#header"
        val route = Screen.ArticleDetail.createRoute(rawArticleId)

        // Must not contain raw slashes after the route prefix that would confuse Navigation path segments
        val segmentAfterPrefix = route.removePrefix("article_detail/")
        assertEquals(false, segmentAfterPrefix.contains("/"))

        // Decoding should perfectly restore the original article id
        val decoded = Screen.ArticleDetail.decodeArticleId(segmentAfterPrefix)
        assertEquals(rawArticleId, decoded)
    }

    @Test
    fun decodeArticleId_handlesNullAndBlankGracefully() {
        assertEquals("", Screen.ArticleDetail.decodeArticleId(null))
        assertEquals("", Screen.ArticleDetail.decodeArticleId(""))
        assertEquals("", Screen.ArticleDetail.decodeArticleId("   "))
    }
}
