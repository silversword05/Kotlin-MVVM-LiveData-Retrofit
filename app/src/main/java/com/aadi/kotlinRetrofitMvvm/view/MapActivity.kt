package com.aadi.kotlinRetrofitMvvm.view

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.aadi.kotlinRetrofitMvvm.BuildConfig
import com.aadi.kotlinRetrofitMvvm.R
import com.aadi.kotlinRetrofitMvvm.databinding.ActivityMapBinding
import com.aadi.kotlinRetrofitMvvm.locationCodes.ForegroundOnlyLocationService
import com.aadi.kotlinRetrofitMvvm.model.LocationData
import com.aadi.kotlinRetrofitMvvm.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56
        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
        private const val ZOOM_LEVEL = 15.0
        private val LOCATION_EXTRACTOR_TYPE: LocationExtractorType = LocationExtractorType.SERVICE_LOCATION_FOREGROUND
    }

    private lateinit var activityMapBinding: ActivityMapBinding
    private lateinit var mapViewModel: MapViewModel
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null
    private lateinit var mMap: GoogleMap

    private val backgroundRationalSnackbar: Snackbar by lazy {
        Snackbar.make(activityMapBinding.root, R.string.background_location_permission_rationale, Snackbar.LENGTH_LONG)
            .setAction(R.string.ok) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE)
            }
    }

    private val fineLocationRationalSnackbar: Snackbar by lazy {
        Snackbar.make(activityMapBinding.root, R.string.background_location_permission_rationale, Snackbar.LENGTH_LONG)
            .setAction(R.string.ok) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE)
            }
    }

    private val foregroundOnlyServiceConnection by lazy {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder = service as ForegroundOnlyLocationService.LocalBinder
                foregroundOnlyLocationService = binder.service
                mapViewModel.setForegroundOnlyLocationServiceBound(true)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                foregroundOnlyLocationService = null
                mapViewModel.setForegroundOnlyLocationServiceBound(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityMapBinding = ActivityMapBinding.inflate(this.layoutInflater)
        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        setContentView(activityMapBinding.root)
        supportActionBar?.hide()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) requestBackgroundLocationPermission()
        requestFineLocationPermission()

        mapViewModel.registerSharedPreferencesListener()

        activityMapBinding.startLocationButton.setOnClickListener {
            if (checkAllPermissions())
                when (LOCATION_EXTRACTOR_TYPE) {
                    LocationExtractorType.BROADCAST_LOCATION_BACKGROUND -> mapViewModel.startBackgroundLocationUpdates()
                    LocationExtractorType.SERVICE_LOCATION_FOREGROUND -> foregroundOnlyLocationService?.subscribeToLocationUpdates()
                        .also { mapViewModel.setForegroundEnabledService(true) } ?: Log.d(
                        MapActivity::class.qualifiedName,
                        "Location Service Not Bound"
                    )
                }
            else {
                Snackbar.make(activityMapBinding.root, "Permissions not accepted", Snackbar.LENGTH_LONG).show()
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) requestBackgroundLocationPermission()
                requestFineLocationPermission()
            }
        }

        activityMapBinding.stopLocationButton.setOnClickListener {
            when (LOCATION_EXTRACTOR_TYPE) {
                LocationExtractorType.SERVICE_LOCATION_FOREGROUND -> foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
                    .also { mapViewModel.setForegroundEnabledService(false) }
                LocationExtractorType.BROADCAST_LOCATION_BACKGROUND -> mapViewModel.stopBackgroundLocationUpdates()
            }
        }

        when (LOCATION_EXTRACTOR_TYPE) {
            LocationExtractorType.BROADCAST_LOCATION_BACKGROUND ->
                mapViewModel.receivingLocationUpdates.observe(this, { locationReceiveState ->
                    if (locationReceiveState) activityMapBinding.locationStatusView.text = getString(R.string.receiving_locations_text)
                    else activityMapBinding.locationStatusView.text = getString(R.string.not_receiving_locations_state)
                })
            LocationExtractorType.SERVICE_LOCATION_FOREGROUND ->
                mapViewModel.getForegroundEnabledService().observe(this, { locationReceiveState ->
                    if (locationReceiveState) activityMapBinding.locationStatusView.text = getString(R.string.receiving_locations_text)
                    else activityMapBinding.locationStatusView.text = getString(R.string.not_receiving_locations_state)
                })
        }

        val mapFragment = supportFragmentManager.findFragmentById(activityMapBinding.map.id) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapViewModel.setLocationsListener()
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent = Intent(this, ForegroundOnlyLocationService::class.java)
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        if (mapViewModel.getForegroundOnlyLocationServiceBound().value == true) {
            unbindService(foregroundOnlyServiceConnection)
            mapViewModel.setForegroundOnlyLocationServiceBound(false)
        }
        mapViewModel.unregisterSharedPreferencesListener()
        super.onStop()
    }

    private fun getAddress(latLng: LatLng): String {
        val geoCoder = Geocoder(this)
        try {
            val addresses: List<Address>? = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != addresses && addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                return address.getAddressLine(0)
            }
        } catch (e: IOException) {
            Log.e(MapActivity::class.qualifiedName, e.toString())
        }
        return "Not Found"
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener { p0 ->
            Log.v(MapActivity::class.qualifiedName, "Location Marker clicked ${p0?.tag}")
            val timestamp: Long = p0?.tag as Long
            activityMapBinding.locationTimeTextView.text = Date(timestamp).toString()
            false
        }
        mapViewModel.getLocationsVisited().observe(this, { listLocations ->
            Log.v(MapActivity::class.qualifiedName, "Location Update received set size ${listLocations.size}")
            mMap.clear()
            val lastLocation: LocationData? = listLocations.maxWithOrNull(compareBy { it.time })
            listLocations.forEach { locationData ->
                val latLong: LatLng? = locationData.longitude?.let { it1 -> locationData.latitude?.let { it2 -> LatLng(it2, it1) } }
                val markerOptions: MarkerOptions? = latLong?.let { it2 ->
                    MarkerOptions().position(it2).title(getAddress(latLong))
                        .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location)))
                }
                val marker: Marker = mMap.addMarker(markerOptions)
                marker.tag = locationData.time
            }
            if (listLocations.isNotEmpty()) {
                val lastLatLng: LatLng? = lastLocation?.latitude?.let { it1 -> lastLocation.longitude?.let { it2 -> LatLng(it1, it2) } }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, ZOOM_LEVEL.toFloat()))
            }
        })
    }

    @RequiresApi(android.os.Build.VERSION_CODES.Q)
    private fun requestBackgroundLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                Log.v(MapActivity::class.qualifiedName, "Background location Permission approved")
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> backgroundRationalSnackbar.show()
            else -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun requestFineLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                Log.v(MapActivity::class.qualifiedName, "Fine location Permission approved")
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) -> fineLocationRationalSnackbar.show()
            else -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE)
        }
    }

    private fun checkAllPermissions(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        else
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(MapActivity::class.qualifiedName, "Got result from permission request")

        when (requestCode) {
            REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
            REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() -> Log.d(MapActivity::class.qualifiedName, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    Log.v(MapActivity::class.qualifiedName, "All permissions approved")

                }
                else -> {
                    val permissionDeniedExplanation =
                        when (requestCode) {
                            REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE -> R.string.fine_permission_denied_explanation
                            else -> R.string.background_permission_denied_explanation
                        }
                    Snackbar.make(activityMapBinding.root, permissionDeniedExplanation, Snackbar.LENGTH_LONG)
                        .setAction(R.string.settings) {
                            val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = uri
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    internal enum class LocationExtractorType {
        SERVICE_LOCATION_FOREGROUND, BROADCAST_LOCATION_BACKGROUND
    }
}