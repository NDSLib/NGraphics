package com.ndsl.ngraphics.drawable

import com.ndsl.ngraphics.base.IDrawable
import com.ndsl.ngraphics.display.NDisplay
import com.ndsl.ngraphics.pos.Pos
import com.ndsl.ngraphics.pos.Rect
import com.ndsl.ngraphics.pos.ToRect
import com.ndsl.ngraphics.util.setColor
import java.awt.Color
import java.awt.Graphics
import java.awt.Image

abstract class DrawableBase(var color:Color = Color.BLACK):IDrawable{
    override fun onDraw(g: Graphics) {
        g.setColor(color){
            draw(it)
        }
    }
    abstract fun draw(g:Graphics)
}

class Line(var from: Pos, var to:Pos,color:Color = Color.BLACK): DrawableBase(color) {
    override fun draw(g: Graphics) {
        g.drawLine(from.x,from.y,to.x,to.y)
    }
}

open class Box(var r:Rect, var isFill: Boolean = false, color:Color = Color.BLACK):DrawableBase(color){
    override fun draw(g: Graphics) {
        if(isFill){
            g.fillRect(r.left_up.x,r.left_up.y,r.right_down.x,r.right_down.y)
        }else{
            g.drawRect(r.left_up.x,r.left_up.y,r.right_down.x,r.right_down.y)
        }
    }
}

class Picture(var r:Rect,var img:Image):DrawableBase(){
    override fun draw(g: Graphics) {
        g.drawImage(img,r.left_up.x,r.left_up.y,r.getWidth(),r.getHeight(),null)
    }
}