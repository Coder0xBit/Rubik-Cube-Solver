package com.example.cube_solver.rubiksCube

import android.animation.ValueAnimator
import android.content.Context
import android.view.animation.LinearInterpolator
import com.example.cube_solver.config.ApplicationConfig
import com.example.cube_solver.rubiksCube.RubiksCubeManager.RubiksCubeFace
import com.example.cube_solver.utils.readAsset
import com.google.android.filament.Entity
import com.google.android.filament.Material
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.KTX1Loader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.rotation

class ModelLoader(
    private val context: Context,
    private val modelViewer: ModelViewer
) {

    var redMaterial: Material
    var greenMaterial: Material
    var blueMaterial: Material
    var yellowMaterial: Material
    var whiteMaterial: Material
    var orangeMaterial: Material

    init {
        context.readAsset("materials/red.filamat").let {
            redMaterial = Material.Builder().payload(it, it.remaining()).build(modelViewer.engine)
        }

        context.readAsset("materials/green.filamat").let {
            greenMaterial = Material.Builder().payload(it, it.remaining()).build(modelViewer.engine)
        }

        context.readAsset("materials/blue.filamat").let {
            blueMaterial = Material.Builder().payload(it, it.remaining()).build(modelViewer.engine)
        }

        context.readAsset("materials/yellow.filamat").let {
            yellowMaterial =
                Material.Builder().payload(it, it.remaining()).build(modelViewer.engine)
        }

        context.readAsset("materials/white.filamat").let {
            whiteMaterial = Material.Builder().payload(it, it.remaining()).build(modelViewer.engine)
        }

        context.readAsset("materials/orange.filamat").let {
            orangeMaterial =
                Material.Builder().payload(it, it.remaining()).build(modelViewer.engine)
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

    fun loadSkyBox(iblName: String) {
        val indirectLightBuffer = context.readAsset(getSkyBoxIblPath(skyBoxName = iblName))
        val indirectLight = KTX1Loader.createIndirectLight(modelViewer.engine, indirectLightBuffer)
        indirectLight.intensity = 20_000f
        modelViewer.scene.indirectLight = indirectLight

        val skyboxBuffer = context.readAsset(getSkyBoxPath(skyBoxName = iblName))
        val skybox = KTX1Loader.createSkybox(modelViewer.engine, skyboxBuffer)
        modelViewer.scene.skybox = skybox
    }

    fun loadGlb(glbName: String) {
        val buffer = context.readAsset(getGlbPath(glbName = glbName))
        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube(centerPoint = ApplicationConfig.defaultCubePosition)
    }

    fun makeFrontRed() {
        modelViewer.asset?.let { asset ->
            asset.renderableEntities.forEachIndexed { index, renderableEntity ->
                val entityName = asset.getName(renderableEntity)
                if (entityName into CubeFace.FRONT) {
                    applyMaterial(renderable = renderableEntity, material = redMaterial)
                }
            }
        }
    }

    fun makeUpYellow() {
        modelViewer.asset?.let { asset ->
            asset.renderableEntities.forEachIndexed { index, renderableEntity ->
                val entityName = asset.getName(renderableEntity)
                if (entityName into CubeFace.UP) {
                    applyMaterial(renderable = renderableEntity, material = yellowMaterial)
                }
            }
        }
    }

    fun applyMaterial(
        modelViewer: ModelViewer = this.modelViewer,
        @Entity renderable: Int,
        material: Material
    ) {
        val renderableInstance = modelViewer.engine.renderableManager.getInstance(renderable)
        modelViewer.engine.renderableManager.setMaterialInstanceAt(
            renderableInstance,
            0,
            material.defaultInstance
        )
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