

# Gin

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=16"><img alt="API" src="https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/wzasd/Gin/actions"><img alt="Build Status" src="https://github.com/wzasd/Gin/actions/workflows/android.yml/badge.svg?branch=main"/></a>
</p>

## 为什么要使用 Gin?

Gin 是用来构建标准化响应接口而开发的，解耦业务和网络框架开发的。框架可以简单直观的处理成功、失败和异常的数据吧，并且有良好的扩展性。所以我们并不需要设计和实现如「Resource」或者「Result」之类的包装类，减少我们的模板代码，只需要关注业务代码。Gin 支持全局的[error response globally](#global-operator)，[Mapper](#mapper)，[Operator](#operator)，而且对[LiveData](#tolivedata)和[Flow](#toflow)兼容。当然，对当前比较流行的协程（[coroutine](#apiresponse-for-coroutines)）还有[flow](#suspendonsuccess-suspendonerror-suspendonexception)都有很好地支持，引入一次，让你用上最新技术~

## Download

[![Maven Central](https://img.shields.io/maven-central/v/io.github.wzasd/gin.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.wzasd%22%20AND%20a:%22gin%22)

### Gradle

将以下代码添加到你根目录的`build.gralde`（不是模块内的）。

```gradle
allprojects {
    repositories {
        mavenCentral()
    }
}
```

讲以下依赖代码复制到你的模块内`bulid.gradle`文件内。

```gradle
dependencies {
    implementation "io.github.wzasd:gin:1.0.0"
}
```

## 目录

- [ApiResponse](https://github.com/wzasd/gin#apiresponse)
- [onSuccess, onError, onException](https://github.com/wzasd/gin#apiresponse-extensions)
- [ApiResponse for coroutines](https://github.com/wzasd/gin#apiresponse-for-coroutines)
- [suspendOnSuccess, suspendOnError, suspendOnException](https://github.com/wzasd/gin#suspendonsuccess-suspendonerror-suspendonexception)
- [Retrieve success data](https://github.com/wzasd/gin#retrieve-success-data)
- [Mapper](https://github.com/wzasd/gin#mapper)
- [Operator](https://github.com/skydoves/sandwich#operator), [Operator for coroutines](https://github.com/wzasd/gin#operator-with-coroutines), [Global Operator](https://github.com/skydoves/sandwich#global-operator)
- [Merge](https://github.com/wzasd/gin#merge)
- [toLiveData](https://github.com/skydoves/sandwich#tolivedata). [toFlow](https://github.com/wzasd/gin#toflow)
- [ResponseDataSource](https://github.com/wzasd/gin#responsedatasource)

## 使用

### ApiResponse

`ApiResponse` 是一个接口，用于根据标准化响应的格式规范。它为处理成功的数据和错误响应提供了灵活的扩展。我们可以使用来自 `Call` 的范围扩展 `request` 来获取 `ApiResponse`。下面的示例是从 `Call` 实例获取`ApiResponse`的基础。

```kotlin
interface DisneyService {
  @GET("/")
  fun fetchDisneyPosterList(): Call<List<Poster>>
}

val disneyService = retrofit.create(DisneyService::class.java)
// fetches a model list from the network and getting [ApiResponse] asynchronously.
disneyService.fetchDisneyPosterList().request { response ->
      when (response) {
        // handles the success case when the API request gets a successful response.
        is ApiResponse.Success -> {
          posterDao.insertPosterList(response.data)
          livedata.post(response.data)
        }
        // handles error cases when the API request gets an error response.
        // e.g., internal server error.
        is ApiResponse.Failure.Error -> {
          // stub error case
          Timber.d(message())

          // handles error cases depending on the status code.
          when (statusCode) {
            StatusCode.InternalServerError -> toastLiveData.postValue("InternalServerError")
            StatusCode.BadGateway -> toastLiveData.postValue("BadGateway")
            else -> toastLiveData.postValue("$statusCode(${statusCode.code}): ${message()}")
          }
        }
        // handles exceptional cases when the API request gets an exception response.
        // e.g., network connection error, timeout.
        is ApiResponse.Failure.Exception -> {
          // stub exception case
        }
      }
    }
```

#### ApiResponse.Success

 Retrofit 网络响应的标准成功响应接口。<br>
我们可以从 `ApiResponse.Success` 中获取响应的成功主体数据、`StatusCode`、`Headers` 等。

```kotlin
val data: List<Poster>? = response.data
val statusCode: StatusCode = response.statusCode
val headers: Headers = response.headers
```

#### ApiResponse.Failure.Error

 Retrofit 网络响应的异常响应接口。<br> API 通信错误处理。

```kotlin
val errorBody: ResponseBody? = response.errorBody
val statusCode: StatusCode = response.statusCode
val headers: Headers = response.headers
```

#### ApiResponse.Failure.Exception 

在客户端创建请求或处理响应时发生意外。

### ApiResponse Extensions

当然，我们可以使用扩展方便地处理响应案例。

#### onSuccess, onError, onException

我们可以将这些作用域函数用于 `ApiResponse`，我们在不使用 `if-else/when` 子句的情况下处理响应情况。 <br> 每个范围将根据 `ApiResponse` 的类型执行或不执行。 （成功、错误、异常）

```kotlin
disneyService.fetchDisneyPosterList().request { response ->
    response.onSuccess {
     // this scope will be only executed if the request would successful.
     // handle the success case
    }.onError {
      // this scope will be only executed when the request would get errors.
      // handle the error case
    }.onException {
     // this scope will be only executed when the request would get exceptions.
     // handle the exception case
    }
  }
```

### ApiResponse for coroutines

如果需要使用协程中使用 `suspend` 关键字并获取 `ApiResponse<*>` 作为响应类型。<br> 使用 `CoroutinesResponseCallAdapterFactory` 调用适配器工厂构建你的改造。

```kotlin
.addCallAdapterFactory(CoroutinesResponseCallAdapterFactory.create())
```

我们应该使用 `suspend` 关键字将正常的服务功能作为暂停功能。我们可以得到 `ApiResponse<*>` 作为响应类型。因此，我们可以从 Retrofit 服务调用中获取 `ApiResponse`，并使用扩展立即处理它们。

```kotlin
interface DisneyCoroutinesService {

  @GET("DisneyPosters.json")
  suspend fun fetchDisneyPosterList(): ApiResponse<List<Poster>>
}
```

我们可以像下面这样使用协程：

```kotlin
class MainCoroutinesViewModel constructor(disneyService: DisneyCoroutinesService) : ViewModel() {

  val posterListLiveData: MutableLiveData<List<Poster>>

  init {
     val response = disneyService.fetchDisneyPosterList()
     response.onSuccess {
       // handles the success case when the API request gets a successful response.
       posterDao.insertPosterList(data)
       posterListLiveData.post(data)
      }.onError {
       // handles error cases when the API request gets an error response.
      }.onException {
       // handles exceptional cases when the API request gets an exception response.
      }
    }
  }
}
```

#### suspendOnSuccess, suspendOnError, suspendOnException

我们可以使用暂停扩展来调用作用域内的暂停相关功能。这些扩展在功能上与 `onSuccess`、`onError` 和 `onException` 扩展没有区别。 <br>
一般我们可以在[repository pattern](https://github.com/wzasd/Pokedex/blob/main/app/src/main/java/com/skydoves/pokedex/repository/MainRepository.kt) 上使用这种方式。

```kotlin
flow {
  val response = disneyService.fetchDisneyPosterList()
  response.suspendOnSuccess {
    posterDao.insertPosterList(data)
    emit(data)
  }.suspendOnError {
    // handles error cases
  }.suspendOnFailure {
    // handles exceptional cases
  }
}.flowOn(Dispatchers.IO)
```

### Retrieve success data

如果我们想直接从 `ApiResponse` 中检索封装的成功数据，我们可以使用以下功能。

#### getOrNull

如果此实例表示 `ApiResponse.Success`，则返回封装的数据；如果此实例失败，则返回 null。

```kotlin
val data: List<Poster>? = disneyService.fetchDisneyPosterList().getOrNull()
```

#### getOrElse

如果此实例表示 `ApiResponse.Success`，则返回封装的数据，如果失败，则返回默认值。

```kotlin
val data: List<Poster>? = disneyService.fetchDisneyPosterList().getOrElse(emptyList())
```

#### getOrThrow

如果此实例表示 `ApiResponse.Success`，则返回封装的数据；如果失败，则抛出封装的 `Throwable` 异常。

```kotlin
try {
  val data: List<Poster>? = disneyService.fetchDisneyPosterList().getOrThrow()
} catch (e: Exception) {
  e.printStackTrace()
}
```

### Mapper

当想要将 `ApiResponse.Success` 或 `ApiResponse.Failure.Error` 转换为 `ApiResponse` 扩展范围中的自定义模型时，映射器很有用。

#### ApiSuccessModelMapper

可以使用 `SuccessPosterMapper<T, R>` 和 `map` 扩展将 `ApiResponse.Success` 模型映射到的自定义模型。

```kotlin
object SuccessPosterMapper : ApiSuccessModelMapper<List<Poster>, Poster?> {

  override fun map(apiErrorResponse: ApiResponse.Success<List<Poster>>): Poster? {
    return apiErrorResponse.data.first()
  }
}

// Maps the success response data.
val poster: Poster? = map(SuccessPosterMapper)
```

可以使用带有 lambda 的 `map` 扩展。

```kotlin
// Maps the success response data using a lambda.
map(SuccessPosterMapper) { poster ->
  emit(poster) // we can use the `this` keyword instead of the poster.
}
```

如果想在 lambda 中从头开始获取转换后的数据，可以将映射器作为 `onSuccess` 或 `suspendOnSuccess` 的参数。

```kotlin
.suspendOnSuccess(SuccessPosterMapper) {
    val poster = this
}
```

#### ApiErrorModelMapper

可以使用 `ApiErrorModelMapper<T>` 和 `map` 扩展将 `ApiResponse.Failure.Error` 模型映射到自定义错误模型。

```kotlin
// Create your custom error model.
data class ErrorEnvelope(
  val code: Int,
  val message: String
)

// An error response mapper.
// Create an instance of your custom model using the `ApiResponse.Failure.Error` in the `map`.
object ErrorEnvelopeMapper : ApiErrorModelMapper<ErrorEnvelope> {

  override fun map(apiErrorResponse: ApiResponse.Failure.Error<*>): ErrorEnvelope {
    return ErrorEnvelope(apiErrorResponse.statusCode.code, apiErrorResponse.message())
  }
}

// Maps an error response.
response.onError {
  // Maps an ApiResponse.Failure.Error to a custom error model using the mapper.
  map(ErrorEnvelopeMapper) {
     val code = this.code
     val message = this.message
  }
}
```

如果想在 lambda 中从头开始获取转换后的数据，可以将映射器作为 `onError` 或 `suspendOnError` 的参数。

```kotlin
.suspendOnError(ErrorEnvelopeMapper) {
    val message = this.message
}
```

### Operator

使用 `operator` 扩展和 `ApiResponseOperator` 委托 `onSuccess`、`onError`、`onException`。如果想要标准地处理 `ApiResponse` 或减少 `ViewModel` 和 `Repository` 的角色，操作符非常有用。这是标准化错误和异常处理的示例。

#### ViewModel

使用 `operate` 扩展委托和操作 `CommonResponseOperator`。

```kotlin
disneyService.fetchDisneyPosterList().operator(
      CommonResponseOperator(
        success = {
          emit(data)
          Timber.d("success data: $data")
        },
        application = getApplication()
      )
    )
```

#### CommonResponseOperator

`CommonResponseOperator` 使用 `onSuccess`、`onError`、`onException` 覆盖方法扩展了 `ApiResponseOperator`。它们将根据 `ApiResponse` 的类型执行。

```kotlin
/** A common response operator for handling [ApiResponse]s regardless of its type. */
class CommonResponseOperator<T> constructor(
  private val success: suspend (ApiResponse.Success<T>) -> Unit,
  private val application: Application
) : ApiResponseOperator<T>() {

  // handles error cases when the API request gets an error response.
  override fun onSuccess(apiResponse: ApiResponse.Success<T>) = success(apiResponse)

  // handles error cases depending on the status code.
  // e.g., internal server error.
  override fun onError(apiResponse: ApiResponse.Failure.Error<T>) {
    apiResponse.run {
      Timber.d(message())
      
      // map the ApiResponse.Failure.Error to a customized error model using the mapper.
      map(ErrorEnvelopeMapper) {
        Timber.d("[Code: $code]: $message")
      }
    }
  }

  // handles exceptional cases when the API request gets an exception response.
  // e.g., network connection error, timeout.
  override fun onException(apiResponse: ApiResponse.Failure.Exception<T>) {
    apiResponse.run {
      Timber.d(message())
      toast(message())
    }
  }
}
```

### Operator for coroutines

如果想操作并将一个暂停 lambda 委托给操作员，可以使用 `suspendOperator` 扩展和 `ApiResponseSuspendOperator` 类

#### ViewModel

可以在 `success` lambda 中使用像 `emit` 这样的挂起函数。

```kotlin
flow {
  disneyService.fetchDisneyPosterList().suspendOperator(
      CommonResponseOperator(
        success = {
          emit(data)
          Timber.d("success data: $data")
        },
        application = getApplication()
      )
    )
}.flowOn(Dispatchers.IO)
```

#### CommonResponseOperator

`CommonResponseOperator` 使用挂起覆盖方法扩展了 `ApiResponseSuspendOperator`。

```kotlin
class CommonResponseOperator<T> constructor(
  private val success: suspend (ApiResponse.Success<T>) -> Unit,
  private val application: Application
) : ApiResponseSuspendOperator<T>() {

  // handles the success case when the API request gets a successful response.
  override suspend fun onSuccess(apiResponse: ApiResponse.Success<T>) = success(apiResponse)

  // ... //
```

### Global operator

也可以使用 `GinInitializer` 在应用程序中对所有 `ApiResponse` 全局操作运算符。所以不需要创建 Operators 的每个实例或使用依赖注入来处理常见的操作。这是一个处理关于 `ApiResponse.Failure.Error` 和 `ApiResponse.Failure.Exception` 的全局运算符的示例。在本例中，我们将手动处理 `ApiResponse.Success`。

#### Application class

We can initialize the global operator on the `GinInitializer.sandwichOperator`. It is recommended to initialize it in the Application class.

```kotlin
class GinDemoApp : Application() {

  override fun onCreate() {
    super.onCreate()
    
    // We will handle only the error and exceptional cases,
    // so we don't need to mind the generic type of the operator.
    GinInitializer.sandwichOperator = GlobalResponseOperator<Any>(this)

    // ... //
```

#### GlobalResponseOperator

`GlobalResponseOperator` 可以扩展任何运算符（`ApiResponseSuspendOperator` 或 `ApiResponseOperator`）

```kotlin
class GlobalResponseOperator<T> constructor(
  private val application: Application
) : ApiResponseSuspendOperator<T>() {

  // The body is empty, because we will handle the success case manually.
  override suspend fun onSuccess(apiResponse: ApiResponse.Success<T>) { }

  // handles error cases when the API request gets an error response.
  // e.g., internal server error.
  override suspend fun onError(apiResponse: ApiResponse.Failure.Error<T>) {
    withContext(Dispatchers.Main) {
      apiResponse.run {
        Timber.d(message())

        // handling error based on status code.
        when (statusCode) {
          StatusCode.InternalServerError -> toast("InternalServerError")
          StatusCode.BadGateway -> toast("BadGateway")
          else -> toast("$statusCode(${statusCode.code}): ${message()}")
        }

        // map the ApiResponse.Failure.Error to a customized error model using the mapper.
        map(ErrorEnvelopeMapper) {
          Timber.d("[Code: $code]: $message")
        }
      }
    }
  }

  // handles exceptional cases when the API request gets an exception response.
  // e.g., network connection error, timeout.
  override suspend fun onException(apiResponse: ApiResponse.Failure.Exception<T>) {
    withContext(Dispatchers.Main) {
      apiResponse.run {
        Timber.d(message())
        toast(message())
      }
    }
  }

  private fun toast(message: String) {
    Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
  }
}
```

#### ViewModel

不需要使用 `operator` 表达式。全局操作符会自动操作，所以应该只处理 `ApiResponse.Success`。

```kotlin
flow {
  disneyService.fetchDisneyPosterList().
    suspendOnSuccess {
      emit(data)
    }
}.flowOn(Dispatchers.IO).asLiveData()
```

### Merge

可以根据策略将多个 `ApiResponse` 合并为一个 `ApiResponse`。<br>
如果每三个 `ApiResponse` 成功，则下面的示例将三个 `ApiResponse` 合并为一个。

```kotlin
disneyService.fetchDisneyPosterList(page = 0).merge(
   disneyService.fetchDisneyPosterList(page = 1),
   disneyService.fetchDisneyPosterList(page = 2),
   mergePolicy = ApiResponseMergePolicy.PREFERRED_FAILURE
).onSuccess { 
  // handles the success case when the API request gets a successful response.
}.onError { 
  // handles error cases when the API request gets an error response.
}
```

#### ApiResponseMergePolicy

`ApiResponseMergePolicy` 是用于合并响应数据的策略，取决于成功与否。

- IGNORE_FAILURE: 无论合并顺序如何，都会忽略响应中的失败响应。
- PREFERRED_FAILURE (default): 无论合并顺序如何，在响应中任何失败都会响应。

### toLiveData

如果响应是“ApiResponse.Success”，可以获得包含成功数据的“LiveData”。如果目标只是获得一个保存成功数据的 LiveData，可以发出 `onSuccess` 扩展。

```kotlin
posterListLiveData = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
  emitSource(
    disneyService.fetchDisneyPosterList()
     .onError {
      // handles error cases when the API request gets an error response.
     }.onException {
      // handles exceptional cases when the API request gets an exception response.
     }.toLiveData()) // returns an observable LiveData
}
```

如果想要转换原始数据并使用成功数据获取包含转换数据的“LiveData”，如果响应是“ApiResponse.Success”。

```kotlin
posterListLiveData = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
  emitSource(
   disneyService.fetchDisneyPosterList()
    .onError {
      // handles error cases when the API request gets an error response.
    }.onException {
      // handles exceptional cases when the API request gets an exception response.
    }.toLiveData {
      this.onEach { poster -> poster.date = SystemClock.currentThreadTimeMillis() }
    }) // returns an observable LiveData
    }
```

### toFlow

如果响应是 `ApiResponse.Success` 并且数据不为空，我们可以获得发出成功数据的 `Flow`。

```kotlin
disneyService.fetchDisneyPosterList()
  .onError {
    // handles error cases when the API request gets an error response.
  }.onException {
    // handles exceptional cases when the API request gets an exception response.
  }.toFlow() // returns a coroutines flow
  .flowOn(Dispatchers.IO)
```

如果想要转换原始数据并使用成功数据获取包含转换数据的“流”，如果响应是“ApiResponse.Success”并且数据不为空。

```kotlin
val response = pokedexClient.fetchPokemonList(page = page)
response.toFlow { pokemons ->
  pokemons.forEach { pokemon -> pokemon.page = page }
  pokemonDao.insertPokemonList(pokemons)
  pokemonDao.getAllPokemonList(page)
}.flowOn(Dispatchers.IO)
```

### ResponseDataSource

ResponseDataSource 是 `DataSource` 接口的实现。 <br>

 * 异步发送请求。 
 * 来自 REST API 调用的临时响应数据持有者，用于在内存中缓存数据。
 * 可观察到每个响应。 
 * 请求失败时重试获取数据。
 * 连接另一个 `DataSource` 并按顺序请求。
 * 单次执行

 #### Combine

结合 `Call` 和 lambda 范围来构造 DataSource。

```kotlin
val disneyService = retrofit.create(DisneyService::class.java)

val dataSource = ResponseDataSource<List<Poster>>()
dataSource.combine(disneyService.fetchDisneyPosterList()) { response ->
    // stubs
}
```

#### Request

异步请求 API 网络调用。 <br>如果请求成功，此数据源将保存成功响应模型。<br>在成功后的下一个请求中，request() 返回缓存的 API 响应。 <br>如果我们需要获取新的响应数据或刷新，我们可以使用 `invalidate()`。

```kotlin
dataSource.request()
```

#### Retry

如果您的请求失败，请重试获取数据（重新请求）。

```kotlin
// retry fetching data 3 times with 5000 milli-seconds time interval when the request gets failure.
dataSource.retry(3, 5000L)
```

#### ObserveResponse

观察 API 调用请求中的每个响应数据“ApiResponse”。

```kotlin
dataSource.observeResponse {
   Timber.d("observeResponse: $it")
}
```

#### RetainPolicy

可以限制在临时内部存储上保留数据的策略。<br>
默认策略是不保留任何从网络获取的数据，但我们可以使用 `dataRetainPolicy` 方法设置策略。

```kotlin
// Retain fetched data on the memory storage temporarily.
// If request again, returns the retained data instead of re-fetching from the network.
dataSource.dataRetainPolicy(DataRetainPolicy.RETAIN)
```

#### Invalidate

使缓存（保存）数据无效并重新获取 API 请求

```kotlin
dataSource.invalidate()
```

#### Concat

如果 API 调用成功，则连接另一个 `DataSource` 并按顺序请求 API 调用。

```kotlin
val dataSource2 = ResponseDataSource<List<PosterDetails>>()
dataSource2.retry(3, 5000L).combine(disneyService.fetchDetails()) {
    // stubs handling dataSource2 response
}

dataSource1
   .request() // request() must be called before concat. 
   .concat(dataSource2) // request dataSource2's API call after the success of the dataSource1.
   .concat(dataSource3) // request dataSource3's API call after the success of the dataSource2.
```

#### asLiveData

可以通过 `DataSource` 观察获取的数据作为 `LiveData`。

```kotlin
val posterListLiveData: LiveData<List<Poster>>

init {
    posterListLiveData = disneyService.fetchDisneyPosterList().toResponseDataSource()
      .retry(3, 5000L)
      .dataRetainPolicy(DataRetainPolicy.RETAIN)
      .request {
        // ... //
      }.asLiveData()
}
```

#### Disposable

可以使用 `joinDisposable` 函数将其作为一次性用品加入到 `CompositeDisposable` 上。它必须在 `request()` 方法之前调用。下面的示例在 ViewModel 中使用。我们可以在 `onCleared()` 覆盖方法中清除 `CompositeDisposable`。

```kotlin
private val disposable = CompositeDisposable()

init {
    disneyService.fetchDisneyPosterList().toResponseDataSource()
      // retry fetching data 3 times with 5000L interval when the request gets failure.
      .retry(3, 5000L)
      // joins onto CompositeDisposable as a disposable and dispose onCleared().
      .joinDisposable(disposable)
      .request {
        // ... //
      }
}

override fun onCleared() {
    super.onCleared()
    if (!disposable.disposed) {
      disposable.clear()
    }
  }
```

这是 `MainViewModel` 中的 `ResponseDataSource` 的示例。

```kotlin
class MainViewModel constructor(
  private val disneyService: DisneyService
) : ViewModel() {

  // request API call Asynchronously and holding successful response data.
  private val dataSource = ResponseDataSource<List<Poster>>()

  val posterListLiveData = MutableLiveData<List<Poster>>()
  val toastLiveData = MutableLiveData<String>()
  private val disposable = CompositeDisposable()

  /** fetch poster list data from the network. */
  fun fetchDisneyPosters() {
    dataSource
      // retry fetching data 3 times with 5000 time interval when the request gets failure.
      .retry(3, 5000L)
      // joins onto CompositeDisposable as a disposable and dispose onCleared().
      .joinDisposable(disposable)
      // combine network service to the data source.
      .combine(disneyService.fetchDisneyPosterList()) { response ->
        // handles the success case when the API request gets a successful response.
        response.onSuccess {
          Timber.d("$data")
          posterListLiveData.postValue(data)
        }
          // handles error cases when the API request gets an error response.
          // e.g. internal server error.
          .onError {
            Timber.d(message())

            // handling error based on status code.
            when (statusCode) {
              StatusCode.InternalServerError -> toastLiveData.postValue("InternalServerError")
              StatusCode.BadGateway -> toastLiveData.postValue("BadGateway")
              else -> toastLiveData.postValue("$statusCode(${statusCode.code}): ${message()}")
            }

            // map the ApiResponse.Failure.Error to a customized error model using the mapper.
            map(ErrorEnvelopeMapper) {
              Timber.d(this.toString())
            }
          }
          // handles exceptional cases when the API request gets an exception response.
          // e.g. network connection error, timeout.
          .onException {
            Timber.d(message())
            toastLiveData.postValue(message())
          }
      }
      // observe every API request responses.
      .observeResponse {
        Timber.d("observeResponse: $it")
      }
      // request API network call asynchronously.
      // if the request is successful, the data source will hold the success data.
      // in the next request after success, returns the cached API response.
      // if you want to fetch a new response data, use invalidate().
      .request()
  }

  override fun onCleared() {
    super.onCleared()
    if (!disposable.disposed) {
      disposable.clear()
    }
  }
}
```

### DataSourceCallAdapterFactory

我们可以直接从 Retrofit 服务获取 `DataSource`。 <br> 将调用适配器工厂 `DataSourceCallAdapterFactory` 添加到您的 Retrofit 构建器。 <br> 并将服务`Call`的返回类型更改为`DataSource`。

```kotlin
Retrofit.Builder()
    ...
    .addCallAdapterFactory(DataSourceCallAdapterFactory.create())
    .build()

interface DisneyService {
  @GET("DisneyPosters.json")
  fun fetchDisneyPosterList(): DataSource<List<Poster>>
}
```

这里 MainViewModel 中的“DataSource”示例。

```kotlin
class MainViewModel constructor(disneyService: DisneyService) : ViewModel() {

  // request API call Asynchronously and holding successful response data.
  private val dataSource: DataSource<List<Poster>>

    init {
    Timber.d("initialized MainViewModel.")

    dataSource = disneyService.fetchDisneyPosterList()
      // retry fetching data 3 times with 5000L interval when the request gets failure.
      .retry(3, 5000L)
      .observeResponse(object : ResponseObserver<List<Poster>> {
        override fun observe(response: ApiResponse<List<Poster>>) {
          // handle the case when the API request gets a success response.
          response.onSuccess {
            Timber.d("$data")
            posterListLiveData.postValue(data)
          }
        }
      })
      .request() // must call request()
```

### CoroutinesDataSourceCallAdapterFactory

可以使用 `suspend` 直接从 Retrofit 服务获取`DataSource`。 <br>

```kotlin
Retrofit.Builder()
    ...
    .addCallAdapterFactory(CoroutinesDataSourceCallAdapterFactory.create())
    .build()

interface DisneyService {
  @GET("DisneyPosters.json")
  fun fetchDisneyPosterList(): DataSource<List<Poster>>
}
```

这是 MainViewModel 中的 `DataSource` 的示例。

```kotlin
class MainCoroutinesViewModel constructor(disneyService: DisneyCoroutinesService) : ViewModel() {

  val posterListLiveData: LiveData<List<Poster>>

  init {
    Timber.d("initialized MainViewModel.")

    posterListLiveData = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
      emitSource(disneyService.fetchDisneyPosterList().toResponseDataSource()
        // retry fetching data 3 times with 5000L interval when the request gets failure.
        .retry(3, 5000L)
        // a retain policy for retaining data on the internal storage
        .dataRetainPolicy(DataRetainPolicy.RETAIN)
        // request API network call asynchronously.
        .request {
          // handle the case when the API request gets a success response.
          onSuccess {
            Timber.d("$data")
          }.onError { // handle the case when the API request gets a error response.
              Timber.d(message())
            }.onException {  // handle the case when the API request gets a exception response.
              Timber.d(message())
            }
        }.asLiveData())
    }
  }
}
```

#### toResponseDataSource

使用下面的方法从网络调用中获取实例后，我们可以将 `DataSource` 更改为 `ResponseDataSource`。

```kotlin
private val dataSource: ResponseDataSource<List<Poster>>

  init {
    dataSource = disneyService.fetchDisneyPosterList().toResponseDataSource()

    //...
  }
```


## Find this library useful? :heart:

# License

```xml
Copyright 2020 wzasd (Jeffrey wang)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
