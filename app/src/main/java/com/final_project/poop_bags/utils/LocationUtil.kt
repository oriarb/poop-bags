package com.final_project.poop_bags.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class LocationUtil(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val TAG = "com.final_project.poop_bags.utils.LocationUtil"

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): Flow<Location?> = flow {
        try {
            val location = fusedLocationClient.lastLocation.await()
            emit(location)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting location", e)
            emit(null)
        }
    }

    @SuppressLint("MissingPermission")
    fun getDistanceFromCurrentLocation(latitude: Double, longitude: Double): Flow<Float?> = flow {
        try {
            val location = fusedLocationClient.lastLocation.await()
            val distance = FloatArray(1)
            Location.distanceBetween(location.latitude, location.longitude, latitude, longitude, distance)
            emit(distance[0])
        } catch (e: Exception) {
            Log.e(TAG, "Error getting distance", e)
            emit(null)
        }
    }
}
