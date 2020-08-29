package com.example.threadingnetwork

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val mHandler: Handler = object :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0) {
                tekstii.text = msg.obj.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isNetworkAvailable()) {
            button1.isEnabled = true
            button1.setOnClickListener {
                val myRunnable = Conn(mHandler, "https://www.w3.org/TR/PNG/iso_8859-1.txt")
                val myThread = Thread(myRunnable)
                myThread.start()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean =
        (this.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager).isDefaultNetworkActive
}

class Conn(
    mHand: Handler,
    val url: String,
) : Runnable {

    private val myHandler = mHand

    override fun run() {
        try {
            val url = URL(url)
            val conn = url.openConnection() as HttpURLConnection

            conn.requestMethod = "GET"

            val iStream: InputStream = conn.inputStream
            val text = iStream.bufferedReader().use { it.readText() }

            val result = StringBuilder()
            result.append(text)

            val msg = myHandler.obtainMessage()
            msg.what = 0
            msg.obj = result.toString()
            myHandler.sendMessage(msg)

        } catch (e: Exception) {
            //TODO: handle
        }
    }
}
