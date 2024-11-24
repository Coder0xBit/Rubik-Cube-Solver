package com.example.cube_solver.ui

import RubiksCubeViewer
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Choreographer
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.filament.utils.Utils

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            Utils.init()
        }
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var choreographer: Choreographer

    private lateinit var rubiksCubeViewer: RubiksCubeViewer

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            rubiksCubeViewer.render(frameTimeNanos)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        surfaceView = SurfaceView(this)
        rubiksCubeViewer = RubiksCubeViewer(
            context = this,
            surfaceView = surfaceView,
            coroutineScope = lifecycleScope
        )
        choreographer = Choreographer.getInstance()

        setContentView(surfaceView)

        surfaceView.setOnTouchListener { v, event ->
            rubiksCubeViewer.onTouch(v, event)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        choreographer.removeFrameCallback(frameCallback)
    }

}