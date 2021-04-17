package com.ndsl.ngraphics.scene

import com.ndsl.ngraphics.base.Drawable
import com.ndsl.ngraphics.scene.layer.Layer

class Scene(val name: String) {
    private val layers = mutableListOf<Layer>()

    fun addLayer(l: Layer) : Scene{
        if (layers.any { it.index == l.index }) {
            println("[Scene-$name] Duplicated Layer Index : ${l.index}")
            layers.removeAll { it.index == l.index }
        }
        layers.add(l)
        return this
    }

    fun newLayer(name:String,index:Int):Layer{
        val layer = Layer(name,index)
        addLayer(layer)
        return layer
    }

    fun newLayer(name:String): Layer {
        var last = 0
        layers.forEach { if(last < it.index) last = it.index }
        return newLayer(name,++last)
    }

    fun forEach(f:(Layer)->Unit){
        layers.forEach { f(it) }
    }

    fun getLayer(i:Int) = layers.getOrNull(i)

    fun addDrawable(d:Drawable,i:Int){
        getLayer(i)?.addDrawable(d)
    }
}