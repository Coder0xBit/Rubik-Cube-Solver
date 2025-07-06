package com.example.cube_solver.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cube_solver.rubiksCube.AssetViewer
import com.example.cube_solver.rubiksCube.RubiksCubeManager
import com.example.cube_solver.rubiksCube.RubiksMove
import com.google.android.filament.gltfio.FilamentAsset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.LinkedList

class MainViewModel(assetViewer: AssetViewer) : ViewModel() {
    private val rubiksCubeManager =
        RubiksCubeManager(assetViewer.engine.transformManager)

    private val movesQueue = LinkedList<RubiksMove>()
    private val rubiksMoves = MutableSharedFlow<RubiksMove>(replay = 0)
    private var isMovesPlaying = false

    private var rubiksMovePlayingJob: Job? = null

    init {
        initializeMoves()
    }

    private fun initializeMoves() {
        movesQueue.addAll(
            listOf(
                RubiksMove.D,
                RubiksMove.D_TWO,
                RubiksMove.F,
                RubiksMove.B,
                RubiksMove.D_APOSTROPHE,
                RubiksMove.D
            )
        )
    }

    fun startSolving(rubiksCube: FilamentAsset) {
        if (isMovesPlaying) return
        isMovesPlaying = true
        if (movesQueue.isEmpty()) {
            initializeMoves()
        }

        rubiksMovePlayingJob?.cancel()
        rubiksMovePlayingJob = viewModelScope.launch {
            launch {
                rubiksMoves.collect { move ->
                    rubiksCubeManager.playMove(rubiksCube, move, viewModelScope) {
                        emitNextMove()
                    }
                }
            }
            emitNextMove()
        }
    }

    fun playMove(rubiksCube: FilamentAsset, move: RubiksMove) {
        if (isMovesPlaying) return
        isMovesPlaying = true
        rubiksCubeManager.playMove(rubiksCube, move, viewModelScope) {
            isMovesPlaying = false
        }
    }

    private suspend fun emitNextMove() {
        movesQueue.poll()?.let { move ->
            delay(200)
            rubiksMoves.emit(move)
        } ?: run {
            isMovesPlaying = false
        }
    }
}

class MainViewModelFactory(
    private val assetViewer: AssetViewer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(assetViewer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}