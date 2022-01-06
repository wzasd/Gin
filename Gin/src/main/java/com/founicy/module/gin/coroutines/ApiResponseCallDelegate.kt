package com.founicy.module.gin.coroutines

import com.founicy.module.gin.ApiResponse

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author founicy(Jeffrey wang)
 *
 * ApiResponseCallDelegate is a delegate [Call] proxy for handling and transforming normal generic type [T]
 * as [ApiResponse] that wrapping [T] data from the network responses.
 */
internal class ApiResponseCallDelegate<T>(proxy: Call<T>) : CallDelegate<T, ApiResponse<T>>(proxy) {

  override fun enqueueImpl(callback: Callback<ApiResponse<T>>) = proxy.enqueue(
    object : Callback<T> {
      override fun onResponse(call: Call<T>, response: Response<T>) {
        val apiResponse = ApiResponse.of { response }
        callback.onResponse(this@ApiResponseCallDelegate, Response.success(apiResponse))
      }

      override fun onFailure(call: Call<T>, t: Throwable) {
        callback.onResponse(this@ApiResponseCallDelegate, Response.success(ApiResponse.error(t)))
      }
    }
  )

  override fun cloneImpl() = ApiResponseCallDelegate(proxy.clone())
}
