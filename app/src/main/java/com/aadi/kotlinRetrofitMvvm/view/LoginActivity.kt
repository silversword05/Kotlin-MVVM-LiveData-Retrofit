package com.aadi.kotlinRetrofitMvvm.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aadi.kotlinRetrofitMvvm.MainApplication
import com.aadi.kotlinRetrofitMvvm.databinding.ActivityLoginBinding
import com.aadi.kotlinRetrofitMvvm.viewmodel.LoginViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var activityLoginBinding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityLoginBinding = ActivityLoginBinding.inflate(this.layoutInflater)
        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        setContentView(activityLoginBinding.root)
        supportActionBar?.hide()

        activityLoginBinding.loginButton.setOnClickListener {
            val email: String = activityLoginBinding.loginEmailAddress.text.toString()
            val password: String = activityLoginBinding.loginEmailPassword.text.toString()
            if (email == "" || password == "") {
                Toast.makeText(this@LoginActivity, "Incorrect credentials provided, fill both fields", Toast.LENGTH_LONG).show()
            } else
                loginViewModel.signInCredentials(email, password)
        }
    }

    override fun onStart() {
        super.onStart()

        loginViewModel.getUidLiveData().observe(this, { currentUser ->
            currentUser?.run {
                Toast.makeText(this@LoginActivity, "Sign in successful", Toast.LENGTH_LONG).show()
                Log.v(LoginActivity::class.qualifiedName, "Current user id ${this.uid}")
                startActivity(Intent(this@LoginActivity,MapActivity::class.java))
            } ?: run {
                Toast.makeText(this@LoginActivity, "Sorry, no user currently signed in !!", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(MainApplication::class.qualifiedName, "Terminating application")
    }
}