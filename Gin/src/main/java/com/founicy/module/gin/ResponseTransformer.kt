@file:Suppress("unused")
@file:JvmName("ResponseTransformer")
@file:JvmMultifileClass

package com.founicy.module.gin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.founicy.module.gin.coroutines.SuspensionFunction
import com.founicy.module.gin.operators.ApiResponseOperator
import com.founicy.module.gin.operators.ApiResponseSuspendOperator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import okhttp3.Headers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 *
 * Requests asynchronously and executes the lambda that receives [ApiResponse] as a result.
 *
 * @param onResult An lambda that receives [ApiResponse] as a result.
 *
 * @return The original [Call].
 */
@JvmSynthetic
public inline fun <T> Call<T>.request(
  crossinline onResult: (response: ApiResponse<T>) -> Unit
): Call<T> = apply {
  enqueue(getCallbackFromOnResult(onResult))
}

/**
 
 *
 * Combines a [DataResponse] to the call for processing response data more handy.
 */
@JvmSynthetic
public inline fun <T> Call<T>.combineDataSource(
  dataSource: DataResponse<T>,
  crossinline onResult: (response: ApiResponse<T>) -> Unit
): DataResponse<T> = dataSource.combine(this, getCallbackFromOnResult(onResult))

/**
 
 *
 * Combines a [DataResponse] to the call for processing response data more handy.
 */
@JvmSynthetic
@SuspensionFunction
public inline fun <T> Call<T>.suspendCombineDataSource(
  dataSource: DataResponse<T>,
  coroutineScope: CoroutineScope,
  crossinline onResult: suspend (response: ApiResponse<T>) -> Unit
): DataResponse<T> = dataSource.combine(this, getCallbackFromOnResultOnCoroutinesScope(coroutineScope, onResult))

/**
 
 *
 * Combines a [DataResponse] to the call for processing response data more handy.
 */
@JvmSynthetic
@SuspensionFunction
public inline fun <T> Call<T>.suspendCombineDataSource(
  dataSource: DataResponse<T>,
  context: CoroutineContext = EmptyCoroutineContext,
  crossinline onResult: suspend (response: ApiResponse<T>) -> Unit
): DataResponse<T> = dataSource.combine(this, getCallbackFromOnResultWithContext(context, onResult))

/**
 
 *
 * Returns a response callback from an onResult lambda.
 *
 * @param onResult A lambda that would be executed when the request finished.
 *
 * @return A [Callback] will be executed.
 */
@PublishedApi
@JvmSynthetic
internal inline fun <T> getCallbackFromOnResult(
  crossinline onResult: (response: ApiResponse<T>) -> Unit
): Callback<T> {
  return object : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
      onResult(ApiResponse.of { response })
    }

    override fun onFailure(call: Call<T>, throwable: Throwable) {
      onResult(ApiResponse.error(throwable))
    }
  }
}

/**
 
 *
 * Returns a response callback from an onResult lambda.
 *
 * @param onResult A lambda that would be executed when the request finished.
 *
 * @return A [Callback] will be executed.
 */
@PublishedApi
@JvmSynthetic
internal inline fun <T> getCallbackFromOnResultOnCoroutinesScope(
  coroutineScope: CoroutineScope,
  crossinline onResult: suspend (response: ApiResponse<T>) -> Unit
): Callback<T> {
  return object : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
      coroutineScope.launch {
        onResult(ApiResponse.of { response })
      }
    }

    override fun onFailure(call: Call<T>, throwable: Throwable) {
      coroutineScope.launch {
        onResult(ApiResponse.error(throwable))
      }
    }
  }
}

/**
 
 *
 * Returns a response callback from an onResult lambda.
 *
 * @param onResult A lambda that would be executed when the request finished.
 *
 * @return A [Callback] will be executed.
 */
@PublishedApi
@JvmSynthetic
internal inline fun <T> getCallbackFromOnResultWithContext(
  context: CoroutineContext = EmptyCoroutineContext,
  crossinline onResult: suspend (response: ApiResponse<T>) -> Unit
): Callback<T> {
  return object : Callback<T> {
    val supervisorJob = SupervisorJob(context[Job])
    val scope = CoroutineScope(context + supervisorJob)
    override fun onResponse(call: Call<T>, response: Response<T>) {
      scope.launch {
        onResult(ApiResponse.of { response })
      }
    }

    override fun onFailure(call: Call<T>, throwable: Throwable) {
      scope.launch {
        onResult(ApiResponse.error(throwable))
      }
    }
  }
}

/**
 
 *
 * Returns the encapsulated data if this instance represents [ApiResponse.Success] or
 * returns null if it is [ApiResponse.Failure.Error] or [ApiResponse.Failure.Exception].
 *
 * @return The encapsulated data or null.
 */
public fun <T> ApiResponse<T>.getOrNull(): T? {
  return when (this) {
    is ApiResponse.Success -> data
    is ApiResponse.Failure.Error -> null
    is ApiResponse.Failure.Exception -> null
  }
}

/**
 
 *
 * Returns the encapsulated data if this instance represents [ApiResponse.Success] or
 * returns the [defaultValue] if it is [ApiResponse.Failure.Error] or [ApiResponse.Failure.Exception].
 *
 * @return The encapsulated data or [defaultValue].
 */
public fun <T> ApiResponse<T>.getOrElse(defaultValue: T): T {
  return when (this) {
    is ApiResponse.Success -> data
    is ApiResponse.Failure.Error -> defaultValue
    is ApiResponse.Failure.Exception -> defaultValue
  }
}

/**
 
 *
 * Returns the encapsulated data if this instance represents [ApiResponse.Success] or
 * invokes the lambda [defaultValue] that returns [T] if it is [ApiResponse.Failure.Error] or [ApiResponse.Failure.Exception].
 *
 * @return The encapsulated data or [defaultValue].
 */
public inline fun <T> ApiResponse<T>.getOrElse(defaultValue: () -> T): T {
  return when (this) {
    is ApiResponse.Success -> data
    is ApiResponse.Failure.Error -> defaultValue()
    is ApiResponse.Failure.Exception -> defaultValue()
  }
}

/**
 
 *
 * Returns the encapsulated data if this instance represents [ApiResponse.Success] or
 * throws the encapsulated Throwable exception if it is [ApiResponse.Failure.Error] or [ApiResponse.Failure.Exception].
 *
 * @throws RuntimeException if it is [ApiResponse.Failure.Error] or
 * the encapsulated Throwable exception if it is [ApiResponse.Failure.Exception.exception]
 *
 * @return The encapsulated data.
 */
public fun <T> ApiResponse<T>.getOrThrow(): T {
  when (this) {
    is ApiResponse.Success -> return data
    is ApiResponse.Failure.Error -> throw RuntimeException(message())
    is ApiResponse.Failure.Exception -> throw exception
  }
}

/**
 
 *
 * A scope function that would be executed for handling successful responses if the request succeeds.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Success] if the request succeeds.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
public inline fun <T> ApiResponse<T>.onSuccess(
  crossinline onResult: ApiResponse.Success<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Success) {
    onResult(this)
  }
  return this
}

/**
 
 *
 * A scope function that would be executed for handling successful responses if the request succeeds with a [ApiSuccessModelMapper].
 *
 * @param mapper The [ApiSuccessModelMapper] for mapping [ApiResponse.Success] response as a custom [V] instance model.
 * @param onResult The receiver function that receiving [ApiResponse.Success] if the request succeeds.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
public inline fun <T, V> ApiResponse<T>.onSuccess(
  mapper: ApiSuccessModelMapper<T, V>,
  crossinline onResult: V.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Success) {
    onResult(map(mapper))
  }
  return this
}

/**
 
 *
 * A suspension scope function that would be executed for handling successful responses if the request succeeds.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Success] if the request succeeds.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T> ApiResponse<T>.suspendOnSuccess(
  crossinline onResult: suspend ApiResponse.Success<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Success) {
    onResult(this)
  }
  return this
}

/**
 
 *
 * A suspension scope function that would be executed for handling successful responses if the request succeeds with a [ApiSuccessModelMapper].
 *
 * @param mapper The [ApiSuccessModelMapper] for mapping [ApiResponse.Success] response as a custom [V] instance model.
 * @param onResult The receiver function that receiving [ApiResponse.Success] if the request succeeds.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T, V> ApiResponse<T>.suspendOnSuccess(
  mapper: ApiSuccessModelMapper<T, V>,
  crossinline onResult: suspend V.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Success) {
    onResult(map(mapper))
  }
  return this
}

/**
 
 *
 * A function that would be executed for handling error responses if the request failed or get an exception.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure] if the request failed or get an exception.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
public inline fun <T> ApiResponse<T>.onFailure(
  crossinline onResult: ApiResponse.Failure<*>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure<*>) {
    onResult(this)
  }
  return this
}

/**
 
 *
 * A suspension function that would be executed for handling error responses if the request failed or get an exception.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure] if the request failed or get an exception.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T> ApiResponse<T>.suspendOnFailure(
  crossinline onResult: suspend ApiResponse.Failure<*>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure<*>) {
    onResult(this)
  }
  return this
}

/**
 
 *
 * A scope function that would be executed for handling error responses if the request failed.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure.Exception] if the request failed.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
public inline fun <T> ApiResponse<T>.onError(
  crossinline onResult: ApiResponse.Failure.Error<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure.Error) {
    onResult(this)
  }
  return this
}

/**
 
 *
 * A scope function that would be executed for handling error responses if the request failed with a [ApiErrorModelMapper].
 * This function receives a [ApiErrorModelMapper] and returns the mapped result into the scope.
 *
 * @param mapper The [ApiErrorModelMapper] for mapping [ApiResponse.Failure.Error] response as a custom [V] instance model.
 * @param onResult The receiver function that receiving [ApiResponse.Failure.Exception] if the request failed.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
public inline fun <T, V> ApiResponse<T>.onError(
  mapper: ApiErrorModelMapper<V>,
  crossinline onResult: V.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure.Error) {
    onResult(map(mapper))
  }
  return this
}

/**
 
 *
 * A suspension scope function that would be executed for handling error responses if the request failed.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure.Exception] if the request failed.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T> ApiResponse<T>.suspendOnError(
  crossinline onResult: suspend ApiResponse.Failure.Error<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure.Error) {
    onResult(this)
  }
  return this
}

/**
 
 *
 * A suspension scope function that would be executed for handling error responses if the request failed with a [ApiErrorModelMapper].
 * This function receives a [ApiErrorModelMapper] and returns the mapped result into the scope.
 *
 * @param mapper The [ApiErrorModelMapper] for mapping [ApiResponse.Failure.Error] response as a custom [V] instance model.
 * @param onResult The receiver function that receiving [ApiResponse.Failure.Exception] if the request failed.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T, V> ApiResponse<T>.suspendOnError(
  mapper: ApiErrorModelMapper<V>,
  crossinline onResult: suspend V.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure.Error) {
    onResult(map(mapper))
  }
  return this
}

/**
 
 *
 * A scope function that would be executed for handling exception responses if the request get an exception.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure.Exception] if the request get an exception.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
public inline fun <T> ApiResponse<T>.onException(
  crossinline onResult: ApiResponse.Failure.Exception<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure.Exception) {
    onResult(this)
  }
  return this
}

/**
 
 *
 * A suspension scope function that would be executed for handling exception responses if the request get an exception.
 *
 * @param onResult The receiver function that receiving [ApiResponse.Failure.Exception] if the request get an exception.
 *
 * @return The original [ApiResponse].
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T> ApiResponse<T>.suspendOnException(
  crossinline onResult: suspend ApiResponse.Failure.Exception<T>.() -> Unit
): ApiResponse<T> {
  if (this is ApiResponse.Failure.Exception) {
    onResult(this)
  }
  return this
}

/**
 
 *
 * A scope function that will be executed for handling successful, error, exception responses.
 *  This function receives and handles [ApiResponse.onSuccess], [ApiResponse.onError],
 *  and [ApiResponse.onException] in one scope.
 *
 * @param onSuccess A scope function that would be executed for handling successful responses if the request succeeds.
 * @param onError A scope function that would be executed for handling error responses if the request failed.
 * @param onException A scope function that would be executed for handling exception responses if the request get an exception.
 *
 *  @return The original [ApiResponse].
 */
@JvmSynthetic
public inline fun <T> ApiResponse<T>.onProcedure(
  crossinline onSuccess: ApiResponse.Success<T>.() -> Unit,
  crossinline onError: ApiResponse.Failure.Error<T>.() -> Unit,
  crossinline onException: ApiResponse.Failure.Exception<T>.() -> Unit
): ApiResponse<T> = apply {
  this.onSuccess(onSuccess)
  this.onError(onError)
  this.onException(onException)
}

/**
 
 *
 * A suspension scope function that will be executed for handling successful, error, exception responses.
 *  This function receives and handles [ApiResponse.onSuccess], [ApiResponse.onError],
 *  and [ApiResponse.onException] in one scope.
 *
 * @param onSuccess A suspension scope function that would be executed for handling successful responses if the request succeeds.
 * @param onError A suspension scope function that would be executed for handling error responses if the request failed.
 * @param onException A suspension scope function that would be executed for handling exception responses if the request get an exception.
 *
 *  @return The original [ApiResponse].
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T> ApiResponse<T>.suspendOnProcedure(
  crossinline onSuccess: suspend ApiResponse.Success<T>.() -> Unit,
  crossinline onError: suspend ApiResponse.Failure.Error<T>.() -> Unit,
  crossinline onException: suspend ApiResponse.Failure.Exception<T>.() -> Unit
): ApiResponse<T> = apply {
  this.suspendOnSuccess(onSuccess)
  this.suspendOnError(onError)
  this.suspendOnException(onException)
}

/**
 
 *
 * Maps [ApiResponse.Success] to a customized success response model.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Success] response as a custom [V] instance model.
 *
 * @return A mapped custom [V] error response model.
 */
public fun <T, V> ApiResponse.Success<T>.map(mapper: ApiSuccessModelMapper<T, V>): V {
  return mapper.map(this)
}

/**
 
 *
 * Maps [ApiResponse.Success] to a customized success response model.
 *
 * @param mapper An executable lambda for mapping [ApiResponse.Success] response as a custom [V] instance model.
 *
 * @return A mapped custom [V] error response model.
 */
public fun <T, V> ApiResponse.Success<T>.map(mapper: (ApiResponse.Success<T>) -> V): V {
  return mapper(this)
}

/**
 
 *
 * Maps [ApiResponse.Success] to a customized error response model with a receiver scope lambda.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Success] response as a custom [V] instance model.
 * @param onResult A receiver scope lambda of the mapped custom [V] success response model.
 *
 * @return A mapped custom [V] success response model.
 */
@JvmSynthetic
public inline fun <T, V> ApiResponse.Success<T>.map(
  mapper: ApiSuccessModelMapper<T, V>,
  crossinline onResult: V.() -> Unit
) {
  onResult(mapper.map(this))
}

/**
 
 *
 * Maps [ApiResponse.Success] to a customized error response model with a suspension receiver scope lambda.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Success] response as a custom [V] instance model.
 * @param onResult A suspension receiver scope lambda of the mapped custom [V] success response model.
 *
 * @return A mapped custom [V] success response model.
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T, V> ApiResponse.Success<T>.suspendMap(
  mapper: ApiSuccessModelMapper<T, V>,
  crossinline onResult: suspend V.() -> Unit
) {
  onResult(mapper.map(this))
}

/**
 
 *
 * Maps [ApiResponse.Success] to a customized error response model with a suspension receiver scope lambda.
 *
 * @param mapper An executable lambda for mapping [ApiResponse.Success] response as a custom [V] instance model.
 *
 * @return A mapped custom [V] success response model.
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T, V> ApiResponse.Success<T>.suspendMap(
  crossinline mapper: suspend (ApiResponse.Success<T>) -> V
): V {
  return mapper(this)
}

/**
 
 *
 * Maps [ApiResponse.Failure.Error] to a customized error response model.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Failure.Error] response as a custom [V] instance model.
 *
 * @return A mapped custom [V] error response model.
 */
public fun <T, V> ApiResponse.Failure.Error<T>.map(mapper: ApiErrorModelMapper<V>): V {
  return mapper.map(this)
}

/**
 
 *
 * Maps [ApiResponse.Failure.Error] to a customized error response model.
 *
 * @param mapper An executable lambda for mapping [ApiResponse.Failure.Error] response as a custom [V] instance model.
 *
 * @return A mapped custom [V] error response model.
 */
public fun <T, V> ApiResponse.Failure.Error<T>.map(mapper: (ApiResponse.Failure.Error<T>) -> V): V {
  return mapper(this)
}

/**
 
 *
 * Maps [ApiResponse.Failure.Error] to a customized error response model with a receiver scope lambda.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Failure.Error] response as a custom [V] instance model.
 * @param onResult A receiver scope lambda of the mapped custom [V] error response model.
 *
 * @return A mapped custom [V] error response model.
 */
@JvmSynthetic
public inline fun <T, V> ApiResponse.Failure.Error<T>.map(
  mapper: ApiErrorModelMapper<V>,
  crossinline onResult: V.() -> Unit
) {
  onResult(mapper.map(this))
}

/**
 
 *
 * Maps [ApiResponse.Failure.Error] to a customized error response model with a suspension receiver scope lambda.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Failure.Error] response as a custom [V] instance model.
 * @param onResult A suspension receiver scope lambda of the mapped custom [V] error response model.
 *
 * @return A mapped custom [V] error response model.
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T, V> ApiResponse.Failure.Error<T>.suspendMap(
  mapper: ApiErrorModelMapper<V>,
  crossinline onResult: suspend V.() -> Unit
) {
  onResult(mapper.map(this))
}

/**
 
 *
 * Maps [ApiResponse.Failure.Error] to a customized error response model with a suspension receiver scope lambda.
 *
 * @param mapper A mapper interface for mapping [ApiResponse.Failure.Error] response as a custom [V] instance model.
 *
 * @return A mapped custom [V] error response model.
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T, V> ApiResponse.Failure.Error<T>.suspendMap(
  crossinline mapper: suspend (ApiResponse.Failure.Error<T>) -> V
): V {
  return mapper(this)
}

/**
 
 *
 * Merges multiple [ApiResponse]s as one [ApiResponse] depending on the policy, [ApiResponseMergePolicy].
 * The default policy is [ApiResponseMergePolicy.IGNORE_FAILURE].
 *
 * @param responses Responses for merging as one [ApiResponse].
 * @param mergePolicy A policy for merging response data depend on the success or not.
 *
 * @return [ApiResponse] that depends on the [ApiResponseMergePolicy].
 */
@JvmSynthetic
public fun <T> ApiResponse<List<T>>.merge(
  vararg responses: ApiResponse<List<T>>,
  mergePolicy: ApiResponseMergePolicy = ApiResponseMergePolicy.IGNORE_FAILURE
): ApiResponse<List<T>> {
  val apiResponses = responses.toMutableList()
  apiResponses.add(0, this)

  var apiResponse: ApiResponse.Success<List<T>> =
    ApiResponse.Success(Response.success(mutableListOf(), Headers.headersOf()))

  val data: MutableList<T> = mutableListOf()

  for (response in apiResponses) {
    if (response is ApiResponse.Success) {
      data.addAll(response.data)
      apiResponse = ApiResponse.Success(
        Response.success(data, response.headers)
      )
    } else if (mergePolicy === ApiResponseMergePolicy.PREFERRED_FAILURE) {
      return response
    }
  }

  return apiResponse
}

/**
 
 *
 * Returns an error message from the [ApiResponse.Failure.Error] that consists of the status and error response.
 *
 * @return An error message from the [ApiResponse.Failure.Error].
 */
public fun <T> ApiResponse.Failure.Error<T>.message(): String = toString()

/**
 * Returns an error message from the [ApiResponse.Failure.Exception] that consists of the localized message.
 *
 * @return An error message from the [ApiResponse.Failure.Exception].
 */
public fun <T> ApiResponse.Failure.Exception<T>.message(): String = toString()

/**
 
 *
 * Operates on an [ApiResponse] and return an [ApiResponse].
 * This allows you to handle success and error response instead of the [ApiResponse.onSuccess],
 * [ApiResponse.onError], [ApiResponse.onException] transformers.
 */
@JvmSynthetic
public fun <T, V : ApiResponseOperator<T>> ApiResponse<T>.operator(
  apiResponseOperator: V
): ApiResponse<T> = apply {
  when (this) {
    is ApiResponse.Success -> apiResponseOperator.onSuccess(this)
    is ApiResponse.Failure.Error -> apiResponseOperator.onError(this)
    is ApiResponse.Failure.Exception -> apiResponseOperator.onException(this)
  }
}

/**
 
 *
 * Operates on an [ApiResponse] and return an [ApiResponse] which should be handled in the suspension scope.
 * This allows you to handle success and error response instead of the [ApiResponse.suspendOnSuccess],
 * [ApiResponse.suspendOnError], [ApiResponse.suspendOnException] transformers.
 */
@JvmSynthetic
@SuspensionFunction
public suspend fun <T, V : ApiResponseSuspendOperator<T>> ApiResponse<T>.suspendOperator(
  apiResponseOperator: V
): ApiResponse<T> = apply {
  when (this) {
    is ApiResponse.Success -> apiResponseOperator.onSuccess(this)
    is ApiResponse.Failure.Error -> apiResponseOperator.onError(this)
    is ApiResponse.Failure.Exception -> apiResponseOperator.onException(this)
  }
}

/**
 
 *
 * Returns a [LiveData] which contains successful data if the response is a [ApiResponse.Success].
 *
 * @return An observable [LiveData] which contains successful data.
 */
public fun <T> ApiResponse<T>.toLiveData(): LiveData<T> {
  val liveData = MutableLiveData<T>()
  if (this is ApiResponse.Success) {
    liveData.postValue(data)
  }
  return liveData
}

/**
 
 *
 * Returns a [LiveData] which contains transformed data using successful data if the response is a [ApiResponse.Success].
 *
 * @param transformer A transformer lambda receives successful data and returns anything.
 *
 * @return An observable [LiveData] which contains successful data.
 */
@JvmSynthetic
public inline fun <T, R> ApiResponse<T>.toLiveData(
  crossinline transformer: T.() -> R
): LiveData<R> {
  val liveData = MutableLiveData<R>()
  if (this is ApiResponse.Success) {
    liveData.postValue(data.transformer())
  }
  return liveData
}

/**
 
 *
 * Returns a [LiveData] which contains transformed data using successful data if the response is a [ApiResponse.Success].
 *
 * @param transformer A suspension transformer lambda receives successful data and returns anything.
 *
 * @return An observable [LiveData] which contains successful data.
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T, R> ApiResponse<T>.toSuspendLiveData(
  crossinline transformer: suspend T.() -> R
): LiveData<R> {
  val liveData = MutableLiveData<R>()
  if (this is ApiResponse.Success) {
    liveData.postValue(data.transformer())
  }
  return liveData
}

/**
 
 *
 * Returns a [Flow] which emits successful data if the response is a [ApiResponse.Success] and the data is not null.
 *
 * @return A coroutines [Flow] which emits successful data.
 */
@JvmSynthetic
public fun <T> ApiResponse<T>.toFlow(): Flow<T> {
  return if (this is ApiResponse.Success) {
    flowOf(data)
  } else {
    emptyFlow()
  }
}

/**
 
 *
 * Returns a [Flow] which contains transformed data using successful data
 * if the response is a [ApiResponse.Success] and the data is not null.
 *
 * @param transformer A transformer lambda receives successful data and returns anything.
 *
 * @return A coroutines [Flow] which emits successful data.
 */
@JvmSynthetic
public inline fun <T, R> ApiResponse<T>.toFlow(
  crossinline transformer: T.() -> R
): Flow<R> {
  return if (this is ApiResponse.Success) {
    flowOf(data.transformer())
  } else {
    emptyFlow()
  }
}

/**
 
 *
 * Returns a [Flow] which contains transformed data using successful data
 * if the response is a [ApiResponse.Success] and the data is not null.
 *
 * @param transformer A suspension transformer lambda receives successful data and returns anything.
 *
 * @return A coroutines [Flow] which emits successful data.
 */
@JvmSynthetic
@SuspensionFunction
public suspend inline fun <T, R> ApiResponse<T>.toSuspendFlow(
  crossinline transformer: suspend T.() -> R
): Flow<R> {
  return if (this is ApiResponse.Success) {
    flowOf(data.transformer())
  } else {
    emptyFlow()
  }
}
