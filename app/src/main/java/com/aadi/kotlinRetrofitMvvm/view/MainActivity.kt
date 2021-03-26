package com.aadi.kotlinRetrofitMvvm.view

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aadi.kotlinRetrofitMvvm.R

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var API_KEY: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabbed)
        supportActionBar?.hide()

        API_KEY = getString(R.string.news_api_id)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_food, R.id.navigation_drinks))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        savedInstanceState ?: run {
            val newFragment = ProductDetailFragment.newInstance(resources.getString(R.string.title_food))
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.nav_host_fragment, newFragment)
                .commit()
        }

        navView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener(function = fun(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.navigation_food -> {
                    val newFragment = ProductDetailFragment.newInstance(resources.getString(R.string.title_food))
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, newFragment)
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }

                R.id.navigation_drinks -> {
                    val newFragment = ProductDetailFragment.newInstance(resources.getString(R.string.title_drinks))
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, newFragment)
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
            }
            return@OnNavigationItemSelectedListener false
        }))
    }
}