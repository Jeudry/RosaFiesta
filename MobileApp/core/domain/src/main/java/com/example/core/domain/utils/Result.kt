package com.example.core.domain.utils

import com.example.core.domain.utils.Error as ErrorInterface

sealed interface Result<out D, out E : ErrorInterface> {
  data class Success<out D>(val data: D) : Result<D, Nothing>
  data class Error<out E : ErrorInterface>(val error: E) : Result<Nothing, E>
}

inline fun <T, E : ErrorInterface, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
  return when (this) {
    is Result.Success -> Result.Success(map(data))
    is Result.Error -> Result.Error(error)
  }
}

fun <T, E : ErrorInterface> Result<T, E>.asEmptyDataResult(): EmptyResult<E> {
  return map { }
}

inline fun <D, E : ErrorInterface> Result<D, E>.onSuccess(action: (D) -> Unit): Result<D, E> {
  if (this is Result.Success) {
    action(data)
  }
  return this
}

inline fun <D, E : ErrorInterface> Result<D, E>.onError(action: (E) -> Unit): Result<D, E> {
  if (this is Result.Error) {
    action(error)
  }
  return this
}

typealias EmptyResult<E> = Result<Unit, E>