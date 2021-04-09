package com.aadi.kotlinRetrofitMvvm

import com.aadi.kotlinRetrofitMvvm.repository.DataRepository
import com.aadi.kotlinRetrofitMvvm.viewmodel.ProductViewModel
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

val mainModule = module {

    single { DataRepository(get()) }

    single { createWebService() }

    viewModel { ProductViewModel(get()) }

}

fun createWebService(): NetWorkApi {
    val dispatcher = Dispatcher()
    dispatcher.maxRequestsPerHost = 2
    val okHttpClient: OkHttpClient = OkHttpClient.Builder().dispatcher(dispatcher).build()

    val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .baseUrl("https://newsapi.org/")
        .build()

    return retrofit.create(NetWorkApi::class.java)
}