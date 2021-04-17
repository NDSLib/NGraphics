package com.ndsl.ngraphics.scene.layer

import com.ndsl.ngraphics.base.Drawable
import com.ndsl.ngraphics.base.IDrawable

class Layer(val name: String,val index:Int = 0) {
    private val list = mutableListOf<Drawable>()
    fun addDrawable(d: Drawable) {
        list.add(d)
    }

    fun addDrawable(i:IDrawable){
        addDrawable(Drawable(i))
    }

    fun forEach(f: (Drawable) -> Unit) {
        list.forEach { f(it) }
    }
}