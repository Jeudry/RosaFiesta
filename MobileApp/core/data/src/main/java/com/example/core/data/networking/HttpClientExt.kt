package com.example.core.data.networking

import com.example.core.data.BuildConfig
import com.example.core.domain.utils.DataError
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.network.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import com.example.core.domain.utils.Result as ResultResponse

suspend inline fun <reified Response : Any> HttpClient.get(
  route: String,
  queryParameters: Map<String, Any?> = mapOf()
): ResultResponse<Response, DataError.Network> {
  return safeCall {
    get {
      url(constructRoute(route))
      queryParameters.forEach { (key, value) ->
        parameter(key, value)
      }
    }
  }
}

suspend inline fun <reified Response : Any> HttpClient.delete(
  route: String,
  queryParameters: Map<String, Any?> = mapOf()
): ResultResponse<Response, DataError.Network> {
  return safeCall {
    delete {
      url(constructRoute(route))
      queryParameters.forEach { (key, value) ->
        parameter(key, value)
      }
    }
  }
}

suspend inline fun <reified Request, reified Response : Any> HttpClient.post(
  route: String,
  body: Request
): ResultResponse<Response, DataError.Network> {
  return safeCall {
    post {
      url(constructRoute(route))
      setBody(body)
    }
  }
}

suspend inline fun <reified Response> safeCall(execute: () -> HttpResponse): ResultResponse<Response, DataError.Network> {
  val response = try {
    execute()
  } catch (e: UnresolvedAddressException) {
    e.printStackTrace()
    return ResultResponse.Error(DataError.Network.NO_INTERNET)
  } catch (e: SerializationException) {
    e.printStackTrace()
    return ResultResponse.Error(DataError.Network.SERIALIZATION)
  } catch (e: Exception) {
    if (e is CancellationException) throw e
    e.printStackTrace()
    return ResultResponse.Error(DataError.Network.UNKNOWN)
  }

  return responseToResult(response)
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): ResultResponse<T, DataError.Network> {
  return when (response.status.value) {
    in 200..299 -> ResultResponse.Success(response.body<T>())
    401 -> ResultResponse.Error(DataError.Network.UNAUTHORIZED)
    408 -> ResultResponse.Error(DataError.Network.REQUEST_TIMEOUT)
    409 -> ResultResponse.Error(DataError.Network.CONFLICT)
    413 -> ResultResponse.Error(DataError.Network.PAYLOAD_TOO_LARGE)
    429 -> ResultResponse.Error(DataError.Network.TOO_MANY_REQUESTS)
    in 500..599 -> ResultResponse.Error(DataError.Network.SERVER_ERROR)
    else -> ResultResponse.Error(DataError.Network.UNKNOWN)
  }
}

fun constructRoute(route: String): String {
  return when {
    route.contains(BuildConfig.BASE_URL) -> route
    route.startsWith("/") -> BuildConfig.BASE_URL + route
    else -> BuildConfig.BASE_URL + "\"$route\""
  }
}