import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import com.example.cube_solver.config.ApplicationConfig.defaultCubePosition
import com.example.cube_solver.rubiksCube.CubeFace
import com.example.cube_solver.rubiksCube.ModelLoader
import com.example.cube_solver.rubiksCube.RubiksCubeManager
import com.example.cube_solver.utils.isNotEmptyOrNull
import com.example.cube_solver.utils.log
import com.google.android.filament.utils.Manipulator
import com.google.android.filament.utils.ModelViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class RubiksCubeViewer(
    val context: Context,
    val surfaceView: SurfaceView,
    val coroutineScope: CoroutineScope
) {

    private var modelViewer: ModelViewer
    private var modelLoader: ModelLoader
    private val rubiksCubeManager: RubiksCubeManager
    private var cameraManipulator: Manipulator = Manipulator.Builder()
        .targetPosition(
            defaultCubePosition.x,
            defaultCubePosition.y,
            defaultCubePosition.z
        )
        .viewport(surfaceView.width, surfaceView.height)
        .build(Manipulator.Mode.ORBIT)

    private val gestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                handleClick(x = event.x, y = event.y)
                return true
            }
        })
    }

    init {

        modelViewer = ModelViewer(surfaceView, manipulator = cameraManipulator)
        rubiksCubeManager =
            RubiksCubeManager(modelViewer = modelViewer, coroutineScope = coroutineScope)
        modelLoader = ModelLoader(
            context = context,
            modelViewer = modelViewer
        )

        modelLoader.loadGlb("GrayRubiksCube")
        modelLoader.loadSkyBox("studio")

        coroutineScope.launch {
            modelLoader.makeFrontRed()
            modelLoader.makeUpYellow()
            rubiksCubeManager.test()
        }
    }

    fun handleClick(x: Float, y: Float) {
        val finalX = x.toInt()
        val finalY = (surfaceView.height - y).toInt()

        modelViewer.view.pick(finalX, finalY, Executors.newSingleThreadExecutor()) { result ->
            modelViewer.asset?.let { asset ->
                val entityName = asset.getName(result.renderable)
                entityName.log()
                if (entityName != null &&
                    entityName.isNotEmptyOrNull() &&
                    entityName.matches(CubeFace.BLACK_MESHES).not()
                ) {
                    modelLoader.applyMaterial(
                        renderable = result.renderable,
                        material = modelLoader.redMaterial
                    )
                }
            }
        }
    }

    fun onTouch(v: View, event: MotionEvent) {
        modelViewer.onTouch(v, event)
        gestureDetector.onTouchEvent(event)
    }

    fun render(frameTimeNanos: Long) {
        modelViewer.render(frameTimeNanos)
    }

}

