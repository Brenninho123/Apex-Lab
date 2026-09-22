package com.apexlab.game.lwjgl3

import com.apexlab.game.ApexLabGame
import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
    val config = Lwjgl3ApplicationConfiguration()
    config.setTitle("Apex Lab")
    config.setWindowedMode(1280, 720)
    config.useVsync(true)
    config.setForegroundFPS(60)
    config.setWindowIcon(Files.FileType.Internal, "icon128.png", "icon32.png", "icon16.png")
    Lwjgl3Application(ApexLabGame(), config)
}
