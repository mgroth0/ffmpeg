package matt.ffmpeg.direct.weirdfix

import matt.lang.anno.SeeURL
import matt.prim.str.elementsToString
import org.bytedeco.ffmpeg.avcodec.AVCodecContext
import org.bytedeco.ffmpeg.global.swscale
import org.bytedeco.javacpp.DoublePointer
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameGrabber

fun weirdFix(frameGrabber: FrameGrabber) {
    val videoC: AVCodecContext = frameGrabber::class.java.getDeclaredField("video_c").also {
        it.isAccessible = true
    }.get(frameGrabber) as AVCodecContext
    val weirdFrame: Frame = frameGrabber::class.java.getDeclaredField("frame").also {
        it.isAccessible = true
    }.get(frameGrabber) as Frame


    /*this is copied directly from FFmpegFrameGrabber*/
    /*Convert the image into BGR or GRAY format that OpenCV uses*/
    val imgConvertCtx = swscale.sws_getCachedContext(
        null,
        videoC.width(),
        videoC.height(),
        videoC.pix_fmt(),
        weirdFrame.imageWidth,
        weirdFrame.imageHeight,
        frameGrabber.pixelFormat,
        if (frameGrabber.imageScalingFlags != 0) frameGrabber.imageScalingFlags else swscale.SWS_BILINEAR,
        null,
        null,
        null as DoublePointer?
    )

    val swsCtxField = frameGrabber::class.java.getDeclaredField("img_convert_ctx")
    swsCtxField.isAccessible = true


    swsCtxField.set(frameGrabber, imgConvertCtx)


//    val swsCtx = swsCtxField.get(frameGrabber) as SwsContext


    @SeeURL("https://stackoverflow.com/a/47772987/6596010")
    val invTable = intArrayOf()
    @Suppress("UNUSED_VARIABLE") val srcRange = intArrayOf()
    val table = intArrayOf()
    val dstRange = intArrayOf()
    val brightness = intArrayOf()
    val contrast = intArrayOf()
    val saturation = intArrayOf()
    /* var e = swscale.sws_getColorspaceDetails(
         imgConvertCtx,
         invTable,
         srcRange,
         table,
         dstRange,
         brightness,
         contrast,
         saturation
     )
     require(e >= 0) {
         "e=${e}"
     }*/
    val e = swscale.sws_setColorspaceDetails(
        imgConvertCtx,
        invTable.also {
            println("invTable=${invTable.toList().elementsToString()}")
        },
        1, /*this is the fix!?*/
        table.also {
            println("table=${table.toList().elementsToString()}")
        },
        dstRange.also {
            println("dstRange=${dstRange.toList().elementsToString()}")
        }.single(),
        brightness.also {
            println("brightness=${brightness.toList().elementsToString()}")
        }.single(),
        contrast.also {
            println("contrast=${contrast.toList().elementsToString()}")
        }.single(),
        saturation.also {
            println("saturation=${saturation.toList().elementsToString()}")
        }.single()
    )
    require(e >= 0) {
        "e=${e}"
    }


}