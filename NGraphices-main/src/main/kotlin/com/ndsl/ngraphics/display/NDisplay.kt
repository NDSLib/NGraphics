package com.ndsl.ngraphics.display

import com.ndsl.ngraphics.base.DrawManager
import com.ndsl.ngraphics.pos.Pos
import com.ndsl.ngraphics.pos.Rect
import com.ndsl.ngraphics.scene.Scene
import com.ndsl.ngraphics.scene.SceneManager
import com.ndsl.ngraphics.util.Register
import com.ndsl.ngraphics.util.TypedRegister
import java.awt.Component
import java.awt.Graphics
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.*
import java.awt.image.BufferStrategy
import java.awt.image.VolatileImage
import javax.swing.JFrame
import javax.swing.WindowConstants

class NDisplay(title: String, bound: Rect/*, bufferSize: Int = 3*/, onClose: Int = WindowConstants.EXIT_ON_CLOSE) : JFrame(title) {
//    var buffer: BufferStrategy
    val windowListener = MainWindowListener(this)
    val exit = ExitManager(this)
    val mouse = MouseInputHandler(this)
    val sceneManager = SceneManager(this)
    val drawManager = DrawManager(this)
    val component = this as Component
    var volatileImage : VolatileImage? = null
    var mWidth = 0
    var mHeight = 0

    init {
        this.title = title
        setBounds(bound.left_up.x, bound.left_up.y, bound.getWidth(), bound.getHeight())
        isVisible = true
//        createBufferStrategy(bufferSize)
//        buffer = bufferStrategy
        defaultCloseOperation = onClose

        addWindowListener(windowListener)
        addMouseMotionListener(mouse)
        addMouseListener(mouse)
        addMouseWheelListener(mouse)

        val scene = Scene("Default")
        scene.newLayer("Default")
        sceneManager.addScene(scene)

        initVolatileImage()
    }

    //VRAMバッファ用イメージの初期化
    private fun initVolatileImage() {

        //VRAMバッファ用イメージが未作成、もしくは現在のサイズと
        //異なっている場合は生成処理を実行する
        if (volatileImage == null || mWidth != component.width || mHeight != component.height) {
            mWidth = component.width
            mHeight = component.height
            volatileImage = component.createVolatileImage(mWidth, mHeight)
        }
    }

    //VRAMバッファ用イメージのチェック
    fun validateVolatileImage() {

        initVolatileImage()

        //グラフィックス設定情報を取得する
        val gc = component.graphicsConfiguration

        //VRAMバッファ用領域に変更があった場合は再度生成処理を実行する
        if (volatileImage?.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
            volatileImage = component.createVolatileImage(mWidth, mHeight)
        }
    }

    //グラフィックスオブジェクトの取得
    override fun getGraphics(): Graphics? {
        return volatileImage?.graphics
    }

    //描画したコンテンツが失われているかのチェック
    fun isContentsLost(): Boolean {
        if(volatileImage == null) return true
        return volatileImage!!.contentsLost()
    }

    //VRAMバッファイメージを返す
    fun getImage(): Image? {
        return volatileImage
    }

    /**
     * Call This As Often As You Can
     */
    fun draw(){
        drawManager.draw()
    }

    override fun update(g: Graphics?) {
        if(g == null){
            println("Skipped Frame!")
        }else{
            drawManager.drawAll(g)
        }
    }

    override fun repaint() {
        Toolkit.getDefaultToolkit().sync()
        val img = getImage()
        if(img != null){
            super.getGraphics().drawImage(img,0,0,null)
        }
    }

    fun contain(p:Pos) = p.x in 0..width && p.y in 0..height
}

class MainWindowListener(val display: NDisplay) : WindowListener {
    enum class WindowEventType {
        Opened, Closing, Closed, Iconified, Deiconified, Activated, Deactivated
    }

    val register = TypedRegister<WindowEvent, WindowEventType>()

    override fun windowOpened(e: WindowEvent) {
        register.invoke(WindowEventType.Opened, e)
    }

    override fun windowClosing(e: WindowEvent) {
        register.invoke(WindowEventType.Closing, e)
    }

    override fun windowClosed(e: WindowEvent) {
        register.invoke(WindowEventType.Closed, e)
    }

    override fun windowIconified(e: WindowEvent) {
        register.invoke(WindowEventType.Iconified, e)
    }

    override fun windowDeiconified(e: WindowEvent) {
        register.invoke(WindowEventType.Deiconified, e)
    }

    override fun windowActivated(e: WindowEvent) {
        register.invoke(WindowEventType.Activated, e)
    }

    override fun windowDeactivated(e: WindowEvent) {
        register.invoke(WindowEventType.Deactivated, e)
    }
}

class ExitManager(private val display: NDisplay) {
    val closingRegistry: Register<WindowEvent> = Register()
    val closedRegistry: Register<WindowEvent> = Register()

    init {
        display.windowListener.register.register(MainWindowListener.WindowEventType.Closing) {
            closingRegistry.invoke(it)
        }

        display.windowListener.register.register(MainWindowListener.WindowEventType.Closed) {
            closedRegistry.invoke(it)
        }
    }
}

class MouseInputHandler(val display: NDisplay) : MouseMotionListener, MouseListener,MouseWheelListener {
    val register: TypedRegister<MouseEvent, MouseEventType> = TypedRegister()
    var nowPos = Pos(0, 0)
    var oldPos = Pos(0, 0)
    var isIn = false
    var isClicking = false

    init {
        register.register({ isIn = true }, MouseEventType.Enter)
        register.register({ isIn = false }, MouseEventType.Exit)
        register.register({ isClicking = true }, MouseEventType.Press)
        register.register({ isClicking = false }, MouseEventType.Release)
        register.register(
            { oldPos = nowPos;nowPos = Pos(it.x, it.y) },
            MouseEventType.Drag,
            MouseEventType.Move,
            MouseEventType.Enter
        )
    }

    enum class MouseEventType {
        Drag, Move, Click, Press, Release, Enter, Exit
    }

    override fun mouseDragged(e: MouseEvent) {
        register.invoke(MouseEventType.Drag, e)
    }

    override fun mouseMoved(e: MouseEvent) {
        register.invoke(MouseEventType.Move, e)
    }

    override fun mouseClicked(e: MouseEvent) {
        register.invoke(MouseEventType.Click, e)
    }

    override fun mousePressed(e: MouseEvent) {
        register.invoke(MouseEventType.Press, e)
    }

    override fun mouseReleased(e: MouseEvent) {
        register.invoke(MouseEventType.Release, e)
    }

    override fun mouseEntered(e: MouseEvent) {
        register.invoke(MouseEventType.Enter, e)
    }

    override fun mouseExited(e: MouseEvent) {
        register.invoke(MouseEventType.Exit, e)
    }

    val wheel : Register<MouseWheelEvent> = Register()

    override fun mouseWheelMoved(e: MouseWheelEvent) {
        wheel.invoke(e)
    }
}