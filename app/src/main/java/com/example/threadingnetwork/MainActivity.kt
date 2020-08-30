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
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus
import org.osmdroid.views.overlay.OverlayItem
import org.osmdroid.views.overlay.Polyline
import java.util.*
import kotlin.collections.ArrayList


const val REQUEST_LOCATION_CODE = 100

class MainActivity : AppCompatActivity() {

    private lateinit var locationClient: FusedLocationProviderClient
    private var firstLocationSet = false
    private lateinit var firstLocation: Location
    private lateinit var geocoder: Geocoder
    private val polyLine = Polyline()
    private val pathPoints = ArrayList<GeoPoint>()
    val items = ArrayList<OverlayItem>()
    private lateinit var locationSubject: PublishSubject<Location>
    private lateinit var startUpdating: PublishSubject<Unit>
    private val disposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_main)
        this.title = "location"
        locationSubject = PublishSubject.create()
        startUpdating = PublishSubject.create()

        geocoder = Geocoder(this, Locale.getDefault())
        locationClient = LocationServices.getFusedLocationProviderClient(this)

        checkPermission()

        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(9.0)


        nappi.setOnClickListener {
            startUpdating.onNext(Unit)
        }

        startUpdating
            .observeOn(Schedulers.io())
            .subscribe {
                locationClient.requestLocationUpdates(
                    createLocationRequest(),
                    locationCallback(),
                    Looper.getMainLooper()
                )
            }.addTo(disposable)

        locationSubject
            .subscribe {
                map.overlays.clear()
                map.controller.setCenter(GeoPoint(it.latitude, it.longitude))
                updateMap(it)
            }.addTo(disposable)
    }


    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_LOCATION_CODE
            )
            return false
        }
        nappi.isEnabled = true
        return true
    }

    private fun locationCallback() = object : LocationCallback() {
        @SuppressLint("SetTextI18n")
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                locationSubject.onNext(location)
            }
        }
    }

    private fun updateMap(location: Location) {
        if (!firstLocationSet) {
            firstLocation = location
            val startingPoint = GeoPoint(
                location.latitude,
                location.longitude
            )
            items.add(
                OverlayItem(
                    "Location",
                    "${location.latitude} ${location.longitude}",
                    startingPoint
                )
            )

            map.controller.setCenter(
                startingPoint
            )
            firstLocationSet = true
        }
        val currentPoint = GeoPoint(
            location.latitude,
            location.longitude
        )
        val mOverlay = ItemizedOverlayWithFocus(
            items,
            object : OnItemGestureListener<OverlayItem?> {
                override fun onItemSingleTapUp(index: Int, item: OverlayItem?): Boolean {
                    //do something
                    return true
                }

                override fun onItemLongPress(index: Int, item: OverlayItem?): Boolean {
                    return false
                }
            }, this@MainActivity
        )
        map.overlays.clear()
        pathPoints.add(currentPoint)
        mOverlay.setFocusItemsOnTap(true)
        polyLine.setPoints(pathPoints)
        map.overlays.add(polyLine)
        map.overlays.add(mOverlay)
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
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showAlert(permissions, "LOCATION", REQUEST_LOCATION_CODE)
                    }
                }
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

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }
}


