package com.apexlab.game.scene

import com.apexlab.game.theme.LabPalette
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector3
import kotlin.math.cos
import kotlin.math.sin

object LabModels {

    private val PLAIN = (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()
    private val TEXTURED = (
        VertexAttributes.Usage.Position or
            VertexAttributes.Usage.Normal or
            VertexAttributes.Usage.TextureCoordinates
        ).toLong()

    fun sample(type: SampleType, liquidColor: Color): Model = when (type) {
        SampleType.ERLENMEYER_FLASK -> erlenmeyerFlask(liquidColor)
        SampleType.TEST_TUBE_RACK -> testTubeRack(liquidColor)
        SampleType.BEAKER -> beaker(liquidColor)
        SampleType.ROUND_FLASK -> roundFlask(liquidColor)
    }

    fun coaster(): Model = build {
        part("coaster", GL20.GL_TRIANGLES, PLAIN, solid("coaster", LabPalette.coaster, 0.4f)).lathe(
            Profile()
                .point(0.55f, 0f)
                .point(0.55f, 0.03f, true)
                .point(0f, 0.03f)
        )
    }

    fun bench(): Model = build {
        part("top", GL20.GL_TRIANGLES, PLAIN, solid("bench-top", LabPalette.benchTop, 0.7f))
            .boxAt(0f, -0.06f, 0f, 8.4f, 0.12f, 3.0f)
        part("cabinet", GL20.GL_TRIANGLES, PLAIN, solid("cabinet", LabPalette.cabinet, 0.25f))
            .boxAt(0f, -0.81f, 0f, 8.0f, 1.38f, 2.7f)

        val columns = floatArrayOf(-2.925f, -0.975f, 0.975f, 2.925f)
        val rows = floatArrayOf(-0.47f, -1.12f)

        val drawers = part("drawers", GL20.GL_TRIANGLES, PLAIN, solid("drawer", LabPalette.drawer, 0.3f))
        for (y in rows) for (x in columns) drawers.boxAt(x, y, 1.37f, 1.85f, 0.58f, 0.04f)

        val handles = part("handles", GL20.GL_TRIANGLES, PLAIN, solid("handle", LabPalette.handle, 0.9f))
        for (y in rows) for (x in columns) handles.boxAt(x, y + 0.16f, 1.41f, 0.5f, 0.05f, 0.05f)
    }

    fun room(floor: Texture, wall: Texture, poster: Texture): Model = build {
        part("floor", GL20.GL_TRIANGLES, TEXTURED, tiled("floor", floor, 1f, 1f)).texturedQuad(
            Vector3(-20f, -1.5f, 20f), Vector3(20f, -1.5f, 20f),
            Vector3(20f, -1.5f, -20f), Vector3(-20f, -1.5f, -20f),
            Vector3.Y, 40f / 2.4f, 40f / 2.4f
        )
        part("wall", GL20.GL_TRIANGLES, TEXTURED, tiled("wall", wall, 1f, 1f)).texturedQuad(
            Vector3(-20f, -1.5f, -3.6f), Vector3(20f, -1.5f, -3.6f),
            Vector3(20f, 6.5f, -3.6f), Vector3(-20f, 6.5f, -3.6f),
            Vector3.Z, 40f / 1.8f, 8f / 1.8f
        )
        part("baseboard", GL20.GL_TRIANGLES, PLAIN, solid("baseboard", LabPalette.baseboard, 0.2f))
            .boxAt(0f, -1.4f, -3.56f, 40f, 0.2f, 0.08f)
        part("poster", GL20.GL_TRIANGLES, TEXTURED, tiled("poster", poster, 1f, 1f))
            .boxAt(-0.8f, 2.6f, -3.57f, 3.6f, 1.8f, 0.05f)
    }

    fun shelf(): Model = build {
        val boards = part("boards", GL20.GL_TRIANGLES, PLAIN, solid("board", LabPalette.shelfBoard, 0.3f))
        boards.boxAt(0f, 0f, 0f, 3.4f, 0.07f, 0.5f)
        boards.boxAt(0f, 1.3f, 0f, 3.4f, 0.07f, 0.5f)

        val lower = listOf(
            Bottle(-1.3f, 0.035f, 0.20f, 0.70f, 0),
            Bottle(-0.6f, 0.035f, 0.17f, 0.55f, 1),
            Bottle(0.1f, 0.035f, 0.18f, 0.62f, 2),
            Bottle(0.8f, 0.035f, 0.15f, 0.45f, 0),
            Bottle(1.4f, 0.035f, 0.19f, 0.75f, 1)
        )
        val upper = listOf(
            Bottle(-1.2f, 1.335f, 0.16f, 0.50f, 2),
            Bottle(-0.4f, 1.335f, 0.19f, 0.65f, 0),
            Bottle(0.5f, 1.335f, 0.17f, 0.60f, 1),
            Bottle(1.2f, 1.335f, 0.20f, 0.70f, 2)
        )
        val bottles = lower + upper
        val glassColors = listOf(LabPalette.bottleAmber, LabPalette.bottleGreen, LabPalette.bottleBlue)
        val glassNames = listOf("bottle-amber", "bottle-green", "bottle-blue")

        for (tone in 0..2) {
            val builder = part(glassNames[tone], GL20.GL_TRIANGLES, PLAIN, solid(glassNames[tone], glassColors[tone], 0.9f))
            for (bottle in bottles.filter { it.tone == tone }) {
                builder.lathe(bottle.body(), 28, Matrix4().setToTranslation(bottle.x, bottle.y, 0f))
            }
        }
        val caps = part("caps", GL20.GL_TRIANGLES, PLAIN, solid("cap", LabPalette.bottleCap, 0.5f))
        for (bottle in bottles) {
            caps.lathe(bottle.cap(), 20, Matrix4().setToTranslation(bottle.x, bottle.y + bottle.height, 0f))
        }
    }

    fun microscope(): Model = build {
        val body = part("body", GL20.GL_TRIANGLES, PLAIN, solid("scope-body", LabPalette.scopeBody, 0.8f))
        body.boxAt(0f, 0.04f, 0f, 0.62f, 0.08f, 0.5f)
        body.boxAt(0f, 0.4f, -0.2f, 0.12f, 0.64f, 0.12f)
        body.boxAt(0f, 0.7f, -0.12f, 0.12f, 0.1f, 0.3f)

        val dark = part("dark", GL20.GL_TRIANGLES, PLAIN, solid("scope-dark", LabPalette.scopeDark, 0.9f))
        dark.boxAt(0f, 0.3f, 0f, 0.42f, 0.04f, 0.34f)
        val tilt = Matrix4().setToTranslation(0f, 0.62f, -0.02f).rotate(Vector3.X, 30f)
        dark.lathe(
            Profile()
                .point(0f, 0f)
                .point(0.075f, 0f, true)
                .point(0.075f, 0.5f, true)
                .point(0.045f, 0.5f, true)
                .point(0.045f, 0.62f, true)
                .point(0f, 0.62f),
            28,
            tilt
        )
        dark.lathe(
            Profile()
                .point(0f, 0.36f)
                .point(0.04f, 0.36f, true)
                .point(0.04f, 0.6f, true)
                .point(0f, 0.6f),
            20,
            Matrix4().setToTranslation(0f, 0f, -0.02f)
        )
    }

    private fun erlenmeyerFlask(liquidColor: Color): Model = build {
        part("glass", GL20.GL_TRIANGLES, PLAIN, glass()).lathe(
            Profile()
                .point(0f, 0f)
                .arc(0.36f, 0.06f, 0.06f, -90f, 0f, 5)
                .point(0.09f, 0.74f)
                .point(0.09f, 0.98f)
                .point(0.125f, 1.01f, true)
                .point(0.125f, 1.04f)
        )
        part("liquid", GL20.GL_TRIANGLES, PLAIN, liquid(liquidColor)).lathe(
            Profile()
                .point(0f, 0.03f)
                .arc(0.34f, 0.08f, 0.05f, -90f, 0f, 4)
                .point(0.274f, 0.32f, true)
                .point(0f, 0.32f)
        )
    }

    private fun beaker(liquidColor: Color): Model = build {
        part("glass", GL20.GL_TRIANGLES, PLAIN, glass()).lathe(
            Profile()
                .point(0f, 0f)
                .arc(0.36f, 0.04f, 0.04f, -90f, 0f, 5)
                .point(0.40f, 0.86f)
                .point(0.43f, 0.89f, true)
                .point(0.43f, 0.91f)
        )
        part("liquid", GL20.GL_TRIANGLES, PLAIN, liquid(liquidColor)).lathe(
            Profile()
                .point(0f, 0.03f)
                .arc(0.33f, 0.07f, 0.04f, -90f, 0f, 4)
                .point(0.37f, 0.50f, true)
                .point(0f, 0.50f)
        )
        val marks = part("marks", GL20.GL_TRIANGLES, PLAIN, solid("marks", LabPalette.marks, 0.1f))
        val levels = floatArrayOf(0.20f, 0.30f, 0.40f, 0.60f, 0.70f)
        for ((index, y) in levels.withIndex()) {
            val width = if (index % 2 == 0) 0.16f else 0.10f
            marks.boxAt(0f, y, 0.402f, width, 0.014f, 0.012f)
        }
    }

    private fun roundFlask(liquidColor: Color): Model = build {
        val radius = 0.42f
        val centerY = 0.54f
        val neckAngle = 76f
        val neckRadius = radius * cos(Math.toRadians(neckAngle.toDouble())).toFloat()
        val neckStart = centerY + radius * sin(Math.toRadians(neckAngle.toDouble())).toFloat()

        part("glass", GL20.GL_TRIANGLES, PLAIN, glass()).lathe(
            Profile()
                .arc(0f, centerY, radius, -90f, neckAngle, 18)
                .point(neckRadius, neckStart + 0.24f)
                .point(0.13f, neckStart + 0.27f, true)
                .point(0.13f, neckStart + 0.30f)
        )
        val liquidRadius = radius - 0.02f
        val level = centerY + 0.03f
        val levelAngle = Math.toDegrees(kotlin.math.asin(((level - centerY) / liquidRadius).toDouble())).toFloat()
        part("liquid", GL20.GL_TRIANGLES, PLAIN, liquid(liquidColor)).lathe(
            Profile()
                .arc(0f, centerY, liquidRadius, -90f, levelAngle, 12, true)
                .point(0f, level)
        )
        part("ring", GL20.GL_TRIANGLES, PLAIN, solid("cork", LabPalette.cork, 0.1f)).lathe(
            Profile()
                .point(0.16f, 0f)
                .point(0.36f, 0f, true)
                .point(0.36f, 0.10f, true)
                .point(0.16f, 0.12f, true)
                .point(0.16f, 0f, true)
        )
    }

    private fun testTubeRack(liquidColor: Color): Model = build {
        val rack = part("rack", GL20.GL_TRIANGLES, PLAIN, solid("rack", LabPalette.rack, 0.3f))
        rack.boxAt(0f, 0.025f, 0f, 1.0f, 0.05f, 0.36f)
        rack.boxAt(0f, 0.55f, 0f, 1.0f, 0.05f, 0.36f)
        rack.boxAt(-0.475f, 0.3f, 0f, 0.05f, 0.55f, 0.36f)
        rack.boxAt(0.475f, 0.3f, 0f, 0.05f, 0.55f, 0.36f)

        val positions = floatArrayOf(-0.3f, 0f, 0.3f)
        val levels = floatArrayOf(0.48f, 0.62f, 0.38f)

        val glass = part("glass", GL20.GL_TRIANGLES, PLAIN, glass())
        for (x in positions) {
            glass.lathe(
                Profile()
                    .point(0f, 0.05f)
                    .arc(0f, 0.15f, 0.10f, -90f, 0f, 6)
                    .point(0.10f, 0.96f)
                    .point(0.115f, 0.98f, true)
                    .point(0.115f, 1.0f),
                24,
                Matrix4().setToTranslation(x, 0f, 0f)
            )
        }
        val liquid = part("liquid", GL20.GL_TRIANGLES, PLAIN, liquid(liquidColor))
        for ((index, x) in positions.withIndex()) {
            liquid.lathe(
                Profile()
                    .point(0f, 0.06f)
                    .arc(0f, 0.15f, 0.09f, -90f, 0f, 6)
                    .point(0.09f, levels[index], true)
                    .point(0f, levels[index]),
                24,
                Matrix4().setToTranslation(x, 0f, 0f)
            )
        }
    }

    private class Bottle(val x: Float, val y: Float, val radius: Float, val height: Float, val tone: Int) {
        fun body(): Profile = Profile()
            .point(0f, 0f)
            .point(radius, 0f, true)
            .point(radius, height * 0.6f)
            .point(radius * 0.4f, height * 0.85f)
            .point(radius * 0.4f, height)

        fun cap(): Profile = Profile()
            .point(0f, 0f)
            .point(radius * 0.5f, 0f, true)
            .point(radius * 0.5f, 0.07f, true)
            .point(0f, 0.07f)
    }

    private fun build(block: ModelBuilder.() -> Unit): Model {
        val builder = ModelBuilder()
        builder.begin()
        builder.block()
        return builder.end()
    }

    private fun solid(id: String, color: Color, gloss: Float): Material {
        val material = Material(id, ColorAttribute.createDiffuse(color))
        if (gloss > 0f) {
            material.set(
                ColorAttribute.createSpecular(gloss, gloss, gloss, 1f),
                FloatAttribute.createShininess(24f + gloss * 40f)
            )
        }
        return material
    }

    private fun tiled(id: String, texture: Texture, scaleU: Float, scaleV: Float): Material {
        val diffuse = TextureAttribute.createDiffuse(texture)
        diffuse.scaleU = scaleU
        diffuse.scaleV = scaleV
        return Material(
            id,
            diffuse,
            ColorAttribute.createSpecular(0.18f, 0.18f, 0.18f, 1f),
            FloatAttribute.createShininess(20f)
        )
    }

    private fun glass(): Material = Material(
        "glass",
        ColorAttribute.createDiffuse(LabPalette.glass),
        ColorAttribute.createSpecular(1f, 1f, 1f, 1f),
        FloatAttribute.createShininess(72f),
        BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.24f),
        IntAttribute.createCullFace(GL20.GL_NONE),
        DepthTestAttribute(GL20.GL_LEQUAL, false)
    )

    private fun liquid(color: Color): Material = Material(
        "liquid",
        ColorAttribute.createDiffuse(color),
        ColorAttribute.createEmissive(0f, 0f, 0f, 1f),
        ColorAttribute.createSpecular(0.5f, 0.5f, 0.5f, 1f),
        FloatAttribute.createShininess(40f)
    )
}
