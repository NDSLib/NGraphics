package com.ndsl.ngraphics.base

import com.ndsl.ngraphics.pos.Rect
import java.awt.Graphics

interface IDrawable {
    fun onDraw(g: Graphics)
}

class Drawable(private val iDrawable: IDrawable) {
    fun onDraw(g: Graphics) {
        iDrawable.onDraw(g)
    }
}