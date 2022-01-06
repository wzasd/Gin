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

package com.founicy.module.gin

import com.founicy.module.gin.disposables.CompositeDisposable
import retrofit2.Call
import retrofit2.Callback

/**
 *
 * An abstract interface design for data sources.
 */
public interface DataResponse<T> {

  /** combine a call and a callback to the DataSource. */
  public fun combine(call: Call<T>, callback: Callback<T>?): DataResponse<T>

  /** retry fetching data few times with time interval when the request gets failure. */
  public fun retry(retryCount: Int, interval: Long): DataResponse<T>

  /** observes a [ApiResponse] value from the API call request. */
  public fun observeResponse(observer: ResponseObserver<T>): DataResponse<T>

  /**
   * concat an another [DataResponse] and request API calls sequentially.
   * we can determine request continuously the concat [DataResponse] or stop when failed using [ConcatStrategy].
   */
  public fun <R> concat(dataSource: DataResponse<R>): DataResponse<R>

  /** request API call and response to the callback. */
  public fun request(): DataResponse<T>

  /** joins onto [CompositeDisposable] as a disposable. */
  public fun joinDisposable(disposable: CompositeDisposable): DataResponse<T>

  /** invalidate cached data and retry counts, request again API call. */
  public fun invalidate()

  /** A concat strategy for determining to request continuously or stop when the first request got failed. */
  public enum class ConcatStrategy {
    // request next call continuously.
    CONTINUOUS,

    // break requesting chain when the previous request got failed.
    BREAK
  }
}
