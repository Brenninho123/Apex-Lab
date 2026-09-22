package com.apexlab.game.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

object HudSkin {

    fun create(): Skin {
        val skin = Skin()
        skin.add("default-font", BitmapFont())

        addFlatDrawable(skin, "button-up", Color(0.16f, 0.42f, 0.62f, 1f))
        addFlatDrawable(skin, "button-down", Color(0.10f, 0.30f, 0.46f, 1f))

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.up = skin.getDrawable("button-up")
        buttonStyle.down = skin.getDrawable("button-down")
        buttonStyle.font = skin.getFont("default-font")
        skin.add("default", buttonStyle)

        val labelStyle = Label.LabelStyle(skin.getFont("default-font"), Color.WHITE)
        skin.add("default", labelStyle)

        return skin
    }

    private fun addFlatDrawable(skin: Skin, name: String, color: Color) {
        val pixmap = Pixmap(8, 8, Pixmap.Format.RGBA8888)
        pixmap.setColor(color)
        pixmap.fill()
        val texture = Texture(pixmap)
        pixmap.dispose()
        skin.add("$name-texture", texture)
        skin.add(name, TextureRegionDrawable(TextureRegion(texture)), Drawable::class.java)
    }
}
