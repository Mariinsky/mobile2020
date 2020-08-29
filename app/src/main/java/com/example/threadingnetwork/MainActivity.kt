package com.example.threadingnetwork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isNetworkAvailable()) {
            try {
                val url = URL("https://racecarsdirect.com/content/UserImages/100877/550844.jpg")
                button1.isEnabled = true
                button1.setOnClickListener {
                    lifecycleScope.launch(Dispatchers.Main) {
                        val img = getImg(url)
                        showImg(img)
                    }
                }
            } catch (e:Exception){
                // TODO:
                Log.i("XXX", e.toString())
            }
        }
    }

    private fun showImg(serverImg: Bitmap) {
        kuva.setImageBitmap(serverImg)
    }

    private suspend fun getImg(url: URL): Bitmap = withContext(Dispatchers.IO) {
        val imgStream = url.openConnection().getInputStream()
        return@withContext BitmapFactory.decodeStream(imgStream)
    }

    private fun isNetworkAvailable(): Boolean =
        (this.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager).isDefaultNetworkActive
}


