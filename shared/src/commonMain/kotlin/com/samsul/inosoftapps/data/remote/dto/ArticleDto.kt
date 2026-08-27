package com.samsul.inosoftapps.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleDto(
    @SerialName("source")
    val source: SourceDto? = null,
    @SerialName("author")
    val author: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("url")
    val url: String? = null,
    @SerialName("urlToImage")
    val urlToImage: String? = null,
    @SerialName("publishedAt")
    val publishedAt: String? = null,
    @SerialName("content")
    val content: String? = null
)

@Serializable
data class SourceDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String? = null
)
