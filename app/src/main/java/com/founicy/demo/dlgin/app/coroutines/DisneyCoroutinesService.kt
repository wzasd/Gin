package com.founicy.demo.dlgin.app.coroutines


import com.founicy.module.gin.ApiResponse
import com.founicy.demo.dlgin.app.model.Poster
import retrofit2.http.GET

interface DisneyCoroutinesService {

  @GET("DisneyPosters.json")
  suspend fun fetchDisneyPosterList(): ApiResponse<List<Poster>>
}
