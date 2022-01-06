package com.founicy.demo.dlgin.app.mapper

import com.founicy.module.gin.ApiErrorModelMapper
import com.founicy.module.gin.ApiResponse
import com.founicy.module.gin.message
import com.founicy.demo.dlgin.app.model.ErrorEnvelope

/**
 * A mapper object for mapping [ApiResponse.Failure.Error] response as a custom [ErrorEnvelope] model.
 *
 * @see [ApiErrorModelMapper](https://github.com/wzasd/gin#apierrormodelmapper)
 */
object ErrorEnvelopeMapper : ApiErrorModelMapper<ErrorEnvelope> {

  override fun map(apiErrorResponse: ApiResponse.Failure.Error<*>): ErrorEnvelope {
    return ErrorEnvelope(apiErrorResponse.statusCode.code, apiErrorResponse.message())
  }
}
