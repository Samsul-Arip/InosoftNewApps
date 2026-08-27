package com.samsul.inosoftapps.data.remote

import com.samsul.inosoftapps.data.mapper.toDomainList
import com.samsul.inosoftapps.data.remote.config.ApiConfigProvider
import com.samsul.inosoftapps.util.AppConstants
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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

    @Test
    fun getTopHeadlines_parsesResponseCorrectly() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/v2/top-headlines", request.url.encodedPath)
            assertEquals("us", request.url.parameters["country"])
            assertEquals("1", request.url.parameters["page"])
            assertEquals(AppConstants.DEFAULT_PAGE_SIZE.toString(), request.url.parameters["pageSize"])
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
    fun getTopHeadlines_handlesEmptyResponseCorrectly() = runTest {
        var callCount = 0
        val mockEngine = MockEngine { request ->
            callCount++
            assertEquals("id", request.url.parameters["country"])
            respond(
                content = ByteReadChannel(sampleEmptyJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = KtorClientFactory.createHttpClient(engine = mockEngine)
        val apiService = KtorNewsApiService(client = httpClient)

        val response = apiService.getTopHeadlines(country = "id")

        assertEquals(1, callCount)
        assertEquals(0, response.articles?.size)
        assertEquals(0, response.totalResults)
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
        assertEquals(2, response.totalResults)
    }

    @Test
    fun ktorNewsApiService_usesInjectedConfigProviderDynamically() = runTest {
        val customConfig = object : ApiConfigProvider {
            override val baseUrl: String = "https://custom-news.org/v2"
            override val apiKey: String = "custom_test_key_123"
            override val defaultCountry: String = "id"
        }

        val mockEngine = MockEngine { request ->
            assertEquals("/v2/top-headlines", request.url.encodedPath)
            assertEquals("custom-news.org", request.url.host)
            assertEquals("id", request.url.parameters["country"])
            assertEquals("custom_test_key_123", request.url.parameters["apiKey"])
            respond(
                content = ByteReadChannel(sampleSuccessJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = KtorClientFactory.createHttpClient(engine = mockEngine, configProvider = customConfig)
        val apiService = KtorNewsApiService(client = httpClient, configProvider = customConfig)

        val response = apiService.getTopHeadlines()
        assertEquals("ok", response.status)
    }
}
