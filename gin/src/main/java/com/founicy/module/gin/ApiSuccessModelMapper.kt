package com.founicy.module.gin

/**
 *
 * A mapper interface for mapping [ApiResponse.Success] response as a custom [V] instance model.
 *
 * @see [ApiSuccessModelMapper](https://github.com/wzasd/gin#apierrormodelmapper)
 */
public fun interface ApiSuccessModelMapper<T, V> {

  /**
   * maps the [ApiResponse.Success] to the [V] using the mapper.
   *
   * @param apiErrorResponse The [ApiResponse.Success] error response from the network request.
   * @return A custom [V] success response model.
   */
  public fun map(apiErrorResponse: ApiResponse.Success<T>): V
}
