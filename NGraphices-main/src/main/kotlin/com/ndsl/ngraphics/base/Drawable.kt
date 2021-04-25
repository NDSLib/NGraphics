package com.ndsl.ngraphics.base

import com.ndsl.ngraphics.display.NDisplay
import com.ndsl.ngraphics.pos.Rect
import java.awt.Graphics

interface IDrawable {
    fun onDraw(g: Graphics,showing:Rect,d:NDisplay)
}

class Drawable(private val iDrawable: IDrawable) {
    fun onDraw(g: Graphics,r:Rect,d:NDisplay) {
        iDrawable.onDraw(g,r,d)
    }
}