package com.apexlab.game.ui

import com.apexlab.game.theme.LabPalette
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

    const val PANEL = "panel"
    const val TITLE = "title"
    const val MUTED = "muted"
    const val ACCENT = "accent"

    fun create(): Skin {
        val skin = Skin()
        val bodyFont = scaledFont(1.3f)
        val titleFont = scaledFont(3.4f)
        skin.add("body-font", bodyFont)
        skin.add("title-font", titleFont)

        addFlatDrawable(skin, "button-up", LabPalette.button)
        addFlatDrawable(skin, "button-over", LabPalette.buttonOver)
        addFlatDrawable(skin, "button-down", LabPalette.buttonDown)
        addFlatDrawable(skin, PANEL, LabPalette.panel)

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.up = skin.getDrawable("button-up")
        buttonStyle.over = skin.getDrawable("button-over")
        buttonStyle.down = skin.getDrawable("button-down")
        buttonStyle.font = bodyFont
        buttonStyle.fontColor = LabPalette.text
        skin.add("default", buttonStyle)

        skin.add("default", Label.LabelStyle(bodyFont, LabPalette.text))
        skin.add(MUTED, Label.LabelStyle(bodyFont, LabPalette.mutedText))
        skin.add(ACCENT, Label.LabelStyle(bodyFont, LabPalette.accent))
        skin.add(TITLE, Label.LabelStyle(titleFont, LabPalette.accent))

        return skin
    }

    private fun scaledFont(scale: Float): BitmapFont {
        val font = BitmapFont()
        font.data.setScale(scale)
        font.region.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        return font
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
