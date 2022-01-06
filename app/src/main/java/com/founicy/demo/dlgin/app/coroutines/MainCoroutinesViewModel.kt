package com.founicy.demo.dlgin.app.coroutines

import androidx.lifecycle.*
import com.founicy.demo.dlgin.app.mapper.ErrorEnvelopeMapper
import com.founicy.module.gin.StatusCode
import com.founicy.module.gin.map
import com.founicy.module.gin.message
import com.founicy.module.gin.suspendOnProcedure
import com.founicy.demo.dlgin.app.model.Poster
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

import timber.log.Timber

class MainCoroutinesViewModel constructor(disneyService: DisneyCoroutinesService) : ViewModel() {

  val posterListLiveData: LiveData<List<Poster>>
  val toastLiveData = MutableLiveData<String>()

  init {
    Timber.d("initialized MainViewModel.")

    posterListLiveData = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
      emitSource(
        flow {
          disneyService.fetchDisneyPosterList()
            .suspendOnProcedure(
              // handles the success case when the API request gets a successful response.
              onSuccess = {
                Timber.d("$data")

                emit(data)
              },
              // handles error cases when the API request gets an error response.
              // e.g., internal server error.
              onError = {
                Timber.d(message())

                // handles error cases depending on the status code.
                when (statusCode) {
                  StatusCode.InternalServerError -> toastLiveData.postValue("InternalServerError")
                  StatusCode.BadGateway -> toastLiveData.postValue("BadGateway")
                  else -> toastLiveData.postValue("$statusCode(${statusCode.code}): ${message()}")
                }

                // map the ApiResponse.Failure.Error to our custom error model using the mapper.
                map(ErrorEnvelopeMapper) {
                  Timber.d("[Code: $code]: $message")
                }
              },
              // handles exceptional cases when the API request gets an exception response.
              // e.g., network connection error, timeout.
              onException = {
                Timber.d(message())
                toastLiveData.postValue(message())
              }
            )
        }.flowOn(Dispatchers.IO).asLiveData()
      )
    }
  }
}
