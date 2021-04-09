package com.aadi.kotlinRetrofitMvvm


import com.aadi.kotlinRetrofitMvvm.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NetWorkApi{

    @GET("v2/everything")
    suspend fun getProducts(@Query("q") query: String, @Query("apikey") apiKey: String): Response<NewsResponse>

}