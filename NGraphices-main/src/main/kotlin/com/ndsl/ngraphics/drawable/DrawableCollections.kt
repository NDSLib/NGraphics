package com.ndsl.ngraphics.drawable

import com.ndsl.ngraphics.base.IDrawable
import com.ndsl.ngraphics.base.time
import com.ndsl.ngraphics.display.NDisplay
import com.ndsl.ngraphics.pos.Pos
import com.ndsl.ngraphics.pos.Rect
import com.ndsl.ngraphics.util.setColor
import sun.awt.image.SurfaceManager
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import kotlin.math.min

abstract class DrawableBase(var color: Color = Color.BLACK) : IDrawable {
    var lastTime: Long = System.nanoTime()
    override fun onDraw(g: Graphics, showing: Rect, d: NDisplay) {
        val time = System.nanoTime()
        g.setColor(color) {
            draw(it, time - lastTime, showing)
        }
        lastTime = time
    }

    /**
     * @param deltaTime DeltaTime From LastFrame In Nano Second
     */
    abstract fun draw(g: Graphics, deltaTime: Long, showing: Rect)
}

abstract class DisplayDrawableBase(var color: Color = Color.BLACK) : IDrawable {
    var lastTime: Long = System.nanoTime()
    override fun onDraw(g: Graphics, showing: Rect, d: NDisplay) {
        val time = System.nanoTime()
        g.setColor(color) {
            draw(it, time - lastTime, showing, d)
        }
        lastTime = time
    }

    /**
     * @param deltaTime DeltaTime From LastFrame In Nano Second
     */
    abstract fun draw(g: Graphics, deltaTime: Long, showing: Rect, d: NDisplay)
}

class Line(var from: Pos, var to: Pos, color: Color = Color.BLACK) : DrawableBase(color) {
    override fun draw(g: Graphics, deltaTime: Long, showing: Rect) {
        g.drawLine(from.x, from.y, to.x, to.y)
    }
}

open class Box(var r: Rect, var isFill: Boolean = false, color: Color = Color.BLACK) : DrawableBase(color) {
    override fun draw(g: Graphics, deltaTime: Long, showing: Rect) {
        if (isFill) {
            g.fillRect(r.left_up.x, r.left_up.y, r.right_down.x, r.right_down.y)
        } else {
            g.drawRect(r.left_up.x, r.left_up.y, r.right_down.x, r.right_down.y)
        }
    }
}

open class Picture(var r: Rect, var img: Image?, val mode: DrawMode = DrawMode.NORMAL) : DisplayDrawableBase() {

    enum class DrawMode {
        NORMAL, WithinWindow
    }

    override fun draw(g: Graphics, deltaTime: Long, showing: Rect, d: NDisplay) {
        time({
            if (img != null) {
                when (mode) {
                    DrawMode.NORMAL -> {
                        g.drawImage(img,
                            r.left_up.x,
                            r.left_up.y,
                            min(r.getWidth(), showing.getWidth()),
                            min(r.getHeight(), showing.getHeight()),
                            0,
                            0,
                            min(r.getWidth(), showing.getWidth()),
                            min(r.getHeight(), showing.getHeight()),
                            null)
                    }

                    DrawMode.WithinWindow -> {
                        g.drawImage(
                            img,
                            0, 0,
                            showing.getWidth(), showing.getHeight(),
                            null
                        )
                    }
                }
            }
        }, {
        })
    }
}