package com.founicy.module.gin.operators

import com.founicy.module.gin.ApiResponse


/**
 *
 * ApiResponseOperator operates on an [ApiResponse] and return an [ApiResponse].
 * This allows you to handle success and error response instead of the [com.founicy.module.gin.onSuccess],
 * [com.founicy.module.gin.onError], [com.founicy.module.gin.onException] transformers.
 * This operator can be applied globally as a singleton instance, or on each [ApiResponse] one by one.
 */
public abstract class ApiResponseOperator<T> : GinOperator {

  /**
   * Operates the [ApiResponse.Success] for handling successful responses if the request succeeds.
   *
   * @param apiResponse The successful response.
   */
  public abstract fun onSuccess(apiResponse: ApiResponse.Success<T>)

  /**
   * Operates the [ApiResponse.Failure.Error] for handling error responses if the request failed.
   *
   * @param apiResponse The failed response.
   */
  public abstract fun onError(apiResponse: ApiResponse.Failure.Error<T>)

  /**
   * Operates the [ApiResponse.Failure.Exception] for handling exception responses if the request get an exception.
   *
   * @param apiResponse The exception response.
   */
  public abstract fun onException(apiResponse: ApiResponse.Failure.Exception<T>)
}
