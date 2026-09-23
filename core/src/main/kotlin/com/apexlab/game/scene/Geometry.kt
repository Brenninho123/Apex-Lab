package com.apexlab.game.scene

import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.BoxShapeBuilder
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private const val EPSILON = 1e-6f

internal class ProfilePoint(val r: Float, val y: Float, val crease: Boolean)

internal class Profile {
    val points = mutableListOf<ProfilePoint>()

    fun point(r: Float, y: Float, crease: Boolean = false): Profile {
        points += ProfilePoint(r, y, crease)
        return this
    }

    fun arc(
        centerR: Float,
        centerY: Float,
        radius: Float,
        fromDegrees: Float,
        toDegrees: Float,
        steps: Int,
        creaseAtEnd: Boolean = false
    ): Profile {
        for (step in 0..steps) {
            val angle = Math.toRadians((fromDegrees + (toDegrees - fromDegrees) * step / steps).toDouble())
            point(
                centerR + radius * cos(angle).toFloat(),
                centerY + radius * sin(angle).toFloat(),
                creaseAtEnd && step == steps
            )
        }
        return this
    }
}

private fun joinNormal(own: Vector2, neighbor: Vector2?, crease: Boolean): Vector2 {
    if (crease || neighbor == null || neighbor.isZero) return own
    val blended = Vector2(own).add(neighbor)
    return if (blended.isZero) own else blended.nor()
}

private fun MeshPartBuilder.emit(
    x: Float, y: Float, z: Float,
    nx: Float, ny: Float, nz: Float,
    transform: Matrix4?,
    position: Vector3,
    normal: Vector3
): Short {
    position.set(x, y, z)
    normal.set(nx, ny, nz)
    if (transform != null) {
        position.mul(transform)
        normal.rot(transform).nor()
    }
    return vertex(position.x, position.y, position.z, normal.x, normal.y, normal.z)
}

internal fun MeshPartBuilder.lathe(profile: Profile, divisions: Int = 36, transform: Matrix4? = null) {
    val points = profile.points
    val segments = points.size - 1
    val segmentNormals = Array(segments) { k ->
        val dr = points[k + 1].r - points[k].r
        val dy = points[k + 1].y - points[k].y
        val length = hypot(dr, dy)
        if (length < EPSILON) Vector2() else Vector2(dy / length, -dr / length)
    }
    val position = Vector3()
    val normal = Vector3()
    val ring = ShortArray((divisions + 1) * 2)

    for (k in 0 until segments) {
        val own = segmentNormals[k]
        if (own.isZero) continue
        val startNormal = joinNormal(own, segmentNormals.getOrNull(k - 1), points[k].crease)
        val endNormal = joinNormal(own, segmentNormals.getOrNull(k + 1), points[k + 1].crease)

        for (i in 0..divisions) {
            val angle = 2.0 * PI * i / divisions
            val c = cos(angle).toFloat()
            val s = sin(angle).toFloat()
            ring[i * 2] = emit(
                points[k].r * c, points[k].y, points[k].r * s,
                startNormal.x * c, startNormal.y, startNormal.x * s,
                transform, position, normal
            )
            ring[i * 2 + 1] = emit(
                points[k + 1].r * c, points[k + 1].y, points[k + 1].r * s,
                endNormal.x * c, endNormal.y, endNormal.x * s,
                transform, position, normal
            )
        }
        for (i in 0 until divisions) {
            val b0 = ring[i * 2]
            val t0 = ring[i * 2 + 1]
            val b1 = ring[(i + 1) * 2]
            val t1 = ring[(i + 1) * 2 + 1]
            index(b0, t0, t1)
            index(b0, t1, b1)
        }
    }
}

internal fun MeshPartBuilder.texturedQuad(
    corner00: Vector3,
    corner10: Vector3,
    corner11: Vector3,
    corner01: Vector3,
    normal: Vector3,
    uMax: Float,
    vMax: Float
) {
    fun corner(position: Vector3, u: Float, v: Float) =
        MeshPartBuilder.VertexInfo().setPos(position).setNor(normal).setUV(u, v)

    rect(
        corner(corner00, 0f, vMax),
        corner(corner10, uMax, vMax),
        corner(corner11, uMax, 0f),
        corner(corner01, 0f, 0f)
    )
}

internal fun MeshPartBuilder.boxAt(x: Float, y: Float, z: Float, width: Float, height: Float, depth: Float) {
    setVertexTransform(Matrix4().setToTranslation(x, y, z))
    BoxShapeBuilder.build(this, width, height, depth)
    setVertexTransform(null)
}
