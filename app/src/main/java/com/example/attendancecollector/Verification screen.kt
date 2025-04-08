package com.example.attendancecollector

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.widget.Toast
import com.google.android.gms.location.Priority



@SuppressLint("MissingPermission")
fun checkLocationAndNavigate(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    navController: NavController,
    //this diastance should be update my the teacher in the database
    distanceThreshold: Double = 2.0
) {
    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        null
    ).addOnSuccessListener { location: Location? ->
        location?.let {
            globalLatitude = it.latitude
            globalLongitude = it.longitude

            val distance = calculateDistance(
                it.latitude, it.longitude,
                fixedLatitude, fixedLongitude
            )

            dis = distance
            loca = "You are $distance meters away from college."

            if (distance <= distanceThreshold) {
                navController.navigate("luv")
            } else {
                Toast.makeText(
                    context,
                    "You are not near the college",
                    Toast.LENGTH_LONG
                ).show()
            }
        } ?: run {
            Toast.makeText(
                context,
                "Could not fetch location",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
