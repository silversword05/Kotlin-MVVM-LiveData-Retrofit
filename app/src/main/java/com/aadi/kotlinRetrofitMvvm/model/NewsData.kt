package com.aadi.kotlinRetrofitMvvm.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NewsData(
    @SerializedName("title")
    var title: String?,

    @SerializedName("author")
    val author: String?,

    @SerializedName("urlToImage")
    val urlToImage: String?,

    @SerializedName("url")
    val url: String?,

    @SerializedName("publishedAt")
    val publishedAt: String?,

    @SerializedName("description")
    val description: String?
) : Serializable