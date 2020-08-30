package com.example.threadingnetwork

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var locationClient: FusedLocationProviderClient
    private var firstLocationSet = false
    private lateinit var firstLocation: Location

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.title = "location"

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    if (!firstLocationSet) {
                        firstLocation = location
                        firstLocationSet = true
                    }
                    loccis.text =  location.longitude.toString() + " " + location.latitude.toString()
                    distance.text = location.distanceTo(firstLocation).toString()
                    if(firstLocation != null) {
                        Log.i("XXX", firstLocation.distanceTo(location).toString())
                    }
                }
            }
        }

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        nappi.setOnClickListener {
            locationClient.requestLocationUpdates(createLocationRequest(),locationCallback, Looper.getMainLooper() )
        }




    }

    private fun createLocationRequest(): LocationRequest? {
        return LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }


    private fun checkLastLocation(){
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION ), 1)
            return
        }
        getLastLocation()

    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLastLocation() {
        locationClient.lastLocation.addOnSuccessListener(this) { location : Location? ->
            if (location != null) {
                loccis.text = location.latitude.toString() + " " + location.longitude.toString()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION ), 1)
        }
    }


}


