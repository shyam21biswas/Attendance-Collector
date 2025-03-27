package com.example.attendancecollector

import android.os.Bundle

import androidx.activity.compose.setContent

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.example.attendancecollector.ui.theme.AttendanceCollectorTheme

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AttendanceCollectorTheme {
                AttendanceScreen(this)

            }
        }
    }
}




@Composable
fun AttendanceScreen(context: Context) {
    var authStatus by remember { mutableStateOf("Click to mark attendance") }
    var location by remember { mutableStateOf("Location: Not Available") }

    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val activity = context as? FragmentActivity // Get the activity from context


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = authStatus, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = location, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            activity?.let { act ->
                authenticateUser(act) { result ->
                    authStatus = result
                    if (result == "Attendance Confirmed!") {
                        getLocation(context, locationClient) { loc -> location = loc }
                    }
                }
            } ?: run {
                authStatus = "Error: Activity not found"
            }
        }) {
            Text(text = "Mark Attendance")
        }
    }
}


fun authenticateUser(activity: FragmentActivity, onResult: (String) -> Unit) {
    val biometricManager = BiometricManager.from(activity)
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            val executor: Executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(
                activity,  // Pass activity instead of context
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        onResult("Attendance Confirmed!")
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onResult("Authentication Failed. Try Again.")
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        onResult("Error: $errString")
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Confirm Attendance")
                .setSubtitle("Use fingerprint to mark attendance")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
        else -> onResult("Biometric authentication not supported on this device.")
    }
}


@SuppressLint("MissingPermission")
fun getLocation(context: Context, fusedLocationClient: FusedLocationProviderClient, onLocationReceived: (String) -> Unit) {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        onLocationReceived("Permission not granted")
        return
    }

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            onLocationReceived("Location: ${location.latitude}, ${location.longitude}")
        } else {
            onLocationReceived("Location not found")
        }
    }
}