package com.aadi.kotlinRetrofitMvvm.view

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.aadi.kotlinRetrofitMvvm.R
import com.aadi.kotlinRetrofitMvvm.databinding.FragmentProductsBinding
import com.aadi.kotlinRetrofitMvvm.model.NewsResponse
import com.aadi.kotlinRetrofitMvvm.viewmodel.ProductViewModel
import org.koin.android.viewmodel.ext.android.viewModel


class ProductDetailFragment : Fragment(R.layout.fragment_products) {

    lateinit var query: String
    private val productListModel: ProductViewModel by viewModel()
    private lateinit var binding: FragmentProductsBinding

    companion object {
        private const val KEY_STRING = "KEY_STRING"

        fun newInstance(query: String): ProductDetailFragment {
            val args = Bundle()
            args.putSerializable(KEY_STRING, query)
            val fragment = ProductDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { query = it.getSerializable(KEY_STRING) as String }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProductsBinding.bind(view)

        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)

        if (this::query.isInitialized) {

            var productDetailAdapter: ProductDetailAdapter? = null

            productListModel.getProducts(query)
            productListModel.getLiveProductList().observe(viewLifecycleOwner, Observer(function = fun(newsResponse: NewsResponse?) {
                newsResponse?.let {

                    productDetailAdapter?.setNewsResponse(newsResponse) ?: run {
                        productDetailAdapter = ProductDetailAdapter(newsResponse)
                    }
                    productDetailAdapter?.notifyDataSetChanged()
                    binding.recyclerView.adapter = productDetailAdapter
                    binding.progressBar.visibility = View.GONE
                }
            }))
        }

    }
}