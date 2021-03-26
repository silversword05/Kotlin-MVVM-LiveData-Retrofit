package com.aadi.kotlinRetrofitMvvm.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.aadi.kotlinRetrofitMvvm.R
import com.aadi.kotlinRetrofitMvvm.model.NewsResponse


class ProductDetailAdapter(private var newsResponse: NewsResponse) :
    androidx.recyclerview.widget.RecyclerView.Adapter<ProductDetailAdapter.ViewHolder>() {
    private var onItemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.adapter_products_details, p0, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return newsResponse.products.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.tvTitle?.text = newsResponse.products[position].title
        viewHolder.tvDescription?.text = newsResponse.products[position].description
        viewHolder.tvAuthorAndPublishedAt?.text = String.format("-%s | Published At: %s", newsResponse.products[position].author, newsResponse.products[position].publishedAt)
        Glide.with(viewHolder.imageViewCover.context).load(newsResponse.products[position].urlToImage).into(viewHolder.imageViewCover)

        viewHolder.itemView.setOnClickListener {
            run {
                val defaultBrowser = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
                defaultBrowser.data = Uri.parse(newsResponse.products[position].url)
                viewHolder.itemView.context.startActivity(defaultBrowser)
            }
        }
    }

    fun setNewsResponse(newsResponse: NewsResponse) {
        this.newsResponse = newsResponse
    }

    inner class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val imageViewCover: ImageView = itemView.findViewById<ImageView>(R.id.imgViewCover)
        val tvTitle: TextView? = itemView.findViewById<TextView>(R.id.tvTitle)
        val tvAuthorAndPublishedAt: TextView? = itemView.findViewById<TextView>(R.id.tvAuthorAndPublishedAt)
        val tvDescription: TextView? = itemView.findViewById<TextView>(R.id.tvDescription)

        init {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(itemView, 0)
            }
        }
    }


    fun setItemClickListener(clickListener: ItemClickListener) {
        onItemClickListener = clickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}