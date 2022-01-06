package com.founicy.module.gin

/**
 *
 * A callback interface for observing [ApiResponse] value updating.
 */
public fun interface ResponseObserver<T> {

  /** observes a new [ApiResponse] value. */
  public fun observe(response: ApiResponse<T>)
}
