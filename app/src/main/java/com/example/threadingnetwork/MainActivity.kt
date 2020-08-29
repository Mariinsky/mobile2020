package com.example.threadingnetwork

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
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
    }
}

class Conn(
        mHand: Handler,
        val url: String,
        val fname: String,
        val lname: String,) : Runnable {

    private val myHandler = mHand

    override fun run() {
        try {
            val url = URL (url)
            val conn = url.openConnection() as HttpURLConnection

            conn.requestMethod = "GET"
            conn.do
        } catch (e: Exception) {
            //TODO: handle
        }
    }
}
