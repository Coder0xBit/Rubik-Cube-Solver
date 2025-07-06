package com.example.cube_solver.rubiksCube

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import com.example.cube_solver.config.ApplicationConfig.defaultObjectPosition
import com.example.cube_solver.ui.RubiksCubeColors
import com.example.cube_solver.utils.isNotEmptyOrNull
import com.example.cube_solver.utils.log
import com.google.android.filament.Entity
import com.google.android.filament.Material
import java.util.concurrent.Executors

class RubiksCubeViewer(
    val context: Context, val surfaceView: SurfaceView
) {
    val assetViewer = AssetViewer(surfaceView)
    private val resourceLoader = ResourceLoader(context = context, assetViewer = assetViewer)

    var colorToBeAppliedOnCubie: RubiksCubeColors = RubiksCubeColors.RED
    private val colorCountMap: MutableMap<RubiksCubeColors, Int> = mutableMapOf(
        RubiksCubeColors.RED to 0,
        RubiksCubeColors.GREEN to 0,
        RubiksCubeColors.BLUE to 0,
        RubiksCubeColors.YELLOW to 0,
        RubiksCubeColors.WHITE to 0,
        RubiksCubeColors.ORANGE to 0,
    )

    private val gestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                handleClick(x = event.x, y = event.y)
                return true
            }
        })
    }

    fun initialize() {
        val indirectLight = resourceLoader.loadIndirectLight("cloud")
        assetViewer.scene.indirectLight = indirectLight

        val skybox = resourceLoader.loadSkyBox("cloud")
        assetViewer.scene.skybox = skybox

        val buffer = resourceLoader.loadGlb("GrayRubiksCube")
        assetViewer.loadModelGlb(buffer)
        assetViewer.transformToUnitCube(centerPoint = defaultObjectPosition)
    }

    fun handleClick(x: Float, y: Float) {
        val finalX = x.toInt()
        val finalY = (surfaceView.height - y).toInt()

        assetViewer.view.pick(finalX, finalY, Executors.newSingleThreadExecutor()) { result ->
            assetViewer.asset?.let { asset ->
                val entityName = asset.getName(result.renderable)
                entityName.log()
                if (entityName != null &&
                    entityName.isNotEmptyOrNull() &&
                    entityName.matches(CubeFace.BLACK_MESHES).not()
                ) {
                    colorCountMap[colorToBeAppliedOnCubie]?.let {
                        if (it < 9) {
                            colorCountMap[colorToBeAppliedOnCubie] = it + 1
                            applyMaterial(
                                renderable = result.renderable,
                                material = resourceLoader.getMaterial(colorToBeAppliedOnCubie)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun applyMaterial(@Entity renderable: Int, material: Material) {
        val renderableInstance = assetViewer.engine.renderableManager.getInstance(renderable)
        assetViewer.engine.renderableManager.setMaterialInstanceAt(
            renderableInstance, 0, material.defaultInstance
        )
    }

    fun makeFrontRed() {
        assetViewer.asset?.let { asset ->
            asset.renderableEntities.forEachIndexed { index, renderableEntity ->
                val entityName = asset.getName(renderableEntity)
                if (entityName into CubeFace.FRONT) {
                    applyMaterial(
                        renderable = renderableEntity, material = resourceLoader.redMaterial
                    )
                }
            }
        }
    }

    fun makeUpYellow() {
        assetViewer.asset?.let { asset ->
            asset.renderableEntities.forEachIndexed { index, renderableEntity ->
                val entityName = asset.getName(renderableEntity)
                if (entityName into CubeFace.UP) {
                    applyMaterial(
                        renderable = renderableEntity, material = resourceLoader.yellowMaterial
                    )
                }
            }
        }
    }

    fun onTouch(v: View, event: MotionEvent) {
        assetViewer.onTouch(v, event)
        gestureDetector.onTouchEvent(event)
    }

    fun render(frameTimeNanos: Long) {
        assetViewer.render(frameTimeNanos)
    }
}

