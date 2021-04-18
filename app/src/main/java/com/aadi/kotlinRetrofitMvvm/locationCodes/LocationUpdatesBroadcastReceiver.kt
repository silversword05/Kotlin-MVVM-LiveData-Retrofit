/*
 * Copyright (C) 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aadi.kotlinRetrofitMvvm.locationCodes

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aadi.kotlinRetrofitMvvm.viewmodel.MapViewModel
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import com.google.firebase.database.DatabaseError
import java.util.*

/**
 * Receiver for handling location updates.
 *  https://github.com/android/location-samples/tree/master/LocationUpdatesBackgroundKotlin  REFER THIS CODE
 */
class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(LocationUpdatesBroadcastReceiver::class.qualifiedName, "onReceive() context:$context, intent:$intent")

        if (intent.action == ACTION_PROCESS_UPDATES) {
            // Checks for location availability changes.
            LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                if (!locationAvailability.isLocationAvailable) {
                    Log.d(LocationUpdatesBroadcastReceiver::class.qualifiedName, "Location services are no longer available!")
                }
            }
            LocationResult.extractResult(intent)?.let { locationResult ->
                val location = locationResult.lastLocation
                Log.v(LocationUpdatesBroadcastReceiver::class.qualifiedName, "Location received ${location.latitude} ${location.longitude} at ${Date(location.time)}")
                Log.v(LocationUpdatesBroadcastReceiver::class.qualifiedName, "Foreground state ${isAppInForeground(context)}")

                val geoFire: GeoFire = MapViewModel.getGeoFireInstance()
                geoFire.setLocation(location.time.toString(), GeoLocation(location.latitude, location.longitude)) { key: String?, error: DatabaseError? ->
                    error?.let { throwable ->
                        Log.e(LocationUpdatesBroadcastReceiver::class.qualifiedName, "Location update failed in database", throwable.toException())
                    } ?: run {
                        Log.d(LocationUpdatesBroadcastReceiver::class.qualifiedName, "Location updated successfully $key")
                    }

                }
            }
        }
    }

    // Note: This function's implementation is only for debugging purposes. If you are going to do
    // this in a production app, you should instead track the state of all your activities in a
    // process via android.app.Application.ActivityLifecycleCallbacks's
    // unregisterActivityLifecycleCallbacks(). For more information, check out the link:
    // https://developer.android.com/reference/android/app/Application.html#unregisterActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks
    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        appProcesses.forEach { appProcess ->
            if (appProcess.importance ==
                ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                appProcess.processName == context.packageName) {
                return true
            }
        }
        return false
    }

    companion object {
        const val ACTION_PROCESS_UPDATES = "com.aadi.kotlinRetrofitMvvm.action.PROCESS_UPDATES"
    }
}
