/*
 * Designed and developed by 2020 wzasd (Jeffrey wang)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.founicy.demo.dlgin.app.mapper

import com.founicy.demo.dlgin.app.model.ErrorEnvelope
import com.founicy.module.gin.ApiErrorModelMapper
import com.founicy.module.gin.ApiResponse
import com.founicy.module.gin.message

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
