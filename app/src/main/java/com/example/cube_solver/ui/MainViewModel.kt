package com.example.cube_solver.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cube_solver.rubiksCube.AssetViewer
import com.example.cube_solver.rubiksCube.RubiksCubeManager
import com.example.cube_solver.rubiksCube.RubiksMove
import com.example.cube_solver.utils.log
import com.google.android.filament.gltfio.FilamentAsset
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.LinkedList

class MainViewModel(assetViewer: AssetViewer) : ViewModel() {
    private val rubiksCubeManager =
        RubiksCubeManager(assetViewer.engine.transformManager)

    private val movesQueue = LinkedList<RubiksMove>()
    private val rubiksMoves = MutableSharedFlow<RubiksMove>(extraBufferCapacity = 1)

    init {
        movesQueue.addAll(listOf(RubiksMove.D, RubiksMove.D_TWO, RubiksMove.F, RubiksMove.B))
    }

    fun startSolving(rubiksCube: FilamentAsset) {
        viewModelScope.launch {
            emitNextMove()
        }
        viewModelScope.launch {
            rubiksMoves.collect { move ->
                rubiksCubeManager.playMove(rubiksCube, move, viewModelScope) {
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