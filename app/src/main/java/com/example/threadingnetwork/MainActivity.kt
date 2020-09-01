package com.example.threadingnetwork
/**
 *  Automatic "flashlight" the darker the environment the lighter the "box" gets.
 * */
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private lateinit var sensorSubject: PublishSubject<Float>
    private lateinit var updateDelaySubject: PublishSubject<Unit>
    private var shouldStartAnimate = true
    private var MULT = 0f
    private var oldColor = Color.BLACK
    private val animationDuration = 700
    private val unsubscribeOnPause = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Light sensor test"
        init()
    }

    override fun onSensorChanged(evt: SensorEvent?) {
        if (evt?.sensor == lightSensor) {
            sensorSubject.onNext(evt.values[0])
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        lightSensor.also { light ->
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
        }

        sensorSubject
            .subscribe {
                if (shouldStartAnimate) {
                    title = "lx: $it"
                    shouldStartAnimate = false
                    animateColor(lamp, it)
                    updateDelaySubject.onNext(Unit)
                }
            }
            .addTo(unsubscribeOnPause)

        updateDelaySubject
            .throttleLatest(animationDuration.toLong(), TimeUnit.MILLISECONDS)
            .subscribe {
                shouldStartAnimate = true
            }
            .addTo(unsubscribeOnPause)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun animateColor(view: View, f: Float) {
        val newColor = Color.valueOf(1-MULT*f, 1-MULT*f, 0f)
        ObjectAnimator.ofObject(view, "backgroundColor", ArgbEvaluator(), oldColor, newColor.toArgb())
            .setDuration(animationDuration.toLong())
            .start()
        oldColor = newColor.toArgb()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) { }

    private fun init() {
        sensorSubject = PublishSubject.create()
        updateDelaySubject = PublishSubject.create()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            MULT = 1f / lightSensor.maximumRange
        } else {
            Log.i("XXX", "no sensor available")
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        unsubscribeOnPause.clear()
    }

}


