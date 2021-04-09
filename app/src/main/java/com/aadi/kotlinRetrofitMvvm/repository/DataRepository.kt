package com.aadi.kotlinRetrofitMvvm.repository

import android.util.Log
import com.aadi.kotlinRetrofitMvvm.NetWorkApi
import com.aadi.kotlinRetrofitMvvm.model.NewsResponse
import com.aadi.kotlinRetrofitMvvm.view.TabbedActivity.Companion.API_KEY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class DataRepository(private val netWorkApi: NetWorkApi) {

    fun getProducts(onProductData: OnProductData, query: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response: Response<NewsResponse> = netWorkApi.getProducts(query, API_KEY)
                onProductData.onSuccess((response.body() as NewsResponse))
                Log.v(DataRepository::class.qualifiedName, "Network API succeeded to get products")
            } catch (e: Exception) {
                onProductData.onFailure(e)
                Log.e(DataRepository::class.qualifiedName, "Network API failed to get products ${e.localizedMessage}", e)
            }


        }
    }

    interface OnProductData {
        fun onSuccess(data: NewsResponse)
        fun onFailure(exception: Exception)
    }
}

