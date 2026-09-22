package com.apexlab.game

import com.badlogic.gdx.Game
import com.apexlab.game.screens.MenuScreen

class ApexLabGame : Game() {
    override fun create() {
        setScreen(MenuScreen(this))
    }
}
