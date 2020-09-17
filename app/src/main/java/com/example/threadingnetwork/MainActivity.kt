package com.example.threadingnetwork

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.AugmentedFace
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.AugmentedFaceNode
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    private val faceMap = HashMap<AugmentedFace, AugmentedFaceNode>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment
        val sceneView = arFragment.arSceneView
        sceneView.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST

        sceneView.scene.addOnUpdateListener {
            val faceList = sceneView.session!!.getAllTrackables(AugmentedFace::class.java)
            for (face in faceList) {
                if (!faceMap.containsKey(face)) {
                    val faceNode = AugmentedFaceNode(face)
                    faceNode.setParent(sceneView.scene)
                    ViewRenderable.builder().setView(this, R.layout.glasses_view).build()
                        .thenAccept {
                            val glasses = Node()
                            glasses.localScale = Vector3(0.5f, 0.5f, 0.5f)
                            glasses.localPosition = Vector3(0.0f, -0.04f, 0.1f)
                            glasses.setParent(faceNode)
                            glasses.renderable = it
                        }

                    Texture.builder()
                        .setSource(this, R.drawable.face_texture)
                        .build()
                        .thenAccept { texture ->
                            faceNode.faceMeshTexture = texture
                        }
                    faceMap[face] = faceNode
                }
            }

            val faceIterator = faceMap.entries.iterator()
            while (faceIterator.hasNext()) {
                val entry = faceIterator.next()
                val face = entry.key
                if (face.trackingState == TrackingState.STOPPED) {
                    val faceNode = entry.value
                    faceNode.setParent(null)
                    faceNode.children.clear()
                    faceIterator.remove()
                }
            }
        }
    }
}

class FacesFragment: ArFragment() {
    override fun getSessionFeatures(): MutableSet<Session.Feature> {
        return EnumSet.of(Session.Feature.FRONT_CAMERA)
    }

    override fun getSessionConfiguration(session: Session): Config {
        val config = Config(session)
        config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        return config
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val fragmentView = super.onCreateView(inflater, container, savedInstanceState)
        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        return fragmentView!!
    }
}


