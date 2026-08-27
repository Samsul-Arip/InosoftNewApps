package com.samsul.inosoftapps.domain.model

/**
 * Domain-level errors providing distinct categories for network, server,
 * cache, and generic failures.
 */
sealed interface DomainError {
    /** Device is offline or host cannot be resolved */
    data object NoInternet : DomainError

    /** Request or connection timed out */
    data object Timeout : DomainError

    /** HTTP 4xx/5xx or backend returned an error response */
    data class ServerError(
        val code: Int? = null,
        val message: String? = null
    ) : DomainError

    /** Requested entity was not found */
    data object NotFound : DomainError

    /** Data source returned an empty result */
    data object EmptyData : DomainError

    /** Fallback for unexpected exceptions */
    data class Unknown(
        val throwable: Throwable? = null,
        val message: String? = null
    ) : DomainError
}
