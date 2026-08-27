package com.samsul.inosoftapps.data.remote

import com.samsul.inosoftapps.data.mapper.toDomain
import com.samsul.inosoftapps.data.mapper.toDomainList
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NewsApiServiceTest {

    private val sampleSuccessJson = """
        {
          "status": "ok",
          "totalResults": 2,
          "articles": [
            {
              "source": { "id": "techcrunch", "name": "TechCrunch" },
              "author": "John Doe",
              "title": "KMP and Compose Multiplatform in 2026",
              "description": "Cross platform development reaches new heights.",
              "url": "https://techcrunch.com/kmp-2026",
              "urlToImage": "https://techcrunch.com/image.png",
              "publishedAt": "2026-08-27T08:00:00Z",
              "content": "Full article content here."
            },
            {
              "source": { "id": null, "name": "Google News" },
              "author": null,
              "title": "[Removed]",
              "description": null,
              "url": "https://news.google.com/removed",
              "urlToImage": null,
              "publishedAt": "2026-08-27T07:00:00Z",
              "content": null
            }
          ]
        }
    """.trimIndent()

    private val sampleEmptyJson = """
        {
          "status": "ok",
          "totalResults": 0,
          "articles": []
        }
    """.trimIndent()

    private val sampleFallbackJson = """
        {
          "status": "ok",
          "totalResults": 1,
          "articles": [
            {
              "source": { "id": "bbc-news", "name": "BBC News" },
              "author": "BBC Tech",
              "title": "Global Innovation Wave",
              "description": "Tech innovations happening across the world.",
              "url": "https://bbc.com/news/global-tech",
              "urlToImage": "https://bbc.com/image.jpg",
              "publishedAt": "2026-08-27T09:00:00Z",
              "content": "BBC content."
            }
          ]
        }
    """.trimIndent()

    @Test
    fun getTopHeadlines_parsesResponseCorrectly() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("https://newsapi.org/v2/top-headlines?country=us&page=1&pageSize=20", request.url.toString())
            respond(
                content = ByteReadChannel(sampleSuccessJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = KtorClientFactory.createHttpClient(engine = mockEngine)
        val apiService = KtorNewsApiService(client = httpClient)

        val response = apiService.getTopHeadlines(country = "us")

        assertEquals("ok", response.status)
        assertEquals(2, response.totalResults)
        assertEquals(2, response.articles?.size)

        // Test mapper & filtering out [Removed] articles
        val domainArticles = response.articles.toDomainList(category = "technology")
        assertEquals(1, domainArticles.size)
        val first = domainArticles.first()
        assertEquals("KMP and Compose Multiplatform in 2026", first.title)
        assertEquals("TechCrunch", first.sourceName)
        assertEquals("https://techcrunch.com/kmp-2026", first.url)
        assertEquals("technology", first.category)
    }

    @Test
    fun getTopHeadlines_triggersSmartFallbackWhenCountryReturnsEmpty() = runTest {
        var callCount = 0
        val mockEngine = MockEngine { request ->
            callCount++
            if (request.url.parameters["country"] == "id") {
                respond(
                    content = ByteReadChannel(sampleEmptyJson),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            } else {
                respond(
                    content = ByteReadChannel(sampleFallbackJson),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }

        val httpClient = KtorClientFactory.createHttpClient(engine = mockEngine)
        val apiService = KtorNewsApiService(client = httpClient)

        val response = apiService.getTopHeadlines(country = "id")

        assertEquals(2, callCount) // 1st call to 'id', 2nd fallback call to 'us'
        assertEquals(1, response.articles?.size)
        assertEquals("Global Innovation Wave", response.articles?.first()?.title)
    }

    @Test
    fun searchNews_callsCorrectEndpointAndParameters() = runTest {
        val mockEngine = MockEngine { request ->
            assertTrue(request.url.encodedPath.endsWith("/everything"))
            assertEquals("kotlin", request.url.parameters["q"])
            assertEquals("publishedAt", request.url.parameters["sortBy"])
            respond(
                content = ByteReadChannel(sampleSuccessJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = KtorClientFactory.createHttpClient(engine = mockEngine)
        val apiService = KtorNewsApiService(client = httpClient)

        val response = apiService.searchNews(query = "kotlin")

        assertEquals("ok", response.status)
        assertEquals(2, response.articles?.size)
    }

    @Test
    fun articleMapper_filtersOutInvalidOrRemovedArticles() {
        val removedDto = com.samsul.inosoftapps.data.remote.dto.ArticleDto(
            title = "[Removed]",
            url = "https://example.com/removed"
        )
        assertNull(removedDto.toDomain())

        val blankUrlDto = com.samsul.inosoftapps.data.remote.dto.ArticleDto(
            title = "Valid Title",
            url = "   "
        )
        assertNull(blankUrlDto.toDomain())

        val validDto = com.samsul.inosoftapps.data.remote.dto.ArticleDto(
            title = "Valid Article",
            url = "https://example.com/valid",
            description = "Some description",
            publishedAt = "2026-08-27T00:00:00Z"
        )
        val domain = validDto.toDomain("business")
        assertNotNull(domain)
        assertEquals("Valid Article", domain.title)
        assertEquals("business", domain.category)
    }
}
