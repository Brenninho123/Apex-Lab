package com.apexlab.game.screens

import com.apexlab.game.ApexLabGame
import com.apexlab.game.data.RunResult
import com.apexlab.game.data.SaveData
import com.apexlab.game.scene.LabSample
import com.apexlab.game.scene.LabScene
import com.apexlab.game.ui.HudSkin
import com.apexlab.game.ui.formatSeconds
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport

class GameScreen(private val game: ApexLabGame) : Screen, LabScene.Listener {

    private val scene = LabScene()
    private val stage = Stage(ScreenViewport())
    private val skin = HudSkin.create()

    private val progressLabel = Label("", skin)
    private val timeLabel = Label("", skin, HudSkin.ACCENT)
    private val hintLabel = Label("", skin)
    private val hintPanel = Table()

    private var elapsedSeconds = 0f
    private var shownTenths = -1
    private var completed = false

    init {
        scene.listener = this

        val hudPanel = Table()
        hudPanel.background = skin.getDrawable(HudSkin.PANEL)
        hudPanel.pad(12f)
        hudPanel.add(Label("Drag to orbit · scroll or pinch to zoom · tap a sample to analyze it", skin, HudSkin.MUTED)).left().row()
        hudPanel.add(progressLabel).left().padTop(6f).row()
        hudPanel.add(timeLabel).left().padTop(2f)
        val hudTable = Table()
        hudTable.setFillParent(true)
        hudTable.top().left().pad(16f)
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
        menuTable.add(menuButton).width(130f).height(50f)
        stage.addActor(menuTable)

        hintPanel.background = skin.getDrawable(HudSkin.PANEL)
        hintPanel.pad(10f, 20f, 10f, 20f)
        hintPanel.add(hintLabel)
        hintPanel.touchable = Touchable.disabled
        hintPanel.color.a = 0f
        val hintTable = Table()
        hintTable.setFillParent(true)
        hintTable.bottom().padBottom(28f)
        hintTable.add(hintPanel)
        hintTable.touchable = Touchable.disabled
        stage.addActor(hintTable)

        updateProgress(pulse = false)
        updateTime(force = true)
    }

    override fun onHoverChanged(sample: LabSample?) {
        if (sample == null) {
            hintPanel.clearActions()
            hintPanel.addAction(Actions.fadeOut(0.15f))
        } else {
            showHint("${sample.type.displayName}  ·  tap to analyze", autoHide = false)
        }
    }

    override fun onSampleAnalyzed(sample: LabSample) {
        showHint("${sample.type.displayName} analyzed", autoHide = true)
        updateProgress(pulse = true)
        if (scene.isComplete && !completed) {
            completed = true
            showCompletionOverlay(SaveData.record(elapsedSeconds))
        }
    }

    private fun showHint(text: String, autoHide: Boolean) {
        hintLabel.setText(text)
        hintPanel.clearActions()
        hintPanel.addAction(
            if (autoHide) {
                Actions.sequence(Actions.fadeIn(0.1f), Actions.delay(1.4f), Actions.fadeOut(0.3f))
            } else {
                Actions.fadeIn(0.1f)
            }
        )
    }

    private fun updateProgress(pulse: Boolean) {
        progressLabel.setText("Samples analyzed: ${scene.analyzedCount} / ${scene.totalCount}")
        if (pulse) {
            progressLabel.setOrigin(Align.left)
            progressLabel.clearActions()
            progressLabel.setScale(1f)
            progressLabel.addAction(Actions.sequence(Actions.scaleTo(1.2f, 1.2f, 0.06f), Actions.scaleTo(1f, 1f, 0.16f)))
        }
    }

    private fun updateTime(force: Boolean) {
        val tenths = (elapsedSeconds * 10f).toInt()
        if (force || tenths != shownTenths) {
            shownTenths = tenths
            timeLabel.setText("Time: ${formatSeconds(elapsedSeconds)}")
        }
    }

    private fun goToMenu() {
        game.setScreen(MenuScreen(game))
        dispose()
    }

    private fun playAgain() {
        game.setScreen(GameScreen(game))
        dispose()
    }

    private fun showCompletionOverlay(result: RunResult) {
        val panel = Table()
        panel.background = skin.getDrawable(HudSkin.PANEL)
        panel.pad(28f)

        panel.add(Label("Experiment complete!", skin, HudSkin.TITLE)).padBottom(12f).row()
        panel.add(Label("Time: ${formatSeconds(result.seconds)}", skin)).padBottom(4f).row()
        val summary = if (result.isNewBest) "New best time!" else "Best: ${formatSeconds(result.bestTime)}"
        panel.add(Label(summary, skin, HudSkin.ACCENT)).padBottom(20f).row()

        val playAgainButton = TextButton("Play Again", skin)
        playAgainButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                playAgain()
            }
        })
        val menuButton = TextButton("Menu", skin)
        menuButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                goToMenu()
            }
        })
        val buttons = Table()
        buttons.add(playAgainButton).width(180f).height(52f).padRight(12f)
        buttons.add(menuButton).width(130f).height(52f)
        panel.add(buttons)

        val overlay = Table()
        overlay.setFillParent(true)
        overlay.center()
        overlay.add(panel)
        overlay.color.a = 0f
        overlay.addAction(Actions.fadeIn(0.4f))
        stage.addActor(overlay)
    }

    override fun show() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true)

        val gestures = GestureDetector(object : GestureDetector.GestureAdapter() {
            private var zoomStartRadius = scene.orbit.radius
            private var lastInitialDistance = -1f

            override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
                return scene.analyzeAt(x.toInt(), y.toInt()) != null
            }

            override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
                scene.orbit.orbit(-deltaX * 0.3f, -deltaY * 0.3f)
                return true
            }

            override fun zoom(initialDistance: Float, distance: Float): Boolean {
                if (initialDistance != lastInitialDistance) {
                    zoomStartRadius = scene.orbit.radius
                    lastInitialDistance = initialDistance
                }
                if (distance > 0f) {
                    scene.orbit.radius = zoomStartRadius * (initialDistance / distance)
                }
                return true
            }
        })

        val extraInput = object : InputAdapter() {
            override fun scrolled(amountX: Float, amountY: Float): Boolean {
                scene.orbit.zoom(amountY * 0.5f)
                return true
            }

            override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
                scene.hover(screenX, screenY)
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
        multiplexer.addProcessor(gestures)
        multiplexer.addProcessor(extraInput)
        Gdx.input.inputProcessor = multiplexer
    }

    override fun render(delta: Float) {
        if (!completed) {
            elapsedSeconds += delta
            updateTime(force = false)
        }
        scene.update(delta)
        scene.render()

        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        scene.resize(width, height)
        stage.viewport.update(width, height, true)
    }

    override fun pause() {}

    override fun resume() {}

    override fun hide() {
        Gdx.input.setCatchKey(Input.Keys.BACK, false)
    }

    override fun dispose() {
        scene.dispose()
        stage.dispose()
        skin.dispose()
    }
}
