package com.ndsl.ngraphics.javacv

import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameGrabber
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import java.io.File

class EasyFrameGrabber(val file: File, private val grabberType: Loader = Loader.FFmpeg) {
    enum class Loader(clazz: Class<*>) {
        OpenCV(OpenCVFrameGrabber::class.java), FFmpeg(FFmpegFrameGrabber::class.java), VideoCapture(org.opencv.videoio.VideoCapture::class.java)
    }

    fun load() {
        when (grabberType) {
            Loader.OpenCV -> {
                opencv = OpenCVVideoLoader(file)
                opencv!!.init()
            }

            Loader.FFmpeg -> {
                ffmpeg = FFmpegVideoLoader(file)
                ffmpeg!!.init()
            }

            Loader.VideoCapture -> {
                videocapture = VideoCaptureLoader(file)
                videocapture!!.init()
            }
        }
    }

    private var opencv: OpenCVVideoLoader? = null
    private var ffmpeg: FFmpegVideoLoader? = null
    private var videocapture: VideoCaptureLoader? = null

    fun getOpenCV() = opencv!!
    fun getFFmpeg() = ffmpeg!!
    fun getVideoCapture() = videocapture!!

    fun getVideoLoader(): VideoLoader<*, *>? {
        if (opencv != null) return opencv as OpenCVVideoLoader
        if (ffmpeg != null) return ffmpeg as FFmpegVideoLoader
        if (videocapture != null) return videocapture as VideoCaptureLoader
        return null
    }
}

/**
 * @param L Loader Type
 * @param V VideoType
 */
abstract class VideoLoader<L, V>(val file: File) {
    internal abstract var loader: L?
    private var frameIndex = 0
    abstract fun init()
    var isGoing = false
        private set

    fun start() {
        if (isGoing) {
            println("[VideoLoader] Already Started!")
            return
        }
        isGoing = true
        internalStart()
    }

    internal abstract fun internalStart()
    fun stop() {
        if (!isGoing) {
            println("[VideoLoader] Already Stopped!")
            return
        }
        isGoing = false
        internalStop()
    }

    internal abstract fun internalStop()
    abstract fun getLoader(): L
    fun next(): V {
        frameIndex++
        return internalNext()
    }

    internal abstract fun internalNext(): V
    fun set(index: Int) {
        try {
            internalSet(index)
            frameIndex = index
        } catch (e: Exception) {
            println(e.message)
        }
    }

    internal abstract fun internalSet(index: Int)
    fun loaderCheck(): Boolean {
        if (loader == null) {
            println("Loader is Null!(Maybe not started?)")
            return false
        }
        return true
    }

    fun loaderCheck(f: (L) -> Unit) {
        if (loaderCheck()) {
            f(loader!!)
        }
    }
}

class OpenCVVideoLoader(file: File) : VideoLoader<OpenCVFrameGrabber, Frame>(file) {
    override var loader: OpenCVFrameGrabber? = null
    override fun init() {
        loader = OpenCVFrameGrabber(file)
    }

    override fun internalStart() {
        loaderCheck {
            it.start()
        }
    }

    override fun internalStop() {
        loaderCheck {
            it.stop()
        }
    }

    override fun getLoader(): OpenCVFrameGrabber {
        if (loaderCheck()) {
            return loader!!
        }
        throw Exception("OpenCVVideoLoader haven't been inited!")
    }

    override fun internalNext(): Frame {
        if (loaderCheck()) {
            return loader!!.grabFrame()
        }
        throw Exception("OpenCVVideoLoader haven't been inited!")
    }

    override fun internalSet(index: Int) {
        loader?.frameNumber = index
    }
}

class FFmpegVideoLoader(file: File) : VideoLoader<FFmpegFrameGrabber, Frame>(file) {
    override var loader: FFmpegFrameGrabber? = null
    override fun init() {
        loader = FFmpegFrameGrabber(file)
    }

    override fun internalStart() {
        loaderCheck {
            it.start()
        }
    }

    override fun internalStop() {
        loaderCheck {
            it.stop()
        }
    }

    override fun getLoader(): FFmpegFrameGrabber {
        if (loaderCheck()) {
            return loader!!
        }
        throw Exception("FFmpegVideoLoader haven't been inited!")
    }

    override fun internalNext(): Frame {
        if (loaderCheck()) {
            return loader!!.grabFrame()
        }
        throw Exception("FFmpegVideoLoader haven't been inited!")
    }

    override fun internalSet(index: Int) {
        loader?.frameNumber = index
    }
}

class VideoCaptureLoader(file: File) : VideoLoader<VideoCapture, Mat>(file) {
    override var loader: VideoCapture? = null
    var latestMat: Mat? = null
    override fun init() {
        loader = VideoCapture(file.absolutePath)
    }

    override fun internalStart() {
        loaderCheck {
            // Nothing
        }
    }

    override fun internalStop() {
        loaderCheck {
            it.release()
        }
    }

    override fun getLoader(): VideoCapture {
        if (loaderCheck()) {
            return loader!!
        }
        throw Exception("VideoCaptureLoader haven't been inited!")
    }

    override fun internalNext(): Mat {
        if (loaderCheck()) {
            loader!!.read(latestMat)
            return latestMat!!
        }
        throw Exception("VideoCaptureLoader haven't been inited!")
    }

    override fun internalSet(index: Int) {
        throw Exception("VideoCaptureLoader doesn't Support Set Operation!")
    }
}