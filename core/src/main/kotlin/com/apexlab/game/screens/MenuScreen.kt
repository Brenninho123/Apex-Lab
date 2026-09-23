package com.apexlab.game.screens

import com.apexlab.game.ApexLabGame
import com.apexlab.game.data.SaveData
import com.apexlab.game.scene.LabScene
import com.apexlab.game.ui.HudSkin
import com.apexlab.game.ui.formatSeconds
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import kotlin.math.sin

class MenuScreen(private val game: ApexLabGame) : Screen {

    private val scene = LabScene()
    private val stage = Stage(ScreenViewport())
    private val skin: Skin = HudSkin.create()
    private var time = 0f

    init {
        scene.orbit.radius = 7.4f
        scene.orbit.elevation = 20f
        scene.orbit.target.set(-2.1f, 0.7f, 0f)

        val stats = SaveData.stats()
        val statsText = stats.bestTime?.let { "Best time: ${formatSeconds(it)}  ·  Runs: ${stats.runsCompleted}" }
            ?: "No experiments completed yet"

        val startButton = TextButton("Start Experiment", skin)
        startButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(GameScreen(game))
                dispose()
            }
        })

        val exitButton = TextButton("Exit", skin)
        exitButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.exit()
            }
        })

        val panel = Table()
        panel.background = skin.getDrawable(HudSkin.PANEL)
        panel.pad(32f)
        panel.add(Label("APEX LAB", skin, HudSkin.TITLE)).left().row()
        panel.add(Label("Laboratory experiment", skin, HudSkin.MUTED)).left().padBottom(6f).row()
        panel.add(Label(statsText, skin, HudSkin.ACCENT)).left().padBottom(28f).row()
        panel.add(startButton).width(300f).height(58f).padBottom(14f).row()
        panel.add(exitButton).width(300f).height(58f)

        val root = Table()
        root.setFillParent(true)
        root.left().padLeft(56f)
        root.add(panel)
        root.color.a = 0f
        root.addAction(Actions.fadeIn(0.5f))
        stage.addActor(root)
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        time += delta
        scene.orbit.azimuth = 24f * sin(time * 0.22f)
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

    override fun hide() {}

    override fun dispose() {
        scene.dispose()
        stage.dispose()
        skin.dispose()
    }
}
