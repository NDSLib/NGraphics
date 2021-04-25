package com.ndsl.ngraphics

import com.ndsl.ngraphics.base.DrawMode
import com.ndsl.ngraphics.display.NDisplay
import com.ndsl.ngraphics.drawable.Box
import com.ndsl.ngraphics.drawable.Picture
import com.ndsl.ngraphics.javacv.*
import com.ndsl.ngraphics.pos.Pos
import com.ndsl.ngraphics.pos.Rect
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFrame
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.exitProcess

fun main() {
//    Test().main()
//    JavaCVTest().main()
    JavaCVTest().playerTest()
}

class JavaCVTest {
    companion object {
        val file = File("NGraphices-main\\src\\main\\resources\\test.mp4")
    }


    lateinit var grabber: EasyFrameGrabber
    fun main() {
        grabber = EasyFrameGrabber(file)
        grabber.load()
        grabber.getFFmpeg().start()
        val display = NDisplay("JavaCVTest", Rect(100, 100, 500, 500))
        display.drawManager.setFPS(60.0)
        val drawer = FFmpegGrabberDrawer(file)
        display.sceneManager.getScene()!!.getLayer(0)!!.addDrawable(drawer)
        do {
            display.draw()
        } while (!drawer.isEnd)
        println("END!")
        exitProcess(0)
    }

    fun fps() {
        val startTime = System.currentTimeMillis()
        var frames = 0
        val grab = FFmpegFrameGrabber(file)
        grab.start()
        while (grab.grab() != null) {
            frames++
            println("$frames")
        }
        val endTime = System.currentTimeMillis()
        println("start:$startTime end:$endTime frames:$frames")
        println("fps:${frames.toDouble() / ((endTime - startTime).toDouble() / 1000.0)}")
    }

    fun fpsWithConvert() {
        val startTime = System.currentTimeMillis()
        var frames = 0
        val grab = FFmpegFrameGrabber(file)
        grab.start()
        var frame: Frame? = null
        var image: BufferedImage? = null
        do {
            frames++
            println("$frames")
            frame = grab.grab()
            if (frame != null) image = createBufferedImage(frame)
        } while (frame != null)
        val endTime = System.currentTimeMillis()
        println("start:$startTime end:$endTime frames:$frames")
        println("fps:${frames.toDouble() / ((endTime - startTime).toDouble() / 1000.0)}")
    }

    fun fpsWithDraw() {
        val display = NDisplay("FpsWithDraw", Rect(100, 100, 600, 600))
//        display.setFullScreen()
        val startTime = System.currentTimeMillis()
        var frames = 0
        val grab = FFmpegFrameGrabber(file)
        grab.start()
//        val drawable = Picture(Rect(0, 0, grab.imageWidth, grab.imageHeight), null,Picture.DrawMode.WithinWindow)
        val drawable = FrameDrawer(null,Rect(0, 0, grab.imageWidth, grab.imageHeight))
        display.sceneManager.getScene()!!.getLayer(0)!!.addDrawable(drawable)
        var frame: Frame?
        do {
            frames++
            println("$frames")
            frame = grab.grabImage()
            if (frame != null) {
                drawable.frame = frame
            }
            display.draw()
        } while (frame != null)
        val endTime = System.currentTimeMillis()
        println("start:$startTime end:$endTime frames:$frames")
        println("fps:${frames.toDouble() / ((endTime - startTime).toDouble() / 1000.0)}")
    }

    fun fpsWithFFmpegGrabberDrawer(){
        val d = NDisplay("FFmpegGrabberDrawer",Rect(100,100,600,600))
        val f = FFmpegGrabberDrawer(file)
        d.sceneManager.getScene()!!.getLayer(0)!!.addDrawable(f)
        while (true){
            d.draw()
        }
    }

    fun playerTest(){
        val d = NDisplay("FFmpegPlayer", Rect(100,100,600,600))
        val dd = FFmpegPlayer(file, null,FFmpegPlayer.DrawMode.FitToWindow)
        d.sceneManager.getScene()!!.getLayer(0)!!.addDrawable(dd)
        d.drawManager.drawMode = DrawMode.TargetFPS
        d.drawManager.setFPS(60.0)
        while (true){
            d.draw()
        }
    }
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
        addRandomRolling(display, img, 0, 1980, 0, 1080, 1000)
//        addRandomImg(display, img, 0, 1920, 0, 1080, 100)
//        display.sceneManager.getScene()!!.newLayer("2nd", 1)
//        display.sceneManager.getScene()!!.getLayer(1)!!.addDrawable(ForDebug(Rect(0, 0, 500, 500)))
        display.extendedState = JFrame.MAXIMIZED_BOTH
        display.drawManager.drawMode = DrawMode.TargetFPS

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

fun addRandomRolling(display: NDisplay, img: Image, xMin: Int, xMax: Int, yMin: Int, yMax: Int, times: Int = 1) {
    for (i in 0 until times) {
        display.sceneManager.getScene()!!.getLayer(0)!!
            .addDrawable(RollingPicture(Pos.random(xMin, xMax, yMin, yMax), Rect.random(xMin, xMax, yMin, yMax), img))
    }
}

class ForDebug(r: Rect) : Box(r, true) {
    var b = false
    override fun draw(g: Graphics, deltaTime: Long, showing: Rect) {
        if (b) {
            super.draw(g, deltaTime, showing)
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

    override fun draw(g: Graphics, deltaTime: Long, showing: Rect,d:NDisplay) {
        roll(deltaTime)
        super.draw(g, deltaTime, showing,d)
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