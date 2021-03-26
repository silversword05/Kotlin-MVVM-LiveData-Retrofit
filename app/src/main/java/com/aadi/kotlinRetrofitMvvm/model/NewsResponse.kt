package com.aadi.kotlinRetrofitMvvm.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class NewsResponse(
    @SerializedName("status")
    @Expose
    var status: String,

    @SerializedName("totalResults")
    @Expose
    var totalResults: Int,

    @SerializedName("articles")
    @Expose
    var products: List<NewsData>

) : Serializable