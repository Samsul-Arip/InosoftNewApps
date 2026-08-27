package com.samsul.inosoftapps.domain.model

/**
 * Exception wrapper for propagating typed [DomainError] inside Kotlin [Result].
 */
class DomainException(
    val error: DomainError,
    override val message: String? = when (error) {
        is DomainError.ServerError -> error.message
        is DomainError.Unknown -> error.message
        is DomainError.NoInternet -> "No internet connection"
        is DomainError.Timeout -> "Request timed out"
        is DomainError.NotFound -> "Resource not found"
        is DomainError.EmptyData -> "No data available"
    }
) : Exception(message, (error as? DomainError.Unknown)?.throwable)
