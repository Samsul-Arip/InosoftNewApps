package com.samsul.inosoftapps.data.local

import com.samsul.inosoftapps.data.local.entity.ArticleEntity
import com.samsul.inosoftapps.data.mapper.toDomain
import com.samsul.inosoftapps.data.mapper.toDomainList
import com.samsul.inosoftapps.data.mapper.toEntity
import com.samsul.inosoftapps.data.mapper.toEntityList
import com.samsul.inosoftapps.domain.model.Article
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArticleEntityMapperTest {

    private val sampleEntity = ArticleEntity(
        id = "100",
        title = "Room KMP Offline First",
        description = "Seamless local persistence across Android & iOS",
        content = "Detailed content about Room KMP",
        author = "Android Team",
        url = "https://developer.android.com/room",
        imageUrl = "https://developer.android.com/room.png",
        publishedAt = "2026-08-27T12:00:00Z",
        sourceName = "Android Developers",
        category = "technology",
        isBookmarked = true,
        cachedAt = 1724760000000L
    )

    private val sampleDomain = Article(
        id = "200",
        title = "Clean Architecture with KMP",
        description = "Decoupling business logic from framework details",
        content = "Architecture guide content",
        author = "Mobile Architect",
        url = "https://example.com/clean-arch",
        imageUrl = null,
        publishedAt = "2026-08-27T13:00:00Z",
        sourceName = "Tech Blog",
        category = "business",
        isBookmarked = false
    )

    /**
     * Tests that [ArticleEntity.toDomain] correctly maps all database entity fields to domain model properties,
     * including date formatting into human-readable representation.
     */
    @Test
    fun entityToDomain_mapsAllFieldsCorrectly() {
        val domain = sampleEntity.toDomain()

        assertEquals(sampleEntity.id, domain.id)
        assertEquals(sampleEntity.title, domain.title)
        assertEquals(sampleEntity.description, domain.description)
        assertEquals(sampleEntity.content, domain.content)
        assertEquals(sampleEntity.author, domain.author)
        assertEquals(sampleEntity.url, domain.url)
        assertEquals(sampleEntity.imageUrl, domain.imageUrl)
        assertTrue(domain.publishedAt.contains("27"))
        assertTrue(domain.publishedAt.contains("2026"))
        assertEquals(sampleEntity.sourceName, domain.sourceName)
        assertEquals(sampleEntity.category, domain.category)
        assertTrue(domain.isBookmarked)
    }

    /**
     * Tests that [Article.toEntity] correctly transforms a domain model into a Room database entity
     * with the specified cache timestamp preserved.
     */
    @Test
    fun domainToEntity_mapsAllFieldsCorrectly() {
        val cachedTimestamp = 1724760500000L
        val entity = sampleDomain.toEntity(cachedAt = cachedTimestamp)

        assertEquals(sampleDomain.id, entity.id)
        assertEquals(sampleDomain.title, entity.title)
        assertEquals(sampleDomain.description, entity.description)
        assertEquals(sampleDomain.content, entity.content)
        assertEquals(sampleDomain.author, entity.author)
        assertEquals(sampleDomain.url, entity.url)
        assertEquals(sampleDomain.imageUrl, entity.imageUrl)
        assertEquals(sampleDomain.publishedAt, entity.publishedAt)
        assertEquals(sampleDomain.sourceName, entity.sourceName)
        assertEquals(sampleDomain.category, entity.category)
        assertEquals(sampleDomain.isBookmarked, entity.isBookmarked)
        assertEquals(cachedTimestamp, entity.cachedAt)
    }

    /**
     * Tests list extension mappers [List.toDomainList] and [List.toEntityList]
     * to ensure batch collection transformations map all elements accurately.
     */
    @Test
    fun listMappers_mapAllItemsCorrectly() {
        val entities = listOf(sampleEntity)
        val domains = entities.toDomainList()
        assertEquals(1, domains.size)
        assertEquals(sampleEntity.id, domains[0].id)

        val domainList = listOf(sampleDomain)
        val entityList = domainList.toEntityList()
        assertEquals(1, entityList.size)
        assertEquals(sampleDomain.id, entityList[0].id)
    }
}
