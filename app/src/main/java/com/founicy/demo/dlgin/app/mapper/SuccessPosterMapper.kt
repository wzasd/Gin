package com.founicy.demo.dlgin.app.mapper

import com.founicy.module.gin.ApiResponse
import com.founicy.module.gin.ApiSuccessModelMapper
import com.founicy.demo.dlgin.app.model.Poster

/**
 * A mapper object for mapping [ApiResponse.Success] response data as [Poster] model.
 *
 * @see [ApiSuccessModelMapper](https://github.com/wzasd/gin#apierrormodelmapper)
 */
object SuccessPosterMapper : ApiSuccessModelMapper<List<Poster>, Poster?> {

  override fun map(apiErrorResponse: ApiResponse.Success<List<Poster>>): Poster? {
    return apiErrorResponse.data.first()
  }
}
