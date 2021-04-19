package com.ndsl.ngraphics.base

import com.ndsl.ngraphics.display.NDisplay
import java.awt.Color
import java.awt.Graphics

class DrawManager(val d: NDisplay) {
    var TargetFPS = 0.0
        private set(value) {
            field = value
            milSecPerSec = 1000 / value
            timer = Timer(milSecPerSec)
            timer.start()
        }
    var milSecPerSec = 0.0
        private set

    init {
        TargetFPS = 60.0
    }

    fun setFPS(fps: Double) {
        TargetFPS = fps
    }

    private var timer = Timer(0.0)

    fun draw() {
        //バッファ内容が失われたら再描画させる
//        if(d.isContentsLost()){
        try {
            timer.start()
            d.validateVolatileImage()
            val g = d.volatileImage!!.graphics
            d.update(g)
            g.dispose()
        } catch (e: NullPointerException) {
            println("Null Pointer (Should be ignored)")
        }
//        }
    }

    var drawCount = 0
    var totalFreeTime = 0.0
    var totalTime = 0L
    var latestTime = 0.0
    var worseFPS = Double.MAX_VALUE
    var drawMode = DrawMode.KeepUp

    fun drawAll(g: Graphics) {
        clear(d)
        d.sceneManager.getScene()!!.forEach { layer ->
            layer.forEach {
                it.onDraw(g)
            }
        }
        d.repaint()

        drawCount++
        totalFreeTime += timer.getLast()
        latestTime = timer.getDelta()
        totalTime += timer.getDelta().toInt()

        if (timer.getLast() < 0) {
            println("Can't Keep UP! delta:${timer.getLast()} timerMil:${timer.milSec} latest:${getLatestFPS()}")
        }

        checkWorseFPS()

        if(drawMode == DrawMode.TargetFPS){
            timer.waitUntil()
        }
    }

    var backGroundColor = Color.white

    private fun clear(d: NDisplay) {
        val g = d.volatileImage!!.graphics
        g.color = backGroundColor
        g.fillRect(0,0,d.width,d.height)
    }

    fun getLatestFPS(): Double {
        return 1 / (latestTime / 1000)
    }

    private fun checkWorseFPS(){
        val f = getLatestFPS()
        if(f < worseFPS) worseFPS = f
    }
}

enum class DrawMode {
    KeepUp,TargetFPS

}

class Timer(val milSec: Double) {
    private var startTime: Long = 0

    fun start() {
        startTime = System.currentTimeMillis()
    }

    fun isUp(): Boolean {
        val delta = System.currentTimeMillis() - startTime
        if (delta.toDouble() >= milSec) return true
        return false
    }

    fun waitUntil(){
        val l = getLast()
        if(l < 0) return
        println("waiting $l ns")
        Thread.sleep(0,l.toInt())
    }

    /**
     * タイマー残り何秒
     */
    fun getLast(): Double {
        return milSec - getDelta()
    }

    /**
     * タイマー何秒経過
     */
    fun getDelta(): Double {
        return ((System.currentTimeMillis() - startTime).toDouble())
    }
}