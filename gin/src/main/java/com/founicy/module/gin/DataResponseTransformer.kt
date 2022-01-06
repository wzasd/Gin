@file:JvmName("DataSourceTransformer")
@file:JvmMultifileClass

package com.founicy.module.gin

/**
 *
 * Changes an instance of the [DataResponse] interface to the [ResponseDataSource].
 */
public fun <T> DataResponse<T>.toResponseDataSource(): ResponseDataSource<T> {
    requireNotNull(this is ResponseDataSource)
    return this as ResponseDataSource<T>
}
