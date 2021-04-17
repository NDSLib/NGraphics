package com.ndsl.ngraphics.scene

import com.ndsl.ngraphics.base.Drawable
import com.ndsl.ngraphics.display.NDisplay

class SceneManager(val display:NDisplay) {
    private val scenes = mutableListOf<Scene>()

    fun addScene(scene:Scene){
        scenes.add(scene)
    }

    fun newScene(name:String): Scene {
        val scene = Scene(name)
        addScene(scene)
        return scene
    }

    private var i = 0

    fun setIndex(i:Int){
        this.i = i
    }

    fun getScene(): Scene? = scenes.getOrNull(i)

    fun addDrawable(d: Drawable, index:Int){
        getScene()?.addDrawable(d,index)
    }
}