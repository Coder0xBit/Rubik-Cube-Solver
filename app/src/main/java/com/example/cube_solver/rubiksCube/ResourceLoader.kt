package com.example.cube_solver.rubiksCube

import android.content.Context
import com.example.cube_solver.config.ApplicationConfig
import com.example.cube_solver.ui.RubiksCubeColors
import com.example.cube_solver.utils.readAsset
import com.google.android.filament.Entity
import com.google.android.filament.IndirectLight
import com.google.android.filament.Material
import com.google.android.filament.Skybox
import com.google.android.filament.utils.KTX1Loader
import java.nio.Buffer
import java.nio.ByteBuffer

class ResourceLoader(
    private val context: Context,
    private val assetViewer: AssetViewer
) {

    var redMaterial: Material
    var greenMaterial: Material
    var blueMaterial: Material
    var yellowMaterial: Material
    var whiteMaterial: Material
    var orangeMaterial: Material

    init {
        context.readAsset("materials/red.filamat").let {
            redMaterial = Material.Builder().payload(it, it.remaining()).build(assetViewer.engine)
        }

        context.readAsset("materials/green.filamat").let {
            greenMaterial = Material.Builder().payload(it, it.remaining()).build(assetViewer.engine)
        }

        context.readAsset("materials/blue.filamat").let {
            blueMaterial = Material.Builder().payload(it, it.remaining()).build(assetViewer.engine)
        }

        context.readAsset("materials/yellow.filamat").let {
            yellowMaterial =
                Material.Builder().payload(it, it.remaining()).build(assetViewer.engine)
        }

        context.readAsset("materials/white.filamat").let {
            whiteMaterial = Material.Builder().payload(it, it.remaining()).build(assetViewer.engine)
        }

        context.readAsset("materials/orange.filamat").let {
            orangeMaterial =
                Material.Builder().payload(it, it.remaining()).build(assetViewer.engine)
        }

    }

    private fun getSkyBoxIblPath(skyBoxName: String): String {
        return "environments/${skyBoxName}/${skyBoxName}_ibl.ktx"
    }

    private fun getSkyBoxPath(skyBoxName: String): String {
        return "environments/${skyBoxName}/${skyBoxName}_skybox.ktx"
    }

    private fun getGlbPath(glbName: String): String {
        return "models/${glbName}.glb"
    }

    fun loadIndirectLight(iblName: String): IndirectLight {
        val indirectLightBuffer = context.readAsset(getSkyBoxIblPath(skyBoxName = iblName))
        val indirectLight = KTX1Loader.createIndirectLight(assetViewer.engine, indirectLightBuffer)
        indirectLight.intensity = 20_000f
        return indirectLight
    }

    fun loadSkyBox(iblName: String): Skybox {
        val skyboxBuffer = context.readAsset(getSkyBoxPath(skyBoxName = iblName))
        return KTX1Loader.createSkybox(assetViewer.engine, skyboxBuffer)
    }

    fun loadGlb(glbName: String): ByteBuffer {
        return context.readAsset(getGlbPath(glbName = glbName))
    }

    fun getMaterial(color : RubiksCubeColors) : Material {
        return when(color) {
            RubiksCubeColors.RED -> redMaterial
            RubiksCubeColors.GREEN -> greenMaterial
            RubiksCubeColors.BLUE -> blueMaterial
            RubiksCubeColors.YELLOW -> yellowMaterial
            RubiksCubeColors.WHITE -> whiteMaterial
            RubiksCubeColors.ORANGE -> orangeMaterial
        }
    }


    fun getMaterialFromName(entityName: String): Material? {
        return when {
            entityName into CubeFace.FRONT -> redMaterial
            entityName into CubeFace.BACK -> greenMaterial
            entityName into CubeFace.RIGHT -> yellowMaterial
            entityName into CubeFace.LEFT -> whiteMaterial
            entityName into CubeFace.UP -> blueMaterial
            entityName into CubeFace.DOWN -> orangeMaterial
            else -> null
        }
    }
}