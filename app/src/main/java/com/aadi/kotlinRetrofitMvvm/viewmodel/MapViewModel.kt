package com.aadi.kotlinRetrofitMvvm.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aadi.kotlinRetrofitMvvm.R
import com.aadi.kotlinRetrofitMvvm.SharedPreferenceUtil
import com.aadi.kotlinRetrofitMvvm.locationCodes.ForegroundOnlyLocationService
import com.aadi.kotlinRetrofitMvvm.locationCodes.MyLocationManager
import com.aadi.kotlinRetrofitMvvm.view.MapActivity
import com.firebase.geofire.GeoFire
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class MapViewModel(application: Application) : AndroidViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        private lateinit var geoFire: GeoFire

        fun initializeGeoFire() {
            Firebase.auth.currentUser?.uid?.let { uid ->
                val userReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Locations").child(uid)
                geoFire = GeoFire(userReference)
            }
        }

        fun getGeoFireInstance(): GeoFire {
            if (this::geoFire.isInitialized) return geoFire
            throw Exception("Authentication has not been successful")
        }
    }

    private var sharedPreferences: SharedPreferences =
        application.applicationContext.getSharedPreferences(application.applicationContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

    private val foregroundEnabledService: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    private var foregroundOnlyLocationServiceBound: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    private val myLocationManager: MyLocationManager by lazy {
        MyLocationManager.getInstance(application.applicationContext)
    }

    val receivingLocationUpdates: LiveData<Boolean> = myLocationManager.receivingLocationUpdates

    fun startBackgroundLocationUpdates() = myLocationManager.startLocationUpdates()
    fun stopBackgroundLocationUpdates() = myLocationManager.stopLocationUpdates()


    fun getForegroundEnabledService(): LiveData<Boolean> = foregroundEnabledService
    fun getForegroundOnlyLocationServiceBound(): LiveData<Boolean> = foregroundOnlyLocationServiceBound
    fun setForegroundOnlyLocationServiceBound(value: Boolean) {
        foregroundOnlyLocationServiceBound.value = value
    }

    fun unregisterSharedPreferencesListener() = sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    fun registerSharedPreferencesListener() = sharedPreferences.registerOnSharedPreferenceChangeListener(this)

    override fun onSharedPreferenceChanged(p0: SharedPreferences, p1: String) {
        if (p1 == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            foregroundEnabledService.value = p0.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
        }
    }
}