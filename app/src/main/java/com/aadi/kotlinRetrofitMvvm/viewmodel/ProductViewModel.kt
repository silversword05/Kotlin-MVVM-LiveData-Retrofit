package com.aadi.kotlinRetrofitMvvm.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.LiveData
import com.aadi.kotlinRetrofitMvvm.model.NewsResponse
import com.aadi.kotlinRetrofitMvvm.repository.DataRepository
import org.koin.standalone.KoinComponent


class ProductViewModel(private val dataRepository: DataRepository) : ViewModel(), KoinComponent {

    private var listOfProducts: MutableLiveData<NewsResponse?>? = MutableLiveData()

    init {
        listOfProducts?.value = null
        Log.v(ProductViewModel::class.qualifiedName, "Initializing Product View Model class")
    }

    fun getProducts(query: String) {
        Log.v(ProductViewModel::class.qualifiedName, "Get Product in ProductViewModel with query $query")
        dataRepository.getProducts(object : DataRepository.OnProductData {
            override fun onSuccess(data: NewsResponse) {
                listOfProducts?.value = data
                Log.v(ProductViewModel::class.qualifiedName, "Success product in ProductViewModel with query $query, list ipdated")
            }

            override fun onFailure() {
                Log.v(ProductViewModel::class.qualifiedName, "Failure to get products in Product View Model")
            }
        }, query)
    }

    fun getLiveProductList(): LiveData<NewsResponse?> = listOfProducts!!
}