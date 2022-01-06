package com.founicy.module.gin

import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type
import javax.sql.DataSource

/**
 *
 * DataSourceCallAdapter is an call adapter for creating [DataSource] from service method.
 *
 * request API network call asynchronously and returns [DataSource].
 */
public class DataResponseCallAdapter<R> constructor(
    private val responseType: Type
) : CallAdapter<R, DataResponse<R>> {

    override fun responseType(): Type {
        return responseType
    }

    override fun adapt(call: Call<R>): DataResponse<R> {
        val responseDataSource: ResponseDataSource<R> = ResponseDataSource()
        return responseDataSource.combine(call, null)
    }
}