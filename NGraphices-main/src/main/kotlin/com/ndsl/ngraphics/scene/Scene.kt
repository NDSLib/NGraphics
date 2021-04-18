package com.ndsl.ngraphics.scene

import com.ndsl.ngraphics.base.Drawable
import com.ndsl.ngraphics.scene.layer.Layer

class Scene(val name: String) {
    private val layers = mutableMapOf<Int, Layer>()

    fun addLayer(l: Layer): Scene {
        layers.remove(l.index)
        layers[l.index] = l
        return this
    }

    fun newLayer(name: String, index: Int): Layer {
        val layer = Layer(name, index)
        addLayer(layer)
        return layer
    }

    fun newLayer(name: String): Layer {
        var last = 0
        layers.forEach { if (last < it.key) last = it.key }
        return newLayer(name, ++last)
    }

    fun forEach(f: (Layer) -> Unit) {
        layers.forEach { f(it.value) }
    }

    fun getLayer(i: Int) = layers[i]

    fun addDrawable(d: Drawable, i: Int) {
        getLayer(i)?.addDrawable(d)
    }
}