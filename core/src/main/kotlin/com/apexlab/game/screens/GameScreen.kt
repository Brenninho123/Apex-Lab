package com.apexlab.game.screens

import com.apexlab.game.ApexLabGame
import com.apexlab.game.data.SaveData
import com.apexlab.game.ui.HudSkin
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
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
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.math.collision.BoundingBox
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import kotlin.math.cos
import kotlin.math.sin

private const val SAMPLE_COUNT = 5
private const val MIN_RADIUS = 3f
private const val MAX_RADIUS = 10f
private const val POP_DURATION = 0.25f
private val PENDING_COLOR = Color(0.2f, 0.6f, 0.9f, 1f)
private val HOVER_COLOR = Color(0.45f, 0.75f, 1f, 1f)
private val ANALYZED_COLOR = Color(0.3f, 0.85f, 0.4f, 1f)

private class Sample(
    val instance: ModelInstance,
    val material: Material,
    val bounds: BoundingBox,
    val baseX: Float,
    val phase: Float
) {
    var analyzed = false
    var popTimer = 0f
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
    private var hoveredSample: Sample? = null

    private var azimuth = 45f
    private var elevation = 35f
    private var radius = 5.5f

    private var elapsedSeconds = 0f
    private var completed = false

    private val stage = Stage(ScreenViewport())
    private val skin = HudSkin.create()
    private val progressLabel = Label("", skin)

    init {
        environment.set(ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f))
        environment.add(DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f))

        camera.near = 0.1f
        camera.far = 300f
        updateCameraPosition()

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

            samples += Sample(instance, material, bounds, x, i * 1.3f)
        }

        val hudTable = Table()
        hudTable.setFillParent(true)
        hudTable.top().left().pad(16f)
        val hudPanel = Table()
        hudPanel.background = skin.getDrawable("panel")
        hudPanel.pad(10f)
        hudPanel.add(Label("Drag to orbit · scroll/pinch to zoom · tap a sample to analyze it", skin)).left().row()
        hudPanel.add(progressLabel).left().padTop(4f)
        hudTable.add(hudPanel)
        stage.addActor(hudTable)

        val menuButton = TextButton("Menu", skin)
        menuButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                goToMenu()
            }
        })
        val menuTable = Table()
        menuTable.setFillParent(true)
        menuTable.top().right().pad(16f)
        menuTable.add(menuButton).width(120f).height(48f)
        stage.addActor(menuTable)

        updateProgressLabel()
    }

    private fun updateCameraPosition() {
        val azRad = Math.toRadians(azimuth.toDouble())
        val elRad = Math.toRadians(elevation.toDouble())
        val horizontalRadius = radius * cos(elRad).toFloat()
        val x = horizontalRadius * sin(azRad).toFloat()
        val z = horizontalRadius * cos(azRad).toFloat()
        val y = radius * sin(elRad).toFloat()
        camera.position.set(x, y, z)
        camera.up.set(Vector3.Y)
        camera.lookAt(Vector3.Zero)
        camera.update()
    }

    private fun goToMenu() {
        game.setScreen(MenuScreen(game))
        dispose()
    }

    override fun show() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true)

        val gestureDetector = GestureDetector(object : GestureDetector.GestureAdapter() {
            override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
                return trySelectSample(x.toInt(), y.toInt())
            }

            override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
                azimuth -= deltaX * 0.3f
                elevation = (elevation - deltaY * 0.3f).coerceIn(10f, 80f)
                updateCameraPosition()
                return true
            }

            private var zoomStartRadius = radius
            private var lastInitialDistance = -1f

            override fun zoom(initialDistance: Float, distance: Float): Boolean {
                if (initialDistance != lastInitialDistance) {
                    zoomStartRadius = radius
                    lastInitialDistance = initialDistance
                }
                if (distance > 0f) {
                    radius = (zoomStartRadius * (initialDistance / distance)).coerceIn(MIN_RADIUS, MAX_RADIUS)
                    updateCameraPosition()
                }
                return true
            }
        })

        val extraInput = object : InputAdapter() {
            override fun scrolled(amountX: Float, amountY: Float): Boolean {
                radius = (radius + amountY * 0.5f).coerceIn(MIN_RADIUS, MAX_RADIUS)
                updateCameraPosition()
                return true
            }

            override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
                updateHover(screenX, screenY)
                return false
            }

            override fun keyDown(keycode: Int): Boolean {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    goToMenu()
                    return true
                }
                return false
            }
        }

        val multiplexer = InputMultiplexer()
        multiplexer.addProcessor(stage)
        multiplexer.addProcessor(gestureDetector)
        multiplexer.addProcessor(extraInput)
        Gdx.input.inputProcessor = multiplexer
    }

    private fun updateHover(screenX: Int, screenY: Int) {
        val ray = camera.getPickRay(screenX.toFloat(), screenY.toFloat())
        var newHover: Sample? = null
        for (sample in samples) {
            if (!sample.analyzed && Intersector.intersectRayBoundsFast(ray, sample.bounds)) {
                newHover = sample
                break
            }
        }
        if (newHover !== hoveredSample) {
            hoveredSample?.material?.set(ColorAttribute.createDiffuse(PENDING_COLOR))
            newHover?.material?.set(ColorAttribute.createDiffuse(HOVER_COLOR))
            hoveredSample = newHover
        }
    }

    private fun trySelectSample(screenX: Int, screenY: Int): Boolean {
        val ray = camera.getPickRay(screenX.toFloat(), screenY.toFloat())
        for (sample in samples) {
            if (!sample.analyzed && Intersector.intersectRayBoundsFast(ray, sample.bounds)) {
                sample.analyzed = true
                sample.popTimer = POP_DURATION
                sample.material.set(ColorAttribute.createDiffuse(ANALYZED_COLOR))
                if (hoveredSample === sample) hoveredSample = null
                analyzedCount++
                updateProgressLabel()
                if (analyzedCount == samples.size && !completed) {
                    completed = true
                    val isNewBest = SaveData.submitRun(elapsedSeconds)
                    showCompletionOverlay(isNewBest)
                }
                return true
            }
        }
        return false
    }

    private fun updateProgressLabel() {
        progressLabel.setText("Samples analyzed: $analyzedCount / ${samples.size}")
        if (analyzedCount > 0) {
            progressLabel.setOrigin(Align.left)
            progressLabel.clearActions()
            progressLabel.setScale(1f)
            progressLabel.addAction(Actions.sequence(Actions.scaleTo(1.2f, 1.2f, 0.06f), Actions.scaleTo(1f, 1f, 0.16f)))
        }
    }

    private fun showCompletionOverlay(isNewBest: Boolean) {
        val overlay = Table()
        overlay.setFillParent(true)
        overlay.center()

        val panel = Table()
        panel.background = skin.getDrawable("panel")
        panel.pad(24f)

        panel.add(Label("Experiment complete!", skin)).padBottom(8f).row()
        panel.add(Label("Time: %.1fs".format(elapsedSeconds), skin)).padBottom(4f).row()
        if (isNewBest) {
            panel.add(Label("New best time!", skin)).padBottom(16f).row()
        } else {
            val best = SaveData.bestTime()
            panel.add(Label("Best: %.1fs".format(if (best >= 0f) best else elapsedSeconds), skin)).padBottom(16f).row()
        }

        val playAgainButton = TextButton("Play Again", skin)
        playAgainButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(GameScreen(game))
                dispose()
            }
        })
        val menuButton = TextButton("Menu", skin)
        menuButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                goToMenu()
            }
        })

        val buttonRow = Table()
        buttonRow.add(playAgainButton).width(160f).height(48f).padRight(12f)
        buttonRow.add(menuButton).width(120f).height(48f)
        panel.add(buttonRow)

        overlay.add(panel)
        overlay.color.a = 0f
        overlay.addAction(Actions.fadeIn(0.35f))
        stage.addActor(overlay)
    }

    private fun updateSampleAnimations(delta: Float) {
        for (sample in samples) {
            val transform = sample.instance.transform
            if (!sample.analyzed) {
                val bobY = sin(elapsedSeconds * 2f + sample.phase) * 0.05f
                val rotationDeg = (elapsedSeconds * 20f + sample.phase * 30f) % 360f
                transform.idt()
                transform.translate(sample.baseX, bobY, 0f)
                transform.rotate(Vector3.Y, rotationDeg)
            } else if (sample.popTimer > 0f) {
                sample.popTimer = (sample.popTimer - delta).coerceAtLeast(0f)
                val scale = 1f + (sample.popTimer / POP_DURATION) * 0.3f
                transform.idt()
                transform.translate(sample.baseX, 0f, 0f)
                transform.scale(scale, scale, scale)
            }
        }
    }

    override fun render(delta: Float) {
        if (!completed) {
            elapsedSeconds += delta
        }
        updateSampleAnimations(delta)

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

    override fun hide() {
        Gdx.input.setCatchKey(Input.Keys.BACK, false)
    }

    override fun dispose() {
        modelBatch.dispose()
        ownedModels.forEach { it.dispose() }
        stage.dispose()
        skin.dispose()
    }
}
