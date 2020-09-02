package com.example.threadingnetwork

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler


class MainActivity : AppCompatActivity() {
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var btManager: BluetoothManager
    private var scanResults: HashMap<String, ScanResult>? = null
    private var scanCallBack: BtScanCallBack? = null
    private var btScanner : BluetoothLeScanner? = null
    private val SCAN_PERIOD: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter

    }

    private fun startScan() {
        scanResults = HashMap()
        scanCallBack = BtScanCallBack()
        btScanner = btAdapter.bluetoothLeScanner

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        val filter: List<ScanFilter>? = null

        val mHandler = Handler()
        mHandler.postDelayed( {stopScan() })

    }

    private inner class BtScanCallBack : ScanCallback() {

    }


}


