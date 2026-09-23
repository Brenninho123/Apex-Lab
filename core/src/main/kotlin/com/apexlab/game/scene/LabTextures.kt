package com.apexlab.game.scene

import com.apexlab.game.theme.LabPalette
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture

object LabTextures {

    fun tiles(tile: Color, grout: Color, size: Int = 256, cells: Int = 4): Texture {
        val pixmap = Pixmap(size, size, Pixmap.Format.RGBA8888)
        pixmap.setColor(grout)
        pixmap.fill()
        val cell = size / cells
        val gap = maxOf(2, size / 96)
        for (row in 0 until cells) {
            for (column in 0 until cells) {
                val shade = 0.94f + ((row * 7 + column * 13) % 5) * 0.015f
                pixmap.setColor(tile.r * shade, tile.g * shade, tile.b * shade, 1f)
                pixmap.fillRectangle(column * cell + gap / 2, row * cell + gap / 2, cell - gap, cell - gap)
            }
        }
        val texture = Texture(pixmap, true)
        pixmap.dispose()
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear)
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat)
        return texture
    }

    fun wallTiles(): Texture = tiles(LabPalette.wallTile, LabPalette.wallGrout)

    fun floorTiles(): Texture = tiles(LabPalette.floorTile, LabPalette.floorGrout)

    fun periodicPoster(): Texture {
        val columns = 18
        val rows = 7
        val cellWidth = 26
        val cellHeight = 30
        val pixmap = Pixmap(512, 256, Pixmap.Format.RGBA8888)
        pixmap.setColor(0.96f, 0.97f, 0.95f, 1f)
        pixmap.fill()
        val originX = (512 - columns * cellWidth) / 2
        val originY = (256 - rows * cellHeight) / 2
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                if (!hasElement(row, column)) continue
                val color = groupColor(column)
                pixmap.setColor(color.r, color.g, color.b, 1f)
                pixmap.fillRectangle(
                    originX + column * cellWidth + 1,
                    originY + row * cellHeight + 1,
                    cellWidth - 2,
                    cellHeight - 2
                )
            }
        }
        val texture = Texture(pixmap)
        pixmap.dispose()
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        return texture
    }

    private fun hasElement(row: Int, column: Int): Boolean = when (row) {
        0 -> column == 0 || column == 17
        1, 2 -> column <= 1 || column >= 12
        else -> true
    }

    private fun groupColor(column: Int): Color = when {
        column == 0 -> Color(0.90f, 0.40f, 0.35f, 1f)
        column == 1 -> Color(0.95f, 0.65f, 0.30f, 1f)
        column <= 11 -> Color(0.35f, 0.60f, 0.85f, 1f)
        column <= 14 -> Color(0.45f, 0.75f, 0.50f, 1f)
        column <= 16 -> Color(0.90f, 0.80f, 0.35f, 1f)
        else -> Color(0.65f, 0.50f, 0.85f, 1f)
    }
}
