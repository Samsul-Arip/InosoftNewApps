package com.samsul.inosoftapps.domain.model

import com.samsul.inosoftapps.util.AppStrings

/**
 * Exception wrapper for propagating typed [DomainError] inside Kotlin [Result].
 */
class DomainException(
    val error: DomainError,
    override val message: String? = when (error) {
        is DomainError.ServerError -> error.message ?: AppStrings.SERVER_ERROR_DEFAULT
        is DomainError.Unknown -> error.message ?: AppStrings.UNKNOWN_ERROR
        is DomainError.NoInternet -> AppStrings.NO_INTERNET_MESSAGE
        is DomainError.Timeout -> AppStrings.TIMEOUT_MESSAGE
        is DomainError.NotFound -> AppStrings.DATA_NOT_FOUND_ERROR
        is DomainError.EmptyData -> AppStrings.EMPTY_DATA_ERROR
    }
) : Exception(message, (error as? DomainError.Unknown)?.throwable)
