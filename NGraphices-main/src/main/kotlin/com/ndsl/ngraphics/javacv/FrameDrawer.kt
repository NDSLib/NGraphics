package com.ndsl.ngraphics.javacv

import com.ndsl.ngraphics.base.Timer
import com.ndsl.ngraphics.drawable.DrawableBase
import com.ndsl.ngraphics.pos.Rect
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.thread


class FrameDrawer(var frame: Frame?, var r: Rect) : DrawableBase() {
    var image: BufferedImage? = null
    private var converter: Java2DFrameConverter? = null
    var fps: Double = 60.0
        set(value) {
            field = value;timer = Timer(1000 / field)
        }
    private var timer = Timer(1000 / fps)
    override fun draw(g: Graphics, deltaTime: Long, showing: Rect) {
        if (frame == null) return
        if (!timer.isStarted()) timer.start()
        if (timer.isUp()) {
            if (converter == null) {
                converter = Java2DFrameConverter()
            }
            image = converter!!.convert(frame)
            timer = Timer(1000 / fps)
        }
        if (image != null) {
            g.drawImage(image, r.left_up.x, r.left_up.y, r.getWidth(), r.getHeight(), null)
        }
    }
}

class FFmpegGrabberDrawer(val file: File, var r: Rect? = null, val bufferSize: Int = 1000) : DrawableBase() {
    private val grabber = FFmpegVideoLoader(file)
    private lateinit var converter: Java2DFrameConverter
    var isInit = true
    private var currentImage: BufferedImage? = null
    var isEnd = false
        private set
    var fps = 60.0
        set(value) {
            field = value;timer = Timer(1000 / value)
        }
    private var timer = Timer(1000 / fps)

    override fun draw(g: Graphics, deltaTime: Long, showing: Rect) {
        if (isInit) {
            grabber.init()
            grabber.start()
            converter = Java2DFrameConverter()
            isInit = false
            thread {
                while (!isEnd) {
                    updateFrame()
                    updateImage()
                }
            }
        }
        if (!timer.isStarted()) timer.start()
        if (timer.isUp()) {
            currentImage = imageBuffer.getOrElse(0) { currentImage }
            imageBuffer[0] = null
            timer = Timer(1000 / fps)
        }
        if (currentImage != null) {
            println("FrameBuffer:${frameBuffer.filterNotNull().size} ImageBuffer:${imageBuffer.filterNotNull().size}")
            if (r != null) {
                g.drawImage(currentImage, r!!.left_up.x, r!!.left_up.y, r!!.getWidth(), r!!.getHeight(), null)
            } else {
                g.drawImage(currentImage, 0, 0, currentImage!!.getWidth(null), currentImage!!.getWidth(null), null)
            }
        } else {
            println("NULLLLLLLLLLLLLLLLLLLL")
        }
    }

    private var frameBuffer = arrayOfNulls<Frame?>(bufferSize)
    private var imageBuffer = arrayOfNulls<BufferedImage?>(bufferSize)

    private fun updateFrame() {
        frameBuffer = frameBuffer.filterNotNull().filter { it.image != null }.toTypedArray().copyOf(bufferSize)
        val start = frameBuffer.indexOfLast { it == null }
        if (start == -1) return
        for (i in start until bufferSize) {
            frameBuffer[i] = grabber.next()
        }
    }

    private fun updateImage() {
        imageBuffer = imageBuffer.filterNotNull().toTypedArray().copyOf(bufferSize)
        val start = imageBuffer.indexOfLast { it == null }
        if (start == -1) return
        for (i in start until bufferSize) {
            imageBuffer[i] = imageNext()
        }
    }

    private fun imageNext(): BufferedImage? {
        val index = frameBuffer.indexOfFirst { it != null }
        if (index == -1) return null
        val frame = frameBuffer[index]
        if (frame != null) {
            if (converter != null) {
                val image = converter.getBufferedImage(frame)
                frameBuffer[index] = null
                return image
            } else {
                return null
            }
        }
        return null
    }
}

class ObjectBuffer<T> {
    private var list = mutableListOf<T>()

    fun get(i: Int): T? = list.getOrNull(i)
    fun getAndRemove(i: Int): T? {
        val t: T? = get(i)
        return if (t == null) {
            null
        } else {
            list.minus(t)
            t
        }
    }

    fun add(t: T) {
        list.add(t)
    }

    fun size(): Int = list.size
}

class FFmpegPlayer(file: File, var r: Rect?, val mode: DrawMode = DrawMode.FitToWindow) : DrawableBase() {

    enum class DrawMode {
        Rect, FitToWindow, ResizeWithin, Normal
    }


    private val grabber = FFmpegFrameGrabber(file)
    private lateinit var converter: Java2DFrameConverter

    private var isInit = false
    var isEnd = false
        private set

    var fps = 60.0
        set(value) {
            field = value;timer = Timer(1000 / value)
        }
    private var timer = Timer(1000 / fps)

    private lateinit var sourceDataLine: SourceDataLine

    private val imageBuffer = ObjectBuffer<BufferedImage>()
    private val audioBuffer = ObjectBuffer<ShortBuffer>()

    private var currentImage: BufferedImage? = null
    private var currentAudio: ShortBuffer? = null

    override fun draw(g: Graphics, deltaTime: Long, showing: Rect) {
        println("Showing Rect:${showing.getHeight()},${showing.getWidth()}")

        if (!isInit) {
            grabber.start()
            converter = Java2DFrameConverter()
//            val audioFormat = AudioFormat(grabber.sampleRate.toFloat(), 16, grabber.audioChannels, true, true)
            val audioFormat = getAudioFormat(grabber)
            val dataLineInfo: DataLine.Info = DataLine.Info(SourceDataLine::class.java, audioFormat)
            sourceDataLine = AudioSystem.getLine(dataLineInfo) as SourceDataLine
            sourceDataLine.open(audioFormat)
            sourceDataLine.start()

            isInit = true
            isEnd = false

            thread {
                while (!isEnd) {
                    updateFrame()
                    performFrame()
                }
            }
        }

        if (!timer.isStarted()) timer.start()
        if (timer.isUp()) {
            shiftBuffer()
            timer = Timer(1000 / fps)
        }

        if (currentImage != null) {
            when (mode) {
                DrawMode.Rect -> {
                    if (r == null) {
                        throw IllegalArgumentException("Rect cannot be null!")
                    }
                    g.drawImage(currentImage, r!!.left_up.x, r!!.left_up.y, r!!.getWidth(), r!!.getHeight(), null)
                }
                DrawMode.FitToWindow -> {
                    g.drawImage(currentImage,
                        showing.left_up.x,
                        showing.left_up.y,
                        showing.getWidth(),
                        showing.getHeight(),
                        null)
                }
                DrawMode.ResizeWithin -> {
                    // TODO
                }
                DrawMode.Normal -> {
                    g.drawImage(currentImage, 0, 0, null)
                }
            }
        }

        if (currentAudio != null) playSound(currentAudio!!)
    }

    private fun shiftBuffer() {
        val i = imageBuffer.getAndRemove(0)
        if (i == null) {
//            println("Image Buffer is out!")
        } else {
            currentImage = i
        }

        val a = audioBuffer.getAndRemove(0)
        if (a == null) {
//            println("Audio Buffer is out!")
        } else {
            currentAudio = a
        }
    }

    private fun performFrame() {
        if (frameBuffer == null) {
            return
        }
        val f: Frame = frameBuffer!!


        if (f.types.contains(Frame.Type.VIDEO)) {
            currentImage = converter.getBufferedImage(f)
        } else if (f.types.contains(Frame.Type.AUDIO)) {
            if (f.samples[0] is ShortBuffer) {
                playSound(f)
            } else {
                println("Sound is not Short Buffer")
            }
        }
    }

    var executor: ExecutorService = Executors.newSingleThreadExecutor()

    private fun playSound(f: Frame) {
        processAudio(f.samples)
    }

    private fun playSound(b: ShortBuffer) {
        b.rewind()

        val outBuffer = ByteBuffer.allocate(b.capacity() * 2)

        for (i in 0 until b.capacity()) {
            val value: Short = b.get(i)
            outBuffer.putShort(value)
        }

        executor.submit {
            sourceDataLine.write(outBuffer.array(), 0, outBuffer.capacity())
            outBuffer.clear()
        }.get()
    }

    private var frameBuffer: Frame? = null

    private fun updateFrame() {
        frameBuffer = grabber.grabFrame()
    }

    private fun getAudioFormat(fg: FFmpegFrameGrabber): AudioFormat? {
        var af: AudioFormat? = null
        when (fg.sampleFormat) {
            avutil.AV_SAMPLE_FMT_U8 -> {
            }
            avutil.AV_SAMPLE_FMT_S16 -> af =
                AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fg.sampleRate.toFloat(), 16, fg.audioChannels,
                    fg.audioChannels * 2, fg.sampleRate.toFloat(), true)
            avutil.AV_SAMPLE_FMT_S32 -> {
            }
            avutil.AV_SAMPLE_FMT_FLT -> af =
                AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fg.sampleRate.toFloat(), 16, fg.audioChannels,
                    fg.audioChannels * 2, fg.sampleRate.toFloat(), true)
            avutil.AV_SAMPLE_FMT_DBL -> {
            }
            avutil.AV_SAMPLE_FMT_U8P -> {
            }
            avutil.AV_SAMPLE_FMT_S16P -> af =
                AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fg.sampleRate.toFloat(), 16, fg.audioChannels,
                    fg.audioChannels * 2, fg.sampleRate.toFloat(), true)
            avutil.AV_SAMPLE_FMT_S32P ->                                         // 32 bit
                af = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fg.sampleRate.toFloat(), 32, fg.audioChannels,
                    fg.audioChannels * 2, fg.sampleRate.toFloat(), true)
            avutil.AV_SAMPLE_FMT_FLTP -> af =
                AudioFormat(AudioFormat.Encoding.PCM_SIGNED, fg.sampleRate.toFloat(), 16, fg.audioChannels,
                    fg.audioChannels * 2, fg.sampleRate.toFloat(), true)
            avutil.AV_SAMPLE_FMT_DBLP -> {
            }
            avutil.AV_SAMPLE_FMT_S64 -> {
            }
            avutil.AV_SAMPLE_FMT_S64P -> {
            }
            else -> {
                println("unsupported")
//                exitProcess(0)
            }
        }
        return af
    }

    private fun processAudio(sample: Array<Buffer>) {
        processAudio(sample, grabber.sampleFormat, sourceDataLine)
    }

    //play audio sample
    private fun processAudio(samples: Array<Buffer>, sampleFormat: Int, sourceDataLine: SourceDataLine) {
        var k: Int
        val buf: Array<Buffer> = samples
        val leftData: FloatBuffer
        val rightData: FloatBuffer
        val ILData: ShortBuffer
        val IRData: ShortBuffer
        val TLData: ByteBuffer
        val TRData: ByteBuffer
        val vol = 1f // volume
        val tl: ByteArray
        val tr: ByteArray
        val combine: ByteArray
        when (sampleFormat) {
            avutil.AV_SAMPLE_FMT_FLTP -> {
                leftData = buf[0] as FloatBuffer
                TLData = floatToByteValue(leftData, vol)
                rightData = buf[1] as FloatBuffer
                TRData = floatToByteValue(rightData, vol)
                tl = TLData.array()
                tr = TRData.array()
                combine = ByteArray(tl.size + tr.size)
                k = 0
                var i = 0
                while (i < tl.size) {
                    //Mix two channels
                    var j = 0
                    while (j < 2) {
                        combine[j + 4 * k] = tl[i + j]
                        combine[j + 2 + 4 * k] = tr[i + j]
                        j++
                    }
                    k++
                    i += 2
                }
                sourceDataLine.write(combine, 0, combine.size)
            }
            avutil.AV_SAMPLE_FMT_S16 -> {
                ILData = buf[0] as ShortBuffer
                TLData = shortToByteValue(ILData, vol)
                tl = TLData.array()
                sourceDataLine.write(tl, 0, tl.size)
            }
            avutil.AV_SAMPLE_FMT_FLT -> {
                leftData = buf[0] as FloatBuffer
                TLData = floatToByteValue(leftData, vol)
                tl = TLData.array()
                sourceDataLine.write(tl, 0, tl.size)
            }
            avutil.AV_SAMPLE_FMT_S16P -> {
                ILData = buf[0] as ShortBuffer
                IRData = buf[1] as ShortBuffer
                TLData = shortToByteValue(ILData, vol)
                TRData = shortToByteValue(IRData, vol)
                tl = TLData.array()
                tr = TRData.array()
                combine = ByteArray(tl.size + tr.size)
                k = 0
                var i = 0
                while (i < tl.size) {
                    var j = 0
                    while (j < 2) {
                        combine[j + 4 * k] = tl[i + j]
                        combine[j + 2 + 4 * k] = tr[i + j]
                        j++
                    }
                    k++
                    i += 2
                }
                sourceDataLine.write(combine, 0, combine.size)
            }
            else -> {
                println("unsupport audio format")
//                System.exit(0)
            }
        }
    }

    private fun shortToByteValue(arr: ShortBuffer, vol: Float): ByteBuffer {
        val len = arr.capacity()
        val bb = ByteBuffer.allocate(len * 2)
        for (i in 0 until len) {
            bb.putShort(i * 2, (arr[i].toFloat() * vol).toInt().toShort())
        }
        return bb
    }

    private fun floatToByteValue(arr: FloatBuffer, vol: Float): ByteBuffer {
        val len = arr.capacity()
        var f: Float
        val v: Float
        val res = ByteBuffer.allocate(len * 2)
        v = 32768.0f * vol
        for (i in 0 until len) {
            f =
                arr[i] * v
            if (f > v) f = v
            if (f < -v) f = v
            res.putShort(i * 2, f.toInt().toShort())
        }
        return res
    }
}