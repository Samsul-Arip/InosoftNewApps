package com.samsul.inosoftapps.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponseDto(
    @SerialName("status")
    val status: String,
    @SerialName("totalResults")
    val totalResults: Int? = null,
    @SerialName("articles")
    val articles: List<ArticleDto>? = null,
    @SerialName("code")
    val code: String? = null,
    @SerialName("message")
    val message: String? = null
)
