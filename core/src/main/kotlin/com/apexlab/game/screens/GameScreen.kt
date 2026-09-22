package com.apexlab.game.screens

import com.apexlab.game.ApexLabGame
import com.apexlab.game.ui.HudSkin
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport

private const val SAMPLE_COUNT = 5
private val PENDING_COLOR = Color(0.2f, 0.6f, 0.9f, 1f)
private val ANALYZED_COLOR = Color(0.3f, 0.85f, 0.4f, 1f)

private class Sample(val instance: ModelInstance, val material: Material, val bounds: BoundingBox) {
    var analyzed = false
}

class GameScreen(private val game: ApexLabGame) : Screen {

    private val modelBatch = ModelBatch()
    private val environment = Environment()
    private val camera = PerspectiveCamera(
        60f,
        Gdx.graphics.width.toFloat(),
        Gdx.graphics.height.toFloat()
    )
    private val modelBuilder = ModelBuilder()
    private val attributes = (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()

    private val ownedModels = mutableListOf<Model>()
    private val tableInstance: ModelInstance
    private val samples = mutableListOf<Sample>()
    private var analyzedCount = 0

    private val stage = Stage(ScreenViewport())
    private val skin = HudSkin.create()
    private val progressLabel = Label("", skin)

    init {
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f))
        environment.add(DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f))

        camera.position.set(0f, 3.2f, 4.2f)
        camera.lookAt(0f, 0f, 0f)
        camera.near = 0.1f
        camera.far = 300f
        camera.update()

        val tableModel = modelBuilder.createBox(
            6f, 0.2f, 3f,
            Material(ColorAttribute.createDiffuse(Color(0.22f, 0.22f, 0.26f, 1f))),
            attributes
        )
        ownedModels += tableModel
        tableInstance = ModelInstance(tableModel)
        tableInstance.transform.setTranslation(0f, -0.6f, 0f)

        for (i in 0 until SAMPLE_COUNT) {
            val material = Material(ColorAttribute.createDiffuse(PENDING_COLOR))
            val model = modelBuilder.createBox(0.6f, 0.6f, 0.6f, material, attributes)
            ownedModels += model

            val instance = ModelInstance(model)
            val x = (i - (SAMPLE_COUNT - 1) / 2f) * 1.1f
            instance.transform.setTranslation(x, 0f, 0f)

            val bounds = BoundingBox()
            instance.calculateBoundingBox(bounds)
            bounds.mul(instance.transform)

            samples += Sample(instance, material, bounds)
        }

        val hudTable = Table()
        hudTable.setFillParent(true)
        hudTable.top().left().pad(16f)
        hudTable.add(Label("Tap a sample to analyze it", skin)).left().row()
        hudTable.add(progressLabel).left().padTop(4f)
        stage.addActor(hudTable)

        val menuButton = TextButton("Menu", skin)
        menuButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(MenuScreen(game))
                dispose()
            }
        })
        val menuTable = Table()
        menuTable.setFillParent(true)
        menuTable.top().right().pad(16f)
        menuTable.add(menuButton).width(120f).height(48f)
        stage.addActor(menuTable)

        updateProgressLabel()
    }

    override fun show() {
        val multiplexer = InputMultiplexer()
        multiplexer.addProcessor(stage)
        multiplexer.addProcessor(object : InputAdapter() {
            override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
                return trySelectSample(screenX, screenY)
            }
        })
        Gdx.input.inputProcessor = multiplexer
    }

    private fun trySelectSample(screenX: Int, screenY: Int): Boolean {
        val ray = camera.getPickRay(screenX.toFloat(), screenY.toFloat())
        for (sample in samples) {
            if (!sample.analyzed && Intersector.intersectRayBoundsFast(ray, sample.bounds)) {
                sample.analyzed = true
                sample.material.set(ColorAttribute.createDiffuse(ANALYZED_COLOR))
                analyzedCount++
                updateProgressLabel()
                return true
            }
        }
        return false
    }

    private fun updateProgressLabel() {
        val suffix = if (analyzedCount == samples.size) " - All samples analyzed!" else ""
        progressLabel.setText("Samples analyzed: $analyzedCount / ${samples.size}$suffix")
    }

    override fun render(delta: Float) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        modelBatch.begin(camera)
        modelBatch.render(tableInstance, environment)
        for (sample in samples) {
            modelBatch.render(sample.instance, environment)
        }
        modelBatch.end()

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        camera.viewportWidth = width.toFloat()
        camera.viewportHeight = height.toFloat()
        camera.update()
        stage.viewport.update(width, height, true)
    }

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    override fun dispose() {
        modelBatch.dispose()
        ownedModels.forEach { it.dispose() }
        stage.dispose()
        skin.dispose()
    }
}
