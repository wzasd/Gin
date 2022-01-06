package com.founicy.demo.dlgin.app.network

import com.founicy.module.gin.DataResponse
import com.founicy.demo.dlgin.app.model.Poster
import retrofit2.http.GET

interface DisneyService {

  @GET("DisneyPosters.json")
  fun fetchDisneyPosterList(): DataResponse<List<Poster>>
}
