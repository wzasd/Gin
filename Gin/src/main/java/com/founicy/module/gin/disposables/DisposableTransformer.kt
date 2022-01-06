@file:Suppress("unused")
@file:JvmName("DisposableTransformer")
@file:JvmMultifileClass

package com.founicy.module.gin.disposables

import retrofit2.Call

/**
 * @author founicy(Jeffrey wang)
 *
 * Returns an instance of [Disposable] from a [Call].
 */
public fun <T> Call<T>.disposable(): Disposable {
  val call = this
  return object : Disposable {
    override fun dispose() {
      if (call.isExecuted && !isDisposed()) {
        call.cancel()
      }
    }

    override fun isDisposed() = call.isCanceled
  }
}

/**
 * @author founicy(Jeffrey wang)
 *
 * Joins onto [CompositeDisposable] as a disposable. must be called before [request].
 */
public fun <T> Call<T>.joinDisposable(compositeDisposable: CompositeDisposable): Call<T> = apply {
  compositeDisposable.add(disposable())
}
