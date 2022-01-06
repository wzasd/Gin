/*
 * Designed and developed by 2020 founicy(Jeffrey wang)
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

package com.founicy.module.gin

import com.founicy.module.gin.operators.GinOperator
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import okio.Timeout
import kotlin.coroutines.CoroutineContext

/**
 *
 * DLGinInitializer is a rules and strategies initializer of the network response.
 */
public object GinInitializer {

  /**
   *
   * determines the success code range of network responses.
   *
   * if a network request is successful and the response code is in the [successCodeRange],
   * its response will be a [ApiResponse.Success].
   *
   * if a network request is successful but out of the [successCodeRange] or failure,
   * the response will be a [ApiResponse.Failure.Error].
   * */
  @JvmStatic
  public var successCodeRange: IntRange = 200..299

  /**
   *
   * A global Operator that operates on [ApiResponse]s globally on each response.
   *
   * [com.founicy.module.gin.operators.ApiResponseOperator] which allows you to handle success and error response instead of
   * the [ApiResponse.onSuccess], [ApiResponse.onError], [ApiResponse.onException] transformers.
   * [com.founicy.module.gin.operators.ApiResponseSuspendOperator] can be used for suspension scope.
   *
   * Via setting a [ginOperator], we don't need to set operator for every [ApiResponse].
   */
  @JvmStatic
  public var ginOperator: GinOperator? = null

  /**
   *
   * A [CoroutineContext] for operating the [ginOperator] when it extends
   * the [com.founicy.module.gin.operators.ApiResponseSuspendOperator].
   */
  @JvmSynthetic
  @OptIn(DelicateCoroutinesApi::class)
  public var dlGinOperatorContext: CoroutineContext = Dispatchers.IO + GlobalScope.coroutineContext

  /**
    
   *
   * A global [Timeout] for operating the [com.founicy.module.gin.coroutines.CoroutinesResponseCallAdapterFactory]
   * or [com.founicy.module.gin.coroutines.CoroutinesResponseCallAdapterFactory] when API requests.
   *
   * Returns a timeout that spans the entire call: resolving DNS, connecting, writing the request
   * body, server processing, and reading the response body. If the call requires redirects or
   * retries all must complete within one timeout period.
   */
  @JvmStatic
  public var dlGinTimeout: Timeout? = null
}
