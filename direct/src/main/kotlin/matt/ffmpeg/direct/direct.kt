package matt.ffmpeg.direct

import matt.file.construct.mFile
import matt.lang.anno.SeeURL
import matt.lang.disabledCode
import matt.model.data.file.FilePath
import matt.service.frames.MFrameGrabber
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.ffmpeg.global.avutil.AV_LOG_ERROR
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.Java2DFrameConverter
import javax.imageio.ImageIO
import kotlin.math.roundToLong

object FFmpegDirectFrameGrabber : MFrameGrabber {
    init {
        @SeeURL("https://github.com/bytedeco/javacv/issues/780")
        (avutil.av_log_set_level(AV_LOG_ERROR))
    }

    override fun extractVideoFrames(video: FilePath, outputFolder: FilePath, framesToExtract: List<Int>) {


        val realOutputFolder = mFile(outputFolder.filePath)

        realOutputFolder.mkdirs()

        val converter = Java2DFrameConverter()

        val frameGrabber = FFmpegFrameGrabber(mFile(video.filePath))

        @SeeURL("https://github.com/bytedeco/javacv/issues/1163")
        @SeeURL("https://ffmpeg.org/ffmpeg-protocols.html")
        @SeeURL("https://github.com/bytedeco/javacv/issues/1154")
        @SeeURL("https://github.com/bytedeco/javacpp-presets/issues/708")
        frameGrabber.setOption("rtsp_transport", "udp")
        frameGrabber.setOption("buffer_size", "40960000")
        frameGrabber.setOption("hwaccel", "cuvid")
        frameGrabber.videoCodecName = "h264_cuvid"
        frameGrabber.setOption("threads", "8")

        frameGrabber.start()




        println("extracting ${framesToExtract.size} frames")

        val frameRate = frameGrabber.frameRate

        framesToExtract.mapIndexed { iterIdx, frameIndex ->
            val i = frameIndex
            if (iterIdx % 100 == 0) {
                println("hasVideo = ${frameGrabber.hasVideo()}")
            }
            disabledCode {
                frameGrabber.frameNumber = i
            }
            frameGrabber.timestamp = ((1000000L * i + 500000L) / frameRate).roundToLong()
            val frame = frameGrabber.grabImage()
            val bi = converter.convert(frame)
            ImageIO.write(bi, "png", realOutputFolder["$i.png"])
        }
        println("stopping frameGrabber")
        frameGrabber.stop()
        println("releasing frameGrabber")
        frameGrabber.release()
        println("released frameGrabber")
    }
}