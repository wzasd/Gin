package com.founicy.module.gin.interceptors

import com.founicy.module.gin.StatusCode
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Related: https://github.com/square/retrofit/issues/2867
 *
 * An interceptor for bypassing the [com.founicy.module.gin.exceptions.NoContentException]
 * when the server has successfully fulfilled the request with the 2xx code
 * and that there is no additional content to send in the response payload body.
 * e.g., 204 (NoContent), 205 (ResetContent).
 */
public object EmptyBodyInterceptor : Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())
    if (!response.isSuccessful || response.code.let {
      it != StatusCode.NoContent.code && it != StatusCode.ResetContent.code
    }
    ) {
      return response
    }

    if ((response.body?.contentLength()?.takeIf { it >= 0 } != null)) {
      return response.newBuilder().code(200).build()
    }

    val emptyBody = "".toResponseBody("text/plain".toMediaType())

    return response
      .newBuilder()
      .code(200)
      .body(emptyBody)
      .build()
  }
}
