package com.aadi.kotlinRetrofitMvvm.repository

import android.util.Log
import com.aadi.kotlinRetrofitMvvm.NetWorkApi
import com.aadi.kotlinRetrofitMvvm.model.NewsResponse
import com.aadi.kotlinRetrofitMvvm.view.MainActivity.Companion.API_KEY
import retrofit2.Call
import retrofit2.Response

class DataRepository(val netWorkApi: NetWorkApi) {

    fun getProducts(onProductData: OnProductData, query: String) {
        netWorkApi.getProducts(query, API_KEY)
            .enqueue(object : retrofit2.Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                onProductData.onSuccess((response.body() as NewsResponse))
                Log.v(DataRepository::class.qualifiedName, "Network API succeeded to get products")
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                onProductData.onFailure()
                Log.v(DataRepository::class.qualifiedName, "Network API failed to get products")
            }
        })
    }

    interface OnProductData {
        fun onSuccess(data: NewsResponse)
        fun onFailure()
    }
}

