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
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.concurrent.Executor
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay


class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AttendanceCollectorTheme {
                MainScreen()

            }
        }
    }
}

// Define Bottom Navigation Items
sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Msg : BottomNavItem("msg", "Message", Icons.Filled.Email)
    object MarkAttendance : BottomNavItem("markAttendance", "Attendance", Icons.Filled.CheckCircle)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { MyBottomNavigation(navController) }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.Msg.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Msg.route) { MessageScreen() }
            composable(BottomNavItem.MarkAttendance.route) { AttendanceScreen(navController) }
            composable("mark") { MarkAttendanceScreen() }
            composable("luv") { AttendanceScreenp(navController) }

        }
    }
}

@Composable
fun MyBottomNavigation(navController: NavController) {
    val items = listOf(BottomNavItem.Msg, BottomNavItem.MarkAttendance)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
/*
@Composable
fun MessageScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column()
        {
            Text(text = "Message Screen", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Mark Attendance")

            }


        }
    }
}
*/
@Composable
fun MessageScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
   // val activity = context as? FragmentActivity
    var locationText by remember { mutableStateOf("Location: Not Available") }
   // var locatioText1 by remember { mutableStateOf("Location: Not Available") }


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getLocationt(context, fusedLocationClient) { latLng ->
                globalLatitude = latLng.first
                globalLongitude = latLng.second
                locationText = "Lat: ${latLng.first}, Lon: ${latLng.second}"
            }
        } else {
            locationText = "Permission Denied"
        }
    }

    fun requestLocation() {
        if (hasLocationPermissions(context)) {
            getLocationt(context, fusedLocationClient) { latLng ->
                globalLatitude = latLng.first
                globalLongitude = latLng.second
                locationText = "Lat: ${latLng.first}, Lon: ${latLng.second}"
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Message Screen", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { requestLocation() }) {
                Text(text = "Get & Store Location")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = locationText)
            Text(text = "Lat: $globalLatitude, Lon: $globalLongitude")


            if (globalLatitude != null && globalLongitude != null) {
                val distance = calculateDistance(
                    globalLatitude!!, globalLongitude!!,
                    fixedLatitude, fixedLongitude
                )
                dis = distance
                loca = "You are $distance m away from college."
            }

            Text(text = loca)

        }
    }
}


// Sample Data for Attendance List
data class AttendanceItem(val teacherName: String, val subjectName: String)

val sampleAttendanceList = listOf(
    AttendanceItem("Mr. Sharma", "Mathematics"),
    AttendanceItem("Ms. Kapoor", "Physics"),
    AttendanceItem("Dr. Singh", "Computer Science"),
    AttendanceItem("Mrs. Mehta", "Chemistry"),
    AttendanceItem("Mr. Reddy", "English")
)

@Composable
fun AttendanceScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Mark Attendance",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(sampleAttendanceList) { item ->
                AttendanceCard(item, navController)

            }
        }
    }
}

@Composable
fun AttendanceCard(attendanceItem: AttendanceItem, navController: NavController) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                //put condition here
                if(dis > 5) Toast.makeText(context, "you are $dis so farr..", Toast.LENGTH_LONG).show()
                else{
                navController.navigate("luv") // Navigate to the attendance screen
                     }
            },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Teacher: ${attendanceItem.teacherName}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Subject: ${attendanceItem.subjectName}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MarkAttendanceScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text(text = "Mark Attendance Screen", style = MaterialTheme.typography.headlineMedium)
    }

}




//my coe.....................   //main logic.............................

@Composable
fun AttendanceScreenp(navController: NavController) {
    val context = LocalContext.current // Get the context
    LaunchedEffect(Unit) {
        delay(10_000) // Wait for 10 seconds
        navController.popBackStack() // Navigate back
    }


    // Call your function that requires context
    AttendanceScreent(context)
}

@Composable
fun AttendanceScreent(context: Context) {
    var authStatus by remember { mutableStateOf("Click to mark attendance") }
    var location by remember { mutableStateOf("Location: Not Available") }

    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val activity = context as? FragmentActivity

    // Permission Request Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getLocation(context, locationClient) { loc -> location = loc }
        } else {
            location = "Location permission denied"
        }
    }

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
                        if (hasLocationPermissions(context)) {
                            getLocation(context, locationClient) { loc -> location = loc }
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
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

// Function to check if permissions are already granted
fun hasLocationPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

fun authenticateUser(activity: FragmentActivity, onResult: (String) -> Unit) {
    val biometricManager = BiometricManager.from(activity)
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            val executor: Executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(
                activity,
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
// Helper function to check if location services are enabled
fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

@SuppressLint("MissingPermission")
fun getLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (String) -> Unit
) {
    // Check if location services are enabled
    if (!isLocationEnabled(context)) {
        onLocationReceived("Please turn on location services")
        // Optionally, you can open the location settings:
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        return
    }

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        onLocationReceived("Permission not granted")
        return
    }

    fusedLocationClient.getCurrentLocation(
        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
        null // You can pass a CancellationToken if needed
    ).addOnSuccessListener { location ->
        if (location != null) {
            onLocationReceived("Location: ${location.latitude}, ${location.longitude}")
        } else {
            onLocationReceived("Location not found")
        }
    }
}

@SuppressLint("MissingPermission")
fun getLocationt(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Pair<Double, Double>) -> Unit
) {
    if (!isLocationEnabled(context)) {
        Toast.makeText(context, "Please enable location services", Toast.LENGTH_SHORT).show()
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        return
    }

    fusedLocationClient.getCurrentLocation(
        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
        null
    ).addOnSuccessListener { location ->
        if (location != null) {
            onLocationReceived(Pair(location.latitude, location.longitude))
        } else {
            Toast.makeText(context, "Location not found", Toast.LENGTH_SHORT).show()
        }
    }
}

