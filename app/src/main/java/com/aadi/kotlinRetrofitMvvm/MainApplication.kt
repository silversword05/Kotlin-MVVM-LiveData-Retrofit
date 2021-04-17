package com.aadi.kotlinRetrofitMvvm

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.android.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
        startKoin(this,
            listOf(mainModule),
            loadPropertiesFromFile = true)
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.v(MainApplication::class.qualifiedName, "Terminating application")
        Firebase.auth.signOut()
    }
}