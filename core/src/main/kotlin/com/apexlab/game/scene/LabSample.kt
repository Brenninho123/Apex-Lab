package com.apexlab.game.scene

import com.apexlab.game.theme.LabPalette
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private const val POP_DURATION = 0.35f

class LabSample internal constructor(
    val type: SampleType,
    val instance: ModelInstance,
    private val restX: Float,
    private val restY: Float,
    private val phase: Float
) {
    var analyzed = false
        private set

    var hovered = false
        internal set

    val bounds = BoundingBox()

    private val restBounds = BoundingBox()
    private val scratchMin = Vector3()
    private val scratchMax = Vector3()
    private val liquid: Material = instance.getMaterial("liquid")
    private val emissive: ColorAttribute = liquid.get(ColorAttribute::class.java, ColorAttribute.Emissive)
    private var hoverAmount = 0f
    private var glow = 0f
    private var popTimer = 0f

    init {
        instance.calculateBoundingBox(restBounds)
        restBounds.mul(Matrix4().setToTranslation(restX, restY, 0f))
        bounds.set(restBounds)
        update(0f, 0f)
    }

    internal fun markAnalyzed() {
        analyzed = true
        hovered = false
        popTimer = POP_DURATION
        liquid.set(ColorAttribute.createDiffuse(LabPalette.analyzed))
    }

    internal fun update(time: Float, delta: Float) {
        val blend = min(1f, delta * 12f)
        val hoverTarget = if (hovered && !analyzed) 1f else 0f
        hoverAmount += (hoverTarget - hoverAmount) * blend

        val glowTarget = if (analyzed) 0.32f else hoverAmount * 0.4f
        glow += (glowTarget - glow) * blend
        val source = if (analyzed) LabPalette.analyzed else LabPalette.hover
        emissive.color.set(source.r * glow, source.g * glow, source.b * glow, 1f)

        var bob = 0f
        var spin = 0f
        var scale = 1f + hoverAmount * 0.06f
        var hop = 0f

        if (!analyzed) {
            bob = sin(time * 2f + phase) * 0.04f
            if (type.spins) spin = (time * 18f + phase * 30f) % 360f
        }
        if (popTimer > 0f) {
            popTimer = max(0f, popTimer - delta)
            val wave = sin((1f - popTimer / POP_DURATION) * PI.toFloat())
            hop = wave * 0.22f
            scale += wave * 0.14f
        }

        instance.transform.idt()
            .translate(restX, restY + bob + hop, 0f)
            .rotate(Vector3.Y, spin)
            .scale(scale, scale, scale)

        bounds.set(
            scratchMin.set(restBounds.min).add(0f, bob, 0f),
            scratchMax.set(restBounds.max).add(0f, bob, 0f)
        )
    }
}
