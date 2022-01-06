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

package com.founicy.module.gin.coroutines

import com.founicy.module.gin.ApiResponse
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

/**
 * @author founicy(Jeffrey wang)
 *
 * CoroutinesResponseCallAdapter is an coroutines call adapter for creating [ApiResponse] from service method.
 *
 * request API network call asynchronously and returns [ApiResponse].
 */
public class CoroutinesResponseCallAdapter constructor(
  private val resultType: Type
) : CallAdapter<Type, Call<ApiResponse<Type>>> {

  override fun responseType(): Type {
    return resultType
  }

  override fun adapt(call: Call<Type>): Call<ApiResponse<Type>> {
    return ApiResponseCallDelegate(call)
  }
}
