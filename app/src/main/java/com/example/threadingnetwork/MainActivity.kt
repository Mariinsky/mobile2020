package com.example.threadingnetwork

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.teksiview.view.*
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var heartRateSubject: PublishSubject<String>
    private val compositeDisposable= CompositeDisposable()
    private var scanResults: HashMap<String, ScanResult>? = null
    private var keys = mutableListOf<String>()
    private var mScanning = false
    private val handler = Handler()

    private val SCAN_PERIOD: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Ultimate BT scanner 0.1"
        heartRateSubject = PublishSubject.create()
        scanResults = HashMap()
        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter(this, keys, scanResults!!, heartRateSubject)
        recyclerView = lista.apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        checkPermission()
        button.setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                askBtPermission()
            } else {
                scanResults!!.clear()
                scanLeDevice()
            }
        }

        heartRateSubject
            .subscribe {
                heartRate.text = "$it bpm"
            }
            .addTo(compositeDisposable)
    }

    private fun scanLeDevice() {
        if (!mScanning) {
            handler.postDelayed({
                mScanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                progress.visibility = View.GONE
                scanResults?.keys?.forEach {
                    keys.add(it)
                }
                viewAdapter.notifyDataSetChanged()
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

class MyAdapter(
    private val context: Context,
    private val keys: MutableList<String>,
    private val data: HashMap<String, ScanResult>,
    private val subject: PublishSubject<String>
) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(val v: View) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {

        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.teksiview, parent, false) as View

        return MyViewHolder(textView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val r = data[keys[position]]
        if (r != null) {
            if (!r.isConnectable) {
                holder.v.name.isEnabled = false
            }
            holder.v.name.text = "${r.device.name} ${r.device.address}"
            holder.v.dbm.text = "${r.rssi} dBm"
            holder.v.setOnClickListener {
                r.device.connectGatt(context, false, GattClientCallback(subject))
            }
        }
    }

    override fun getItemCount() = keys.size
}

class GattClientCallback(val hearRateSubject: PublishSubject<String>): BluetoothGattCallback() {

    val HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D)
    val HEART_RATE_MEASUREMENT_CHAR_UUID = convertFromInteger(0x2A37)
    val CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902)

    private fun convertFromInteger(i: Int): UUID {
        val MSB = 0x0000000000001000L
        val LSB = -0x7fffff7fa064cb05L
        val value = (i and -0x1).toLong()
        return UUID(MSB or (value shl 32), LSB)
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        super.onConnectionStateChange(gatt, status, newState)
        Log.i("DBG", status.toString() + " " + newState.toString())
        if (status == BluetoothGatt.GATT_FAILURE) {
            Log.d("DBG", "GATT connection failure")
            return
        } else if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d("DBG", "GATT connection success")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("DBG", "Connected GATT service")
                gatt?.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("DBG", "Disconnected GATT service")
            }
            return
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (status != BluetoothGatt.GATT_SUCCESS) {
            return
        }
        gatt?.services?.forEach {
            if (it.uuid == HEART_RATE_SERVICE_UUID) {
                gatt.setCharacteristicNotification(it.characteristics[0], true)
                val descriptor = it.characteristics[0].getDescriptor(
                    CLIENT_CHARACTERISTIC_CONFIG_UUID
                ).apply {
                    value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                }
                gatt.writeDescriptor(descriptor)
            }
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val heartRate = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
        hearRateSubject.onNext(heartRate.toString())
    }

}
