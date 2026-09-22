package com.apexlab.game.screens

import com.apexlab.game.ApexLabGame
import com.apexlab.game.data.SaveData
import com.apexlab.game.ui.HudSkin
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport

class MenuScreen(private val game: ApexLabGame) : Screen {

    private val stage = Stage(ScreenViewport())
    private val skin: Skin = HudSkin.create()

    init {
        val root = Table()
        root.setFillParent(true)
        stage.addActor(root)

        val title = Label("APEX LAB", skin)
        title.setFontScale(2.4f)
        title.setAlignment(Align.center)

        val subtitle = Label("Laboratory Experiment", skin)
        subtitle.color.a = 0.7f

        val bestTime = SaveData.bestTime()
        val statsText = if (bestTime >= 0f) {
            "Best time: %.1fs  ·  Runs: %d".format(bestTime, SaveData.runsCompleted())
        } else {
            "No experiments completed yet"
        }
        val statsLabel = Label(statsText, skin)
        statsLabel.color.a = 0.6f

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

        root.add(title).padBottom(8f).row()
        root.add(subtitle).padBottom(16f).row()
        root.add(statsLabel).padBottom(40f).row()
        root.add(startButton).width(260f).height(56f).padBottom(16f).row()
        root.add(exitButton).width(260f).height(56f)

        root.color.a = 0f
        root.addAction(Actions.fadeIn(0.4f))
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    override fun dispose() {
        stage.dispose()
        skin.dispose()
    }
}
