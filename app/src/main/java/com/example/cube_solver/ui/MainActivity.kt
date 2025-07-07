package com.example.cube_solver.ui

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Choreographer
import android.view.SurfaceView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cube_solver.config.ApplicationConfig
import com.example.cube_solver.config.ApplicationConfig.enableFreeCameraMovement
import com.example.cube_solver.rubiksCube.RubiksCubeViewer
import com.example.cube_solver.rubiksCube.RubiksMove
import com.google.android.filament.utils.Utils
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        init {
            Utils.init()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold { paddingValues ->
                RubiksCubeScreen(paddingValues)
            }
        }
    }
}


@SuppressLint("ClickableViewAccessibility")
@Composable
fun RubiksCubeScreen(paddingValue: PaddingValues = PaddingValues()) {
    val context = LocalContext.current

    val surfaceView = remember {
        SurfaceView(context).apply {
            holder.setFormat(PixelFormat.OPAQUE)
        }
    }

    val rubiksCubeViewer = remember {
        RubiksCubeViewer(
            context = context,
            surfaceView = surfaceView
        )
    }

    val choreographer = remember { Choreographer.getInstance() }

    val frameCallback = remember {
        object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                choreographer.postFrameCallback(this)
                rubiksCubeViewer.render(frameTimeNanos)
            }
        }
    }

    val viewModel: MainViewModel =
        viewModel(factory = MainViewModelFactory(rubiksCubeViewer.assetViewer))

    LaunchedEffect(Unit) {
        rubiksCubeViewer.initialize()
        choreographer.postFrameCallback(frameCallback)
    }

    DisposableEffect(Unit) {
        onDispose {
            choreographer.removeFrameCallback(frameCallback)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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

        var showSlider by remember { mutableStateOf(rubiksCubeViewer.assetViewer.isFreeMovement.not()) }
        Column(
            modifier = Modifier
                .padding(paddingValue.calculateTopPadding())
                .padding(top = 20.dp)
                .align(Alignment.TopStart)
        ) {
            Row {
                Button(
                    modifier = Modifier.padding(end = 5.dp),
                    onClick = {
                        rubiksCubeViewer.assetViewer.asset?.let { rubiksCube ->
                            viewModel.startSolving(rubiksCube)
                        }
                    }
                ) {
                    Text("Test")
                }

                Button(
                    onClick = {
                        showSlider = !showSlider
                        rubiksCubeViewer.assetViewer.isFreeMovement = showSlider.not()
                    }
                ) {
                    val label = if (showSlider) {
                        "Not Free Movement"
                    } else {
                        "Free Movement"
                    }
                    Text(label)
                }
            }

            if (showSlider) {
                var sliderX by remember { mutableFloatStateOf(0f) }
                var sliderY by remember { mutableFloatStateOf(0f) }
                var sliderZ by remember { mutableFloatStateOf(0f) }
                Slider(
                    value = sliderX,
                    onValueChange = {
                        sliderX = it
                        rubiksCubeViewer.assetViewer.updateAngle(angleX = sliderX.toDouble())
                    },
                    valueRange = 0f..360f,
                    steps = 0
                )
                Slider(
                    value = sliderY,
                    onValueChange = {
                        sliderY = it
                        rubiksCubeViewer.assetViewer.updateAngle(angleY = sliderY.toDouble())
                    },
                    valueRange = 0f..360f,
                    steps = 0
                )
                Slider(
                    value = sliderZ,
                    onValueChange = {
                        sliderZ = it
                        rubiksCubeViewer.assetViewer.updateAngle(angleZ = sliderZ.toDouble())
                    },
                    valueRange = 0f..360f,
                    steps = 0
                )
            }

            LazyRow {
                items(RubiksMove.entries.size) { index ->
                    Button(
                        modifier = Modifier.padding(end = 5.dp),
                        onClick = {
                            rubiksCubeViewer.assetViewer.asset?.let { rubiksCube ->
                                viewModel.playMove(rubiksCube, RubiksMove.entries[index])
                            }
                        }
                    ) {
                        Text(RubiksMove.entries[index].value)
                    }
                }
            }
        }

        ColorPicker(
            modifier = Modifier
                .padding(paddingValue.calculateBottomPadding())
                .padding(bottom = 50.dp)
                .align(Alignment.BottomCenter)
        ) {
            rubiksCubeViewer.colorToBeAppliedOnCubie = it
        }
    }
}

