package com.ndsl.ngraphics

import com.ndsl.ngraphics.display.NDisplay
import com.ndsl.ngraphics.drawable.Box
import com.ndsl.ngraphics.drawable.Picture
import com.ndsl.ngraphics.pos.Pos
import com.ndsl.ngraphics.pos.Rect
import java.awt.Graphics
import java.awt.Image
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFrame
import kotlin.math.cos
import kotlin.math.sin

fun main() {
    Test().main()
}

class Test {
    fun main() {
        val display = NDisplay("TEST", Rect(100, 100, 500, 500))
        display.exit.closingRegistry.register {
            println("drawCount:${display.drawManager.drawCount}")
            println("TotalFreeMil:${display.drawManager.totalFreeTime}")
            println("AvgFreeMil:${display.drawManager.totalFreeTime / display.drawManager.drawCount}")
            println("AvgFreePercent:${(display.drawManager.totalFreeTime / display.drawManager.drawCount) * 100 / display.drawManager.milSecPerSec}%")
            println("AvgFPS:${display.drawManager.drawCount / (display.drawManager.totalTime / 1000)}FPS")
            println("WorseFPS:${display.drawManager.worseFPS}FPS")
        }

        display.exit.closedRegistry.register { println("Closed") }
        display.mouse.wheel.register { println("Wheel ${it.preciseWheelRotation}") }
        display.drawManager.setFPS(60.0)
        val img = ImageIO.read(File("NGraphices-main\\src\\main\\resources\\192.png"))
//        display.sceneManager.getScene()!!.getLayer(0)!!
//            .addDrawable(RollingPicture(Pos(1920 / 2, 1080 / 2), Rect(500, 500, 700, 700), img))
        addRandomRolling(display,img,0,1980,0,1080,1000)
//        addRandomImg(display, img, 0, 1920, 0, 1080, 100)
//        display.sceneManager.getScene()!!.newLayer("2nd", 1)
//        display.sceneManager.getScene()!!.getLayer(1)!!.addDrawable(ForDebug(Rect(0, 0, 500, 500)))
        display.extendedState = JFrame.MAXIMIZED_BOTH

        while (true) {
            display.draw()
        }
    }
}

fun addRandomImg(display: NDisplay, img: Image, xMin: Int, xMax: Int, yMin: Int, yMax: Int, times: Int = 1) {
    for (i in 0 until times) {
        display.sceneManager.getScene()!!.getLayer(0)!!.addDrawable(Picture(Rect.random(xMin, xMax, yMin, yMax), img))
    }
}

fun addRandomRolling(display: NDisplay, img: Image, xMin: Int, xMax: Int, yMin: Int, yMax: Int, times: Int = 1){
    for (i in 0 until times) {
        display.sceneManager.getScene()!!.getLayer(0)!!.addDrawable(RollingPicture(Pos.random(xMin, xMax, yMin, yMax),Rect.random(xMin, xMax, yMin, yMax), img))
    }
}

class ForDebug(r: Rect) : Box(r, true) {
    var b = false
    override fun draw(g: Graphics, deltaTime: Long) {
        if (b) {
            super.draw(g, deltaTime)
        }
        b = !b
    }
}

class RollingPicture(val center: Pos, r: Rect, img: Image) : Picture(r, img) {
    companion object {
        const val rate = 1.0
    }

    val base = r.clone()
    var rotate = 0.0

    override fun draw(g: Graphics, deltaTime: Long) {
        roll(deltaTime)
        super.draw(g, deltaTime)
    }

    fun roll(deltaTime: Long) {
        rotate += ((deltaTime.toDouble() / 1000.0) / 1000.0) / 100.0
        rotate %= 360.0 * (1 / rate)
        val rr = base.clone()
        rr.set(
            (cos(rotate * rate) * center.distance(base.center())).toInt(),
            (sin(rotate * rate) * center.distance(base.center())).toInt()
        )
        r = rr
    }
}