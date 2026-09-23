package com.apexlab.game.scene

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.Ray
import kotlin.math.cos
import kotlin.math.sin

class OrbitCamera(fieldOfView: Float = 55f) {

    val camera = PerspectiveCamera(
        fieldOfView,
        Gdx.graphics.width.toFloat(),
        Gdx.graphics.height.toFloat()
    )
    val target = Vector3(0f, 0.6f, 0f)

    var azimuth = 0f
        set(value) {
            field = value.coerceIn(MIN_AZIMUTH, MAX_AZIMUTH)
        }

    var elevation = 24f
        set(value) {
            field = value.coerceIn(MIN_ELEVATION, MAX_ELEVATION)
        }

    var radius = 6.4f
        set(value) {
            field = value.coerceIn(MIN_RADIUS, MAX_RADIUS)
        }

    init {
        camera.near = 0.1f
        camera.far = 120f
        update()
    }

    fun orbit(deltaAzimuth: Float, deltaElevation: Float) {
        azimuth += deltaAzimuth
        elevation += deltaElevation
    }

    fun zoom(deltaRadius: Float) {
        radius += deltaRadius
    }

    fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
    }

    fun pickRay(screenX: Int, screenY: Int): Ray = camera.getPickRay(screenX.toFloat(), screenY.toFloat())

    fun update() {
        val azimuthRadians = Math.toRadians(azimuth.toDouble())
        val elevationRadians = Math.toRadians(elevation.toDouble())
        val horizontal = radius * cos(elevationRadians).toFloat()
        camera.position.set(
            target.x + horizontal * sin(azimuthRadians).toFloat(),
            target.y + radius * sin(elevationRadians).toFloat(),
            target.z + horizontal * cos(azimuthRadians).toFloat()
        )
        camera.up.set(Vector3.Y)
        camera.lookAt(target)
        camera.update()
    }

    companion object {
        const val MIN_AZIMUTH = -70f
        const val MAX_AZIMUTH = 70f
        const val MIN_ELEVATION = 8f
        const val MAX_ELEVATION = 70f
        const val MIN_RADIUS = 3.5f
        const val MAX_RADIUS = 11f
    }
}
