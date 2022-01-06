package com.founicy.demo.dlgin.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.founicy.demo.dlgin.app.mapper.ErrorEnvelopeMapper
import com.founicy.demo.dlgin.app.network.DisneyService
import com.founicy.module.gin.*
import com.founicy.module.gin.disposables.CompositeDisposable
import com.founicy.demo.dlgin.app.model.Poster

import timber.log.Timber

class MainViewModel constructor(disneyService: DisneyService) : ViewModel() {

    val posterListLiveData: LiveData<List<Poster>>
    val toastLiveData = MutableLiveData<String>()
    private val disposable = CompositeDisposable()

    init {
        Timber.d("initialized MainViewModel.")

        posterListLiveData = disneyService.fetchDisneyPosterList().toResponseDataSource()
            // retry fetching data 3 times with 5000L interval when the request gets failure.
            .retry(3, 5000L)
            // a retain policy for retaining data on the internal storage.
            .dataRetainPolicy(DataRetainPolicy.RETAIN)
            // joins onto CompositeDisposable as a disposable and dispose onCleared().
            .joinDisposable(disposable)
            // request API network call asynchronously.
            // if the request is successful, the data source will hold the success data.
            // in the next request after success, returns the temporarily cached API response.
            // if you want to fetch a new response data, use NO_RETAIN policy or invalidate().
            .request {
                // handle the case when the API request gets a success response.
                onSuccess {
                    Timber.d("$data")
                }
                // handle the case when the API request gets a error response.
                // e.g. internal server error.
                onError {
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
                    // handle the case when the API request gets a exception response.
                    // e.g. network connection error, timeout.
                    .onException {
                        Timber.d(message())
                        toastLiveData.postValue(message())
                    }
            }.asLiveData()
    }

    override fun onCleared() {
        super.onCleared()
        if (!disposable.disposed) {
            disposable.clear()
        }
    }
}
