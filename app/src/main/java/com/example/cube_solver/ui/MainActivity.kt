package com.example.cube_solver.ui

import com.example.cube_solver.rubiksCube.RubiksCubeViewer
import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Choreographer
import android.view.SurfaceView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.filament.utils.Utils

class MainActivity : AppCompatActivity() {

    companion object {
        init {
            Utils.init()
        }
    }

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
        choreographer = Choreographer.getInstance()
        setContent {
            Scaffold { paddingValues ->
                RubiksCubeScreen(modifier = Modifier.padding(paddingValues))
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Composable
    fun RubiksCubeScreen(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val lifecycleScope = rememberCoroutineScope()

        val surfaceView = remember {
            SurfaceView(context).apply {
                holder.setFormat(PixelFormat.OPAQUE)
            }
        }

        // Setup RubiksCubeViewer only once
        LaunchedEffect(Unit) {
            rubiksCubeViewer = RubiksCubeViewer(
                context = context,
                surfaceView = surfaceView,
                coroutineScope = lifecycleScope
            )
            choreographer.postFrameCallback(frameCallback)
        }

        DisposableEffect(Unit) {
            onDispose {
                choreographer.removeFrameCallback(frameCallback)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidView(
                factory = { surfaceView },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.setOnTouchListener { v, event ->
                        rubiksCubeViewer.onTouch(v, event)
                        true
                    }
                }
            )

            Button(
                onClick = { /* TODO: Handle button click */ },
                modifier = modifier
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 10.dp)

            ) {
                Text(text = "Cube")
            }
        }
    }

}