package com.example.cube_solver.rubiksCube

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.view.animation.AccelerateInterpolator
import com.example.cube_solver.utils.rotateClockwise
import com.example.cube_solver.utils.rotateCounterclockwise
import com.example.cube_solver.utils.toFlatList
import com.example.cube_solver.utils.toSafeInt
import com.example.cube_solver.utils.toSquareMatrix
import com.google.android.filament.EntityInstance
import com.google.android.filament.TransformManager
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.Mat4
import com.google.android.filament.utils.rotation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RubiksCubeManager(private val transformManager: TransformManager) {

    private val rubiksCubeRepresentation = Rubiks.initialCubieIdentifiers.toMutableList()
    private val representationLock = Mutex()

    companion object {
        const val ONE_MOVE_ROTATION = 90f
    }

    infix fun String.into(cubeFace: RubiksCubeFace): Boolean {
        val matchResult = RubiksCube.numberFindingRegex.find(this) ?: return false
        val leadingNumber = matchResult.groupValues[1].toSafeInt()
        return cubeFace.indexIdentifiers.any { rubiksCubeRepresentation[it] == leadingNumber }
    }

    enum class RubiksCubeFace(val indexIdentifiers: List<Int>) {
        FRONT(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)),
        BACK(listOf(18, 19, 20, 21, 22, 23, 24, 25, 26)),
        UP(listOf(0, 1, 2, 9, 10, 11, 18, 19, 20)),
        BOTTOM(listOf(6, 7, 8, 15, 16, 17, 24, 25, 26)),
        RIGHT(listOf(2, 5, 8, 11, 14, 17, 20, 23, 26)),
        LEFT(listOf(0, 3, 6, 9, 12, 15, 18, 21, 24)),
    }

    private fun getEntityInstancesFromFace(asset: FilamentAsset, face: RubiksCubeFace): List<Int> {
        val entitiesToRotate = mutableListOf<Int>()
        asset.renderableEntities.forEachIndexed { _, renderableEntity ->
            val entityName = asset.getName(renderableEntity)
            if (entityName into face) {
                entitiesToRotate.add(renderableEntity)
            }
        }
        return entitiesToRotate
    }

    fun playMove(
        asset: FilamentAsset,
        move: RubiksMove,
        scope: CoroutineScope,
        onAnimationEnd: suspend CoroutineScope.() -> Unit
    ) {

        val entitiesToRotate = getEntityInstancesFromFace(asset, move.face)

        if (entitiesToRotate.isEmpty()) {
            scope.launch {
                onAnimationEnd()
                return@launch
            }
        }

        val rotatingCubies = entitiesToRotate.map { entity ->
            val inst = transformManager.getInstance(entity)
            val initialTransform = transformManager.getTransform(inst)
            RotatingCubie(entity, initialTransform)
        }

        val animator = ValueAnimator.ofFloat(0f, move.rotationCount * ONE_MOVE_ROTATION).apply {
            duration = 1000L
            interpolator = AccelerateInterpolator()
            var lastAngle = 0f

            addUpdateListener { animation ->
                val currentAngle = animation.animatedValue as Float
                val deltaAngle = currentAngle - lastAngle
                rotateEntities(rotatingCubies, move.axis, deltaAngle)
                lastAngle = currentAngle
            }

            addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    scope.launch {
                        updateCubeRepresentation(move = move)
                        onAnimationEnd()
                        return@launch
                    }
                }

                override fun onAnimationCancel(animation: Animator) {}

                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
        animator.start()
    }

    private fun TransformManager.getTransform(@EntityInstance entityInstance: Int): Mat4 {
        val transformMatrix = FloatArray(16)
        getTransform(entityInstance, transformMatrix)
        return Mat4.of(*transformMatrix)
    }

    private fun rotateEntities(
        rotatingCubies: List<RotatingCubie>,
        axis: Float3,
        deltaAngleSinceLastFrame: Float
    ) {
        rotatingCubies.forEach { cubie ->
            val inst = transformManager.getInstance(cubie.entity)
            val currentTransform = transformManager.getTransform(inst)
            val deltaRotation = rotation(axis, deltaAngleSinceLastFrame)
            val newTransform = currentTransform * deltaRotation
            transformManager.setTransform(inst, newTransform.toFloatArray())
        }
    }

//    private fun rotateEntities(rotatingCubies: List<RotatingCubie>, axis: Float3, angle: Float) {
//        rotatingCubies.forEach { cubie ->
//            val inst = transformManager.getInstance(cubie.entity)
//            val rotation = rotation(axis, angle)
//            val newTransform = cubie.initialTransform * rotation
//            transformManager.setTransform(inst, newTransform.toFloatArray())
//        }
//    }

    private suspend fun updateCubeRepresentation(move: RubiksMove) {
        representationLock.withLock {
            val ids = move.face.indexIdentifiers.map { rubiksCubeRepresentation[it] }
            val matrix = ids.toSquareMatrix(3)
            val rotated = when (move.rubiksMoveType) {
                RubiksMoveType.CLOCKWISE -> matrix.rotateClockwise(move.rotationCount)
                RubiksMoveType.ANTI_CLOCKWISE -> matrix.rotateCounterclockwise(move.rotationCount)
            }
            val flatten = rotated.toFlatList()
            move.face.indexIdentifiers.forEachIndexed { i, id ->
                rubiksCubeRepresentation[id] = flatten[i]
            }
        }
    }

    data class RotatingCubie(val entity: Int, val initialTransform: Mat4)
}



