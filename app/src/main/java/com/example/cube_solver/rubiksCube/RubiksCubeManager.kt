package com.example.cube_solver.rubiksCube

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.util.Log
import android.view.animation.LinearInterpolator
import com.example.cube_solver.utils.log
import com.example.cube_solver.utils.logList
import com.example.cube_solver.utils.rotateClockwise
import com.example.cube_solver.utils.rotateCounterclockwise
import com.example.cube_solver.utils.toFlatList
import com.example.cube_solver.utils.toMatrix
import com.example.cube_solver.utils.toSafeInt
import com.example.cube_solver.utils.toSquareMatrix
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.rotation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.LinkedList

class RubiksCubeManager(val modelViewer: ModelViewer, val coroutineScope: CoroutineScope) {
    private val cubeModelRepresentation = Rubiks.initialCubieIdentifiers.toMutableList()
    private val rubiksMoves: MutableSharedFlow<RubiksMove> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    companion object {
        const val ONE_MOVE_ROTATION = 90f
    }

    private val rubiksCubeEachFaceRotation = hashMapOf(
        RubiksCubeFace.FRONT to 0f,
        RubiksCubeFace.BACK to 0f,
        RubiksCubeFace.UP to 0f,
        RubiksCubeFace.BOTTOM to 0f,
        RubiksCubeFace.RIGHT to 0f,
        RubiksCubeFace.LEFT to 0f
    )

    private val movesLinkedList = LinkedList<RubiksMove>()

    enum class RubiksMoveType {
        NONE, CLOCKWISE, ANTI_CLOCKWISE
    }

    enum class RubiksMove(
        val value: String,
        val face: RubiksCubeFace,
        val axis: Float3,
        val rotationCount: Int,
        val rubiksMoveType: RubiksMoveType
    ) {
        NONE(
            value = "",
            face = RubiksCubeFace.NONE,
            axis = Float3(),
            rotationCount = 0,
            rubiksMoveType = RubiksMoveType.NONE
        ),

        // Front Face Rotations
        F(
            value = "F",
            face = RubiksCubeFace.FRONT,
            axis = Float3(x = 1.0f, y = 0.0f, z = 0.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.CLOCKWISE
        ),
        F_APOSTROPHE(
            value = "F'",
            face = RubiksCubeFace.FRONT,
            axis = Float3(x = -1.0f, y = 0.0f, z = 0.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
        ),
        F_TWO(
            value = "F2",
            face = RubiksCubeFace.FRONT,
            axis = Float3(x = 1.0f, y = 0.0f, z = 0.0f),
            rotationCount = 2,
            rubiksMoveType = RubiksMoveType.CLOCKWISE
        ),

        // Back Face Rotations
        B(
            value = "B",
            face = RubiksCubeFace.BACK,
            axis = Float3(x = -1.0f, y = 0.0f, z = 0.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
        ),
        B_APOSTROPHE(
            value = "B'",
            face = RubiksCubeFace.BACK,
            axis = Float3(x = 1.0f, y = 0.0f, z = 0.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.CLOCKWISE
        ),
        B_TWO(
            value = "B2",
            face = RubiksCubeFace.BACK,
            axis = Float3(x = -1.0f, y = 0.0f, z = 0.0f),
            rotationCount = 2,
            rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
        ),

        // Left Face Rotations
        L(
            value = "L",
            face = RubiksCubeFace.LEFT,
            axis = Float3(x = 0.0f, y = 0.0f, z = -1.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
        ),
        L_APOSTROPHE(
            value = "L'",
            face = RubiksCubeFace.LEFT,
            axis = Float3(x = 1.0f, y = 0.0f, z = 1.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.CLOCKWISE
        ),
        L_TWO(
            value = "L2",
            face = RubiksCubeFace.LEFT,
            axis = Float3(x = 0.0f, y = 0.0f, z = -1.0f),
            rotationCount = 2,
            rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
        ),

        // Right Face Rotations
        R(
            value = "R",
            face = RubiksCubeFace.RIGHT,
            axis = Float3(x = 0.0f, y = 0.0f, z = 1.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.CLOCKWISE
        ),
        R_APOSTROPHE(
            value = "R'",
            face = RubiksCubeFace.RIGHT,
            axis = Float3(x = 0.0f, y = 0.0f, z = -1.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
        ),
        R_TWO(
            value = "R2",
            face = RubiksCubeFace.RIGHT,
            axis = Float3(x = 0.0f, y = 0.0f, z = 1.0f),
            rotationCount = 2,
            rubiksMoveType = RubiksMoveType.CLOCKWISE
        ),

        // Up Face Rotations
        U(
            value = "U",
            face = RubiksCubeFace.UP,
            axis = Float3(x = 0.0f, y = -1.0f, z = 0.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
        ),
        U_APOSTROPHE(
            value = "U'",
            face = RubiksCubeFace.UP,
            axis = Float3(x = 0.0f, y = 1.0f, z = 0.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.CLOCKWISE
        ),
        U_TWO(
            value = "U2",
            face = RubiksCubeFace.UP,
            axis = Float3(x = 0.0f, y = -1.0f, z = 0.0f),
            rotationCount = 2,
            rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
        ),

        // Down Face Rotations
        D(
            value = "D",
            face = RubiksCubeFace.BOTTOM,
            axis = Float3(x = 0.0f, y = 1.0f, z = 0.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.CLOCKWISE
        ),
        D_APOSTROPHE(
            value = "D'",
            face = RubiksCubeFace.BOTTOM,
            axis = Float3(x = 0.0f, y = -1.0f, z = 0.0f),
            rotationCount = 1,
            rubiksMoveType = RubiksMoveType.ANTI_CLOCKWISE
        ),
        D_TWO(
            value = "D2",
            face = RubiksCubeFace.BOTTOM,
            axis = Float3(x = 0.0f, y = 1.0f, z = 0.0f),
            rotationCount = 2,
            rubiksMoveType = RubiksMoveType.CLOCKWISE
        );
    }


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

    fun playMove(move: RubiksMove, onAnimationEnd: suspend CoroutineScope.() -> Unit) {
        val entitiesToRotate = mutableListOf<Int>()
        modelViewer.asset?.let { asset ->
            asset.renderableEntities.forEachIndexed { index, renderableEntity ->
                val entityName = asset.getName(renderableEntity)
                if (entityName into move.face) {
                    entitiesToRotate.add(renderableEntity)
                }
            }

            if (entitiesToRotate.isNotEmpty()) {
                val currentRotation = rubiksCubeEachFaceRotation[move.face] ?: 0f
                val finalRotation = currentRotation + move.rotationCount * ONE_MOVE_ROTATION

                val animator = ValueAnimator.ofFloat(0f, finalRotation).apply {
                    duration = 5000L
                    interpolator = LinearInterpolator()

                    addUpdateListener { animation ->
                        val currentAngle = animation.animatedValue as Float

                        entitiesToRotate.forEachIndexed { index, entity ->
                            val transformManager = modelViewer.engine.transformManager
                            val entityInstance = transformManager.getInstance(entity)
                            transformManager.setTransform(
                                entityInstance,
                                rotation(move.axis, currentAngle).toFloatArray()
                            )
                        }
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
        }
    }

    // this changes the cubeRepresentation by getting the each faces index from face
    private fun updateCubeRepresentation(move: RubiksMove) {
        val cubiesIdentifiers = mutableListOf<Int>()
        move.face.indexIdentifiers.forEach { cubiesIdentifiers.add(cubeModelRepresentation[it]) }
        require(cubiesIdentifiers.size == 9) { "just got screwed cubiesIdentifiers" }
        val matrix = cubiesIdentifiers.toSquareMatrix(size = 3)
        matrix.log(tag = "Before Transformation")
        val rotatedMatrix = when (move.rubiksMoveType) {
            RubiksMoveType.CLOCKWISE -> matrix.rotateClockwise(move.rotationCount)
            RubiksMoveType.ANTI_CLOCKWISE -> matrix.rotateCounterclockwise(move.rotationCount)
            RubiksMoveType.NONE -> listOf(listOf())
        }
        rotatedMatrix.log(tag = "After Transformation")
        val flattenList = rotatedMatrix.toFlatList()
        flattenList.logList(tag = "Flatten list")
        move.face.indexIdentifiers.forEachIndexed { index, identifier ->
            cubeModelRepresentation[identifier] = flattenList[index]
        }
        cubeModelRepresentation.logList(tag = "Cube Representation")
    }

    fun startSolving() {
        val listOfMove =
            mutableListOf(RubiksMove.D , RubiksMove.F)

        movesLinkedList.addAll(listOfMove)
        startCollectionOfMove()
        coroutineScope.launch {
            emitMove()
        }
    }

    private suspend fun emitMove() {
        val move = movesLinkedList.poll() ?: RubiksMove.NONE
        delay(15000)
        rubiksMoves.emit(move)
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

