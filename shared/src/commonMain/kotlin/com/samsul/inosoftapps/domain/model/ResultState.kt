package com.samsul.inosoftapps.domain.model

/**
 * Generic wrapper for asynchronous UI / domain states.
 */
sealed interface ResultState<out T> {
    data object Idle : ResultState<Nothing>
    data object Loading : ResultState<Nothing>
    data class Success<out T>(val data: T) : ResultState<T>
    data class Error(val error: DomainError) : ResultState<Nothing>
}

/**
 * Helper extension to execute an action on Success.
 */
inline fun <T> ResultState<T>.onSuccess(action: (value: T) -> Unit): ResultState<T> {
    if (this is ResultState.Success) {
        action(data)
    }
    return this
}

/**
 * Helper extension to execute an action on Error.
 */
inline fun <T> ResultState<T>.onError(action: (error: DomainError) -> Unit): ResultState<T> {
    if (this is ResultState.Error) {
        action(error)
    }
    return this
}

/**
 * Helper extension to map the Success value to another type.
 */
inline fun <T, R> ResultState<T>.map(transform: (value: T) -> R): ResultState<R> {
    return when (this) {
        is ResultState.Idle -> ResultState.Idle
        is ResultState.Loading -> ResultState.Loading
        is ResultState.Success -> ResultState.Success(transform(data))
        is ResultState.Error -> ResultState.Error(error)
    }
}

/**
 * Returns data if Success, otherwise null.
 */
fun <T> ResultState<T>.dataOrNull(): T? = (this as? ResultState.Success)?.data

/**
 * Returns true if the state is Loading.
 */
val <T> ResultState<T>.isLoading: Boolean get() = this is ResultState.Loading

/**
 * Returns true if the state is Success.
 */
val <T> ResultState<T>.isSuccess: Boolean get() = this is ResultState.Success
