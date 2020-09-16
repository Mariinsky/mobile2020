package com.example.threadingnetwork

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var fragment: ArFragment
    private lateinit var modelUri: Uri
    private lateinit var fitToScanImageView: ImageView
    private var testRenderable: ViewRenderable? = null
    private var testRenderable2: ViewRenderable? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragment = supportFragmentManager.findFragmentById(R.id.arimage_fragment) as ArFragment
        fitToScanImageView = findViewById<ImageView>(R.id.fit_to_scan_img)
        val renderableFuture = ViewRenderable.builder()
            .setView(this, R.layout.rendtext)
            .build()
        renderableFuture.thenAccept {it -> testRenderable = it }
        val renderableFuture2 = ViewRenderable.builder()
            .setView(this, R.layout.rendtext2)
            .build()
        renderableFuture2.thenAccept {it -> testRenderable2 = it }
        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            frameUpdate()
        }
    }

    private fun frameUpdate() {
        val arFrame = fragment.arSceneView.arFrame
        if (arFrame == null || arFrame.camera.trackingState != TrackingState.TRACKING) {
            return
        }
        val updatedAugmentedImages = arFrame.getUpdatedTrackables(AugmentedImage::class.java)
        updatedAugmentedImages.forEach {
            when(it.trackingState) {
                TrackingState.PAUSED -> {
                    val text = "Detected Image: " + it.name + " - need more info"
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
                }

                TrackingState.STOPPED -> {
                val text = "Tracking stopped: " + it.name
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            }
            }
        }
    }
}


class AImgFrag: ArFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState:
        Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
        return view
    }

    override fun getSessionConfiguration(session: Session?): Config {
        val config = super.getSessionConfiguration(session)
        setupAugmentedImageDatabase(config, session)
        return config
    }

    private fun setupAugmentedImageDatabase(config: Config, session: Session?) {
        val augmentedImageDb: AugmentedImageDatabase
        val assetManager = context!!.assets
        val inputStream1 = assetManager.open(”sofa.jpg")
        val augmentedImageBitmap1 = BitmapFactory.decodeStream(inputStream1)
        val inputStream2 = assetManager.open(”corals.jpg")
        val augmentedImageBitmap2 = BitmapFactory.decodeStream(inputStream2)
        augmentedImageDb = AugmentedImageDatabase(session)
        augmentedImageDb.addImage(”sofa", augmentedImageBitmap1)
        augmentedImageDb.addImage(”corals", augmentedImageBitmap2)
        config.augmentedImageDatabase = augmentedImageDb
    }
}


