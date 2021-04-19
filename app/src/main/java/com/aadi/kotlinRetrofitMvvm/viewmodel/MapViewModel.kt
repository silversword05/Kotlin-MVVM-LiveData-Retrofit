package com.aadi.kotlinRetrofitMvvm.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aadi.kotlinRetrofitMvvm.R
import com.aadi.kotlinRetrofitMvvm.SharedPreferenceUtil
import com.aadi.kotlinRetrofitMvvm.locationCodes.MyLocationManager
import com.aadi.kotlinRetrofitMvvm.model.LocationData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class MapViewModel(application: Application) : AndroidViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        internal val userReference: DatabaseReference? by lazy {
            Firebase.auth.currentUser?.uid?.run {
                FirebaseDatabase.getInstance().getReference("Locations").child(this)
            }
        }
    }

    private var sharedPreferences: SharedPreferences =
        application.applicationContext.getSharedPreferences(application.applicationContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

    private val foregroundEnabledService: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    private val foregroundOnlyLocationServiceBound: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    private val locationsVisited: MutableLiveData<ArrayList<LocationData>> = MutableLiveData()

    init {
        locationsVisited.value = ArrayList()
    }

    private val myLocationManager: MyLocationManager by lazy {
        MyLocationManager.getInstance(application.applicationContext)
    }

    val receivingLocationUpdates: LiveData<Boolean> = myLocationManager.receivingLocationUpdates

    fun startBackgroundLocationUpdates() = myLocationManager.startLocationUpdates()
    fun stopBackgroundLocationUpdates() = myLocationManager.stopLocationUpdates()


    fun getForegroundEnabledService(): LiveData<Boolean> = foregroundEnabledService
    fun setForegroundEnabledService(value: Boolean) {
        foregroundEnabledService.postValue(value)
    }
    fun getForegroundOnlyLocationServiceBound(): LiveData<Boolean> = foregroundOnlyLocationServiceBound
    fun setForegroundOnlyLocationServiceBound(value: Boolean) {
        foregroundOnlyLocationServiceBound.value = value
    }

    fun unregisterSharedPreferencesListener() = sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    fun registerSharedPreferencesListener() = sharedPreferences.registerOnSharedPreferenceChangeListener(this)

    fun setLocationsListener() {
        userReference?.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(MapViewModel::class.qualifiedName, "onChildAdded:" + dataSnapshot.key!!)
                val locationData: LocationData? = dataSnapshot.getValue(LocationData::class.java)
                locationData?.run {
                    val currentLocations: ArrayList<LocationData> = locationsVisited.value as ArrayList<LocationData>
                    currentLocations.add(this)
                    locationsVisited.postValue(currentLocations)
                }
                Log.v(MapViewModel::class.qualifiedName, "Location fetched $locationData ${locationsVisited.value?.size}")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val locationData: LocationData? = snapshot.getValue(LocationData::class.java)
                locationData?.run {
                    val currentLocations: ArrayList<LocationData> = locationsVisited.value as ArrayList<LocationData>
                    currentLocations.filter { it.time == this.time }.forEach { locationsVisited.value?.remove(it) }
                    currentLocations.add(this)
                    locationsVisited.postValue(currentLocations)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val locationData: LocationData? = snapshot.getValue(LocationData::class.java)
                locationData?.run {
                    val currentLocations: ArrayList<LocationData> = locationsVisited.value as ArrayList<LocationData>
                    currentLocations.filter { it.time == this.time && it.latitude == this.latitude && it.longitude == this.longitude }.forEach { currentLocations.remove(it) }
                    locationsVisited.postValue(currentLocations)
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                val locationData: LocationData? = snapshot.getValue(LocationData::class.java)
                locationData?.run {
                    val currentLocations: ArrayList<LocationData> = locationsVisited.value as ArrayList<LocationData>
                    currentLocations.filter { it.time == this.time }.forEach { currentLocations.remove(it) }
                    locationsVisited.postValue(currentLocations)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(MapViewModel::class.qualifiedName, "Location Fetch Error", error.toException())
            }
        })
    }

    fun getLocationsVisited(): LiveData<ArrayList<LocationData>> = locationsVisited

    override fun onSharedPreferenceChanged(p0: SharedPreferences, p1: String) {
        if (p1 == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            foregroundEnabledService.value = p0.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
        }
    }
}