package com.example.threadingnetwork

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.roundToInt

const val REQUEST_LOCATION_CODE = 100

class MainActivity : AppCompatActivity() {

    private lateinit var locationClient: FusedLocationProviderClient
    private var firstLocationSet = false
    private lateinit var firstLocation: Location
    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.title = "location"
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())
        checkPermission()

        nappi.setOnClickListener {
                locationClient.requestLocationUpdates(
                    createLocationRequest(),
                    locationCallback(),
                    Looper.getMainLooper()
                )
            }
        }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION ), REQUEST_LOCATION_CODE)
            return false
        }
        nappi.isEnabled = true
        return true
    }

    private fun locationCallback() = object : LocationCallback() {
        @SuppressLint("SetTextI18n")
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations){
                if (!firstLocationSet) {
                    firstLocation = location
                    firstLocationSet = true
                }
                val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                loccis.text =  "${address[0].locality}\n${address[0].getAddressLine(0)}"
                distance.text = "Distance from origin: ${location.distanceTo(firstLocation).roundToInt() / 1000}km"
            }
        }
    }

    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_CODE) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> nappi.isEnabled = true
                PackageManager.PERMISSION_DENIED -> {
                    Log.i("XXX", permissions.toString())
                    if (shouldShowRequestPermissionRationale( Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showAlert(permissions, "LOCATION", REQUEST_LOCATION_CODE)
                    }}
            }
        }
    }

    private fun showAlert(permissions: Array<String>, name: String, requestCode: Int) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Persmission to $name is need to use this app")
            setTitle("Permission needed")
            setPositiveButton("I understand") { _, _ ->
                ActivityCompat.requestPermissions(this@MainActivity, permissions, requestCode)
                }
            }
            .create()
            .show()
    }
}


