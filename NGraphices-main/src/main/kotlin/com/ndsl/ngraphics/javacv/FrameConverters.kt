package com.ndsl.ngraphics.javacv

import org.bytedeco.javacv.Frame
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.*
import java.lang.NullPointerException
import java.nio.ByteBuffer

private var framePixels: ByteArray? = null

fun createBufferedImage(frame: Frame): BufferedImage? {
    try{
        val buffer: ByteBuffer = frame.image[0].position(0) as ByteBuffer
        if (framePixels == null) framePixels = ByteArray(buffer.limit())
        buffer.get(framePixels)
        val cs: ColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB)
        val cm: ColorModel = ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE)
        val wr: WritableRaster = Raster.createWritableRaster(ComponentSampleModel(DataBuffer.TYPE_BYTE,
            frame.imageWidth,
            frame.imageHeight,
            frame.imageChannels,
            frame.imageStride,
            intArrayOf(2, 1, 0)), null)
        val bufferPixels: ByteArray = (wr.dataBuffer as DataBufferByte).data
        System.arraycopy(framePixels!!, 0, bufferPixels, 0, framePixels!!.size)
        return BufferedImage(cm, wr, false, null)
    }catch (e:NullPointerException){
        return null
    }
}