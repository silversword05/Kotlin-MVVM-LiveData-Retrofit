package com.aadi.kotlinRetrofitMvvm.view

import android.os.Bundle
import android.view.Menu
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aadi.kotlinRetrofitMvvm.R
import com.aadi.kotlinRetrofitMvvm.databinding.ActivityTabbedBinding

class TabbedActivity : AppCompatActivity() {

    companion object {
        lateinit var API_KEY: String
    }

    private lateinit var activityTabbedBinding: ActivityTabbedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityTabbedBinding = ActivityTabbedBinding.inflate(this.layoutInflater)
        setContentView(activityTabbedBinding.root)
        supportActionBar?.hide()

        val query1: String = intent.getStringExtra(resources.getString(R.string.query_group1)) ?: ""
        val query2: String = intent.getStringExtra(resources.getString(R.string.query_group2)) ?: ""

        API_KEY = getString(R.string.news_api_id)
        val navView: BottomNavigationView = activityTabbedBinding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(activityTabbedBinding.navHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_food, R.id.navigation_drinks))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val menu: Menu = navView.menu
        menu.findItem(R.id.navigation_food).title = query1.replace('+', ' ')
        menu.findItem(R.id.navigation_drinks).title = query2.replace('+', ' ')

        savedInstanceState ?: run {
            val newFragment = ProductDetailFragment.newInstance(query1)
            supportFragmentManager
                .beginTransaction()
                .replace(activityTabbedBinding.navHostFragment.id, newFragment)
                .commit()
        }

        navView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener(function = fun(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.navigation_food -> {
                    val newFragment = ProductDetailFragment.newInstance(query1)
                    supportFragmentManager
                        .beginTransaction()
                        .replace(activityTabbedBinding.navHostFragment.id, newFragment)
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }

                R.id.navigation_drinks -> {
                    val newFragment = ProductDetailFragment.newInstance(query2)
                    supportFragmentManager
                        .beginTransaction()
                        .replace(activityTabbedBinding.navHostFragment.id, newFragment)
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
            }
            return@OnNavigationItemSelectedListener false
        }))
    }
}