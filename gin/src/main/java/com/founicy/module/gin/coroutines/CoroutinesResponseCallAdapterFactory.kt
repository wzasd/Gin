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

@file:Suppress("unused")

package com.founicy.module.gin.coroutines

import com.founicy.module.gin.ApiResponse
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @author founicy(Jeffrey wang)
 *
 * CoroutinesResponseCallAdapterFactory is an coroutines call adapter factory for creating [ApiResponse].
 *
 * Adding this class to [Retrofit] allows you to return on [ApiResponse] from service method.
 *
 * ```
 * @GET("DisneyPosters.json")
 * suspend fun fetchDisneyPosterList(): ApiResponse<List<Poster>>
 * ```
 */
public class CoroutinesResponseCallAdapterFactory private constructor() : CallAdapter.Factory() {

  override fun get(
    returnType: Type,
    annotations: Array<Annotation>,
    retrofit: Retrofit
  ): CoroutinesResponseCallAdapter? = when (getRawType(returnType)) {
    Call::class.java -> {
      val callType = getParameterUpperBound(0, returnType as ParameterizedType)
      when (getRawType(callType)) {
        ApiResponse::class.java -> {
          val resultType = getParameterUpperBound(0, callType as ParameterizedType)
          CoroutinesResponseCallAdapter(resultType)
        }
        else -> null
      }
    }
    else -> null
  }

  public companion object {
    @JvmStatic
    public fun create(): CoroutinesResponseCallAdapterFactory = CoroutinesResponseCallAdapterFactory()
  }
}
