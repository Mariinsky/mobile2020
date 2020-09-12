package com.example.threadingnetwork

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers.io
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    lateinit var recFile: File
    private var isRecording = false
    private val startPlaying = PublishSubject.create<Unit>()
    private val dispose = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission()

        record.setOnClickListener {
            audioRecorder()
            recording_text.visibility = View.VISIBLE
            record.isEnabled = false
            stop.isEnabled = true
            play.isEnabled = false
        }

        stop.setOnClickListener {
            isRecording = false
            recording_text.visibility = View.GONE
            record.isEnabled = true
            stop.isEnabled = false
            play.isEnabled = true
        }

        play.setOnClickListener { startPlaying.onNext(Unit) }

        startPlaying
            .observeOn(io())
            .subscribe {
                audioPlayer(FileInputStream(recFile))
            }.addTo(dispose)
    }

    private fun audioRecorder() {
        val file = "recording.raw"
        val dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)

        try {
            recFile = File("$dir/$file")
        } catch (e: Exception) {
            Log.d("XXX", "creating file error: ${e.message}")
        }

        try {
            val dataOutputStream = DataOutputStream(BufferedOutputStream(FileOutputStream(recFile)))

            val minBufferSize =
                AudioRecord.getMinBufferSize(
                    44100,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

            val aFormat =
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(44100)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()

            val recorder =
                AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.MIC)
                    .setAudioFormat(aFormat)
                    .setBufferSizeInBytes(minBufferSize)
                    .build()

            val audioData = ByteArray(minBufferSize)

            GlobalScope.launch(Dispatchers.IO) {
                isRecording = true
                recorder.startRecording()
                while (isRecording) {
                    val numofBytes = recorder.read(audioData, 0, minBufferSize)
                    if (numofBytes > 0) {
                        dataOutputStream.write(audioData)
                    }
                }
                recorder.stop()
                dataOutputStream.close()
            }

        } catch (e: Exception) {
            Log.d("XXX", "Audio recording error: ${e.printStackTrace()}")
        }
    }

    private fun audioPlayer(stream: InputStream) {
        val minBufferSize =
            AudioTrack.getMinBufferSize(
                44100, AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
        val aBuilder = AudioTrack.Builder()

        val aAttr: AudioAttributes =
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

        val aFormat: AudioFormat =
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()

        val track =
            aBuilder.setAudioAttributes(aAttr)
                .setAudioFormat(aFormat)
                .setBufferSizeInBytes(minBufferSize)
                .build()

        track.setVolume(0.8f)
        track.play()

        var i = 0
        val buffer = ByteArray(minBufferSize)
        try {
            i = stream.read(buffer, 0, minBufferSize)
            while (i != -1) {
                track.write(buffer, 0, i)
                i = stream.read(buffer, 0, minBufferSize)
            }
        } catch (e: IOException) {
            Log.d("XXX", e.message.toString())
        }
        try {
            stream.close()
        } catch (e: IOException) {
            Log.d("XXX", e.message.toString())
        }
        track.stop()
        track.release()
    }

    private fun askPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1
            )
        }
    }
}





