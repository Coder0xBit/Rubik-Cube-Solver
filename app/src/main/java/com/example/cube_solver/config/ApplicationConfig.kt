package com.example.cube_solver.config

import com.google.android.filament.utils.Float3

object ApplicationConfig {
    val defaultObjectPosition = Float3(0.0f, 0.0f, -8.0f)
    val nearPlane = 0.05f
    val farPlane = 1000.0f
    val aperture = 16f
    val shutterSpeed = 1f / 125f
    val sensitivity = 100f
    var enableFreeCameraMovement = false
    fun getFreeMovementLabel(isFreeMovement: Boolean): String {
        return if (enableFreeCameraMovement) {
            "Free Movement"
        } else {
            "Not Free Movement"
        }
    }
}