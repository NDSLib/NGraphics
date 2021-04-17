package com.ndsl.ngraphics

import com.ndsl.ngraphics.display.NDisplay
import com.ndsl.ngraphics.drawable.Box
import com.ndsl.ngraphics.drawable.Picture
import com.ndsl.ngraphics.pos.Rect
import java.awt.Graphics
import java.awt.Image
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFrame

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
        addRandomImg(display,img,0,1920,0,1080,1000)
//        display.sceneManager.getScene()!!.getLayer(0)!!.addDrawable(ForDebug(Rect(100,100,150,150)))
        display.extendedState = JFrame.MAXIMIZED_BOTH

        while (true) {
            display.draw()
        }
    }
}

fun addRandomImg(display:NDisplay,img:Image,xMin:Int,xMax:Int,yMin:Int,yMax:Int,times:Int = 1){
    for (i in 0..times){
        display.sceneManager.getScene()!!.getLayer(0)!!.addDrawable(Picture(Rect.random(xMin, xMax, yMin, yMax),img))
    }
}

class ForDebug(r: Rect): Box(r) {
    var b = false
    override fun draw(g: Graphics) {
        if(b){
            super.draw(g)
        }
        b != b
    }
}