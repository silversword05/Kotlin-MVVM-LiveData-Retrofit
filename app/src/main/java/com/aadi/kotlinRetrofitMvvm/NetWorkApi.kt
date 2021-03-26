package com.aadi.kotlinRetrofitMvvm


import com.aadi.kotlinRetrofitMvvm.model.NewsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NetWorkApi{

    @GET("v2/top-headlines")
    fun getProducts(@Query("q") query: String, @Query("apikey") apiKey: String): Call<NewsResponse>

}