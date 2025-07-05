package com.example.cube_solver.rubiksCube

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import com.example.cube_solver.utils.rotateClockwise
import com.example.cube_solver.utils.rotateCounterclockwise
import com.example.cube_solver.utils.toFlatList
import com.example.cube_solver.utils.toSafeInt
import com.example.cube_solver.utils.toSquareMatrix
import com.google.android.filament.EntityInstance
import com.google.android.filament.TransformManager
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.Mat4
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.rotation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList

class RubiksCubeManager(
    private val modelViewer: ModelViewer,
    private val coroutineScope: CoroutineScope
) {

    private val cubeModelRepresentation = Rubiks.initialCubieIdentifiers.toMutableList()
    private val rubiksMoves = MutableSharedFlow<RubiksMove>(extraBufferCapacity = 1)
    private val transformManager by lazy { modelViewer.engine.transformManager }

    companion object {
        const val ONE_MOVE_ROTATION = 90f
    }

    private val movesQueue = LinkedList<RubiksMove>()

    enum class RubiksCubeFace(val indexIdentifiers: List<Int>) {
        FRONT(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8)),
        BACK(listOf(18, 19, 20, 21, 22, 23, 24, 25, 26)),
        UP(listOf(0, 1, 2, 9, 10, 11, 18, 19, 20)),
        BOTTOM(listOf(6, 7, 8, 15, 16, 17, 24, 25, 26)),
        RIGHT(listOf(2, 5, 8, 11, 14, 17, 20, 23, 26)),
        LEFT(listOf(0, 3, 6, 9, 12, 15, 18, 21, 24)),
        NONE(listOf())
    }

    private infix fun String.into(cubeFace: RubiksCubeFace): Boolean {
        val matchResult = RubiksCube.numberFindingRegex.find(this) ?: return false
        val leadingNumber = matchResult.groupValues[1].toSafeInt()
        return cubeFace.indexIdentifiers.any { cubeModelRepresentation[it] == leadingNumber }
    }

    private fun getEntityInstancesFromFace(face: RubiksCubeFace): List<Int> {
        val entitiesToRotate = mutableListOf<Int>()
        modelViewer.asset?.let { asset ->
            asset.renderableEntities.forEachIndexed { _, renderableEntity ->
                val entityName = asset.getName(renderableEntity)
                if (entityName into face) {
                    entitiesToRotate.add(renderableEntity)
                }
            }
        }
        return entitiesToRotate
    }

    private fun playMove(move: RubiksMove, onAnimationEnd: suspend CoroutineScope.() -> Unit) {
        val entitiesToRotate = getEntityInstancesFromFace(move.face)

        if (entitiesToRotate.isEmpty()) {
            coroutineScope.launch { onAnimationEnd() }
            return
        }

        val rotatingCubies = entitiesToRotate.map { entity ->
            val inst = transformManager.getInstance(entity)
            val initialTransform = transformManager.getTransform(inst)
            RotatingCubie(entity, initialTransform)
        }

        val animator = ValueAnimator.ofFloat(0f, move.rotationCount * ONE_MOVE_ROTATION).apply {
            duration = 1000L
            interpolator = AccelerateInterpolator()

            addUpdateListener { animation ->
                val currentAngle = animation.animatedValue as Float
                rotateEntities(rotatingCubies, move.axis, currentAngle)
            }

            addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    coroutineScope.launch {
                        updateCubeRepresentation(move = move)
                        onAnimationEnd()
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

    private fun rotateEntities(rotatingCubies: List<RotatingCubie>, axis: Float3, angle: Float) {
        rotatingCubies.forEach { cubie ->
            val inst = transformManager.getInstance(cubie.entity)
            val rotation = rotation(axis, angle)
            val newTransform = cubie.initialTransform * rotation
            transformManager.setTransform(inst, newTransform.toFloatArray())
        }
    }

    private suspend fun updateCubeRepresentation(move: RubiksMove) =
        withContext(Dispatchers.Default) {
            val ids = move.face.indexIdentifiers.map { cubeModelRepresentation[it] }
            val matrix = ids.toSquareMatrix(3)
            val rotated = when (move.rubiksMoveType) {
                RubiksMoveType.CLOCKWISE -> matrix.rotateClockwise(move.rotationCount)
                RubiksMoveType.ANTI_CLOCKWISE -> matrix.rotateCounterclockwise(move.rotationCount)
                RubiksMoveType.NONE -> matrix
            }
            val flatten = rotated.toFlatList()
            move.face.indexIdentifiers.forEachIndexed { i, id ->
                cubeModelRepresentation[id] = flatten[i]
            }
        }

    fun startSolving() {
        movesQueue.addAll(listOf(RubiksMove.D, RubiksMove.D_TWO, RubiksMove.F, RubiksMove.B))
        coroutineScope.launch {
            emitNextMove()
        }
        coroutineScope.launch {
            rubiksMoves.collect { move ->
                playMove(move) {
                    emitNextMove()
                }
            }
        }
    }

    private suspend fun emitNextMove() {
        movesQueue.poll()?.let { move ->
            delay(200)
            rubiksMoves.emit(move)
        }
    }

    fun test() = startSolving()

    data class RotatingCubie(val entity: Int, val initialTransform: Mat4)
}


