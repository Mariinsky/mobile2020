package com.example.threadingnetwork

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var scanResults: HashMap<String, ScanResult>? = null
    private var mScanning = false
    private val handler = Handler()

    private val SCAN_PERIOD: Long = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Ultimate BT scanner 0.1"
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        scanResults = HashMap()
        checkPermission()
        button.setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                askBtPermission()
            } else {
                scanLeDevice()
            }
        }
    }

    private fun scanLeDevice() {
        if (!mScanning) {
            handler.postDelayed({
                mScanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                var name = ""
                var strenght = ""
                scanResults?.forEach {
                    val r = it.value
                    name += "${r.device.address}\n"
                    strenght += "${r.rssi} dBm\n"
                }
                adress.text = name
                str.text = strenght
                progress.visibility = View.GONE
            }, SCAN_PERIOD)
            progress.visibility = View.VISIBLE
            mScanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            mScanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }

    private fun addResult(result: ScanResult) {
        scanResults?.set(result.device.address, result)
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            addResult(result)
        }
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                200
            )
            return false
        }
        button.isEnabled = true
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 200) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> button.isEnabled = true
                PackageManager.PERMISSION_DENIED -> {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showAlert(permissions, "LOCATION", 200)
                    }
                }
            }
        }
    }

    private fun askBtPermission() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivity(enableBtIntent)
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


