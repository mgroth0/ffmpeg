package matt.ffmpeg

import matt.lang.anno.SeeURL
import matt.lang.optArray
import matt.model.data.file.FilePath
import matt.prim.str.joinWithCommas
import matt.shell.Shell

enum class FFmpegPixelFormat {
    rgb8
}

@SeeURL("https://ffmpeg.org/ffmpeg.html")
fun <R> Shell<R>.ffmpeg(
    input: FilePath,
    encoderPixelFormat: FFmpegPixelFormat? = null,
    output: FilePath,
    outputFps: Int? = null,
    overwrite: Boolean,
    filtergraph: Filtergraph? = null,
    threads: Int? = null
): R = sendCommand(
    this::ffmpeg.name,
    if (overwrite) "-y" else "-n",
    "-i",
    input.filePath,
    *optArray(encoderPixelFormat) { arrayOf("-pxl_fmt", "+${name}") },
    *optArray(outputFps) { arrayOf("-r", toString()) },
    *optArray(filtergraph) { arrayOf("-vf", arg()) },
    *optArray(threads) { arrayOf("-threads", threads.toString()) },
    output.filePath
)


@SeeURL("https://ffmpeg.org/ffmpeg-filters.html")
class Filtergraph(vararg val filters: FiltergraphFilter) {
    fun arg(): String = filters.joinWithCommas()
}

interface FiltergraphFilter {
    fun arg(): String
}

class Scale(private val width: ScaleDimension, private val height: ScaleDimension) : FiltergraphFilter {
    init {
        require(listOf(width, height).filterIsInstance<MaintainAspectRatioDivisibleBy>().size < 2)
    }

    override fun arg(): String {
        return "scale=$width:$height"
    }
}

interface ScaleDimension {
    fun arg(): String
}

class Absolute(val pixels: Int) : ScaleDimension {
    override fun arg(): String {
        return pixels.toString()
    }
}

class MaintainAspectRatioDivisibleBy(val divisor: Int) : ScaleDimension {
    init {
        require(divisor > 0)
    }

    override fun arg(): String {
        return "-$divisor"
    }
}

@SeeURL("http://trac.ffmpeg.org/wiki/Scaling")
class PixelAspectRatio(private val widthRatio: Int, private val heightRatio: Int) : FiltergraphFilter {
    override fun arg(): String {
        return "setsar=$widthRatio/$heightRatio"
    }
}