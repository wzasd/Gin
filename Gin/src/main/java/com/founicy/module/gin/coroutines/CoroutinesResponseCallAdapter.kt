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
