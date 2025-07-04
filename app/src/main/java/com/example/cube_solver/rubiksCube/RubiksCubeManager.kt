package com.example.cube_solver.rubiksCube

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.example.cube_solver.utils.log
import com.example.cube_solver.utils.logList
import com.example.cube_solver.utils.rotateClockwise
import com.example.cube_solver.utils.rotateCounterclockwise
import com.example.cube_solver.utils.toFlatList
import com.example.cube_solver.utils.toRadians
import com.example.cube_solver.utils.toSafeInt
import com.example.cube_solver.utils.toSquareMatrix
import com.google.android.filament.EntityInstance
import com.google.android.filament.TransformManager
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.Mat4
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Quaternion
import com.google.android.filament.utils.normalize
import com.google.android.filament.utils.rotation
import com.google.android.filament.utils.transpose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedList

class RubiksCubeManager(val modelViewer: ModelViewer, val coroutineScope: CoroutineScope) {
    private val cubeModelRepresentation = Rubiks.initialCubieIdentifiers.toMutableList()
    private val rubiksMoves: MutableSharedFlow<RubiksMove> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    companion object {
        const val ONE_MOVE_ROTATION = 90
    }

    private val movesLinkedList = LinkedList<RubiksMove>()

    enum class RubiksCubeFace(val indexIdentifiers: MutableList<Int>) {
        FRONT(indexIdentifiers = mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8)),
        BACK(indexIdentifiers = mutableListOf(18, 19, 20, 21, 22, 23, 24, 25, 26)),
        UP(indexIdentifiers = mutableListOf(0, 1, 2, 9, 10, 11, 18, 19, 20)),
        BOTTOM(indexIdentifiers = mutableListOf(6, 7, 8, 15, 16, 17, 24, 25, 26)),
        RIGHT(indexIdentifiers = mutableListOf(2, 5, 8, 11, 14, 17, 20, 23, 26)),
        LEFT(indexIdentifiers = mutableListOf(0, 3, 6, 9, 12, 15, 18, 21, 24)),
        NONE(indexIdentifiers = mutableListOf())
    }

    private infix fun String.into(cubeFace: RubiksCubeFace): Boolean {
        val matchResult = RubiksCube.numberFindingRegex.find(this)
        val leadingNumber = matchResult?.groupValues?.get(1) ?: return false
        val leadingNumberInt = leadingNumber.toSafeInt()
        return cubeFace.indexIdentifiers.any { cubeModelRepresentation[it] == leadingNumberInt }
    }

    private fun getEntityInstancesFromFace(face: RubiksCubeFace): MutableList<Int> {
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
        val entitiesToRotate = getEntityInstancesFromFace(face = move.face)

        val totalTicks = ONE_MOVE_ROTATION
        val totalDurationMs = 1000L
        val tickDurationMs = totalDurationMs / totalTicks
        coroutineScope.launch {
            if (entitiesToRotate.isNotEmpty()) {
                for (tick in 1..totalTicks) {
                    rotateEntities(entitiesToRotate, move.axis, 1f)
                    delay(tickDurationMs)
                }
            }
            updateCubeRepresentation(move)
            onAnimationEnd()
        }
    }

    private fun rotateEntities(entityInstances: List<Int>, axis: Float3, angle: Float) {
        entityInstances.forEachIndexed { _, entity ->
            val transformManager = modelViewer.engine.transformManager
            val entityInstance = transformManager.getInstance(entity)
            val currentTransform = transformManager.getTransform(entityInstance)
            val newRotation = rotation(axis, angle)
            val newTransform = currentTransform * newRotation
            transformManager.setTransform(entityInstance, newTransform.toFloatArray())
        }
    }

    private fun TransformManager.getTransform(@EntityInstance entityInstance: Int): Mat4 {
        val transformMatrix = FloatArray(16)
        getTransform(entityInstance, transformMatrix)
        return Mat4.of(*transformMatrix)
    }

    private suspend fun updateCubeRepresentation(move: RubiksMove) =
        withContext(Dispatchers.Default) {
            val cubiesIdentifiers =
                move.face.indexIdentifiers.map { cubeModelRepresentation[it] }.also {
                    it.log(tag = "Screwed thing")
                    require(it.size == 9) { "just got screwed cubiesIdentifiers" }
                }

            val matrix = cubiesIdentifiers.toSquareMatrix(size = 3)
            val rotatedMatrix = when (move.rubiksMoveType) {
                RubiksMoveType.CLOCKWISE -> matrix.rotateClockwise(move.rotationCount)
                RubiksMoveType.ANTI_CLOCKWISE -> matrix.rotateCounterclockwise(move.rotationCount)
                RubiksMoveType.NONE -> listOf(listOf())
            }
            val flattenList = rotatedMatrix.toFlatList()
            move.face.indexIdentifiers.forEachIndexed { index, identifier ->
                cubeModelRepresentation[identifier] = flattenList[index]
            }
        }

    private fun startSolving() {
        val listOfMove =
            mutableListOf(RubiksMove.D, RubiksMove.F, RubiksMove.D)

        movesLinkedList.addAll(listOfMove)
        startCollectionOfMove()
        coroutineScope.launch {
            emitMove()
        }
    }

    private suspend fun emitMove() {
        movesLinkedList.poll()?.let { move ->
            delay(1000)
            rubiksMoves.emit(move)
        }
    }

    private fun startCollectionOfMove() {
        coroutineScope.launch {
            rubiksMoves.collect { rubiksMove ->
                playMove(rubiksMove) {
                    emitMove()
                }
            }
        }
    }

    fun test() {
        startSolving()
    }
}

