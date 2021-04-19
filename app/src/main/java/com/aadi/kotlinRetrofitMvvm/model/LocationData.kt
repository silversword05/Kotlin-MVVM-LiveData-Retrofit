package com.aadi.kotlinRetrofitMvvm.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class LocationData(val time: Long? = null, val latitude: Double? = null, val longitude: Double? = null) {

    override fun equals(other: Any?): Boolean {
        if(other is LocationData) {
            return other.latitude == this.latitude && other.longitude == this.longitude
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = (latitude?.hashCode() ?: 0)
        result = 31 * result + (longitude?.hashCode() ?: 0)
        return result
    }
}