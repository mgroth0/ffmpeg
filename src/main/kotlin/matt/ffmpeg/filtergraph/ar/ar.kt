package matt.ffmpeg.filtergraph.ar

import matt.ffmpeg.filtergraph.FilterChainDsl
import matt.ffmpeg.filtergraph.FiltergraphFilter
import matt.ffmpeg.filtergraph.FiltergraphValue
import matt.ffmpeg.filtergraph.withValue
import matt.lang.anno.SeeURL


@SeeURL("https://stackoverflow.com/questions/50346707/ffmpeg-scaling-not-working-for-video")
object PixelAspectRatio : FiltergraphFilter<AspectRatioValue> {
    override val name = "setsar"
}


fun FilterChainDsl.aspectRatio(
    width: Int, height: Int
) {
    filters += PixelAspectRatio withValue PixelAspectRatioValue(widthRatio = width, heightRatio = height)
}

fun FilterChainDsl.sampleAspectRatio(
    sar: Int
) {
    filters += PixelAspectRatio withValue Sar(sar)
}

interface AspectRatioValue : FiltergraphValue

@SeeURL("http://trac.ffmpeg.org/wiki/Scaling")
class PixelAspectRatioValue(
    private val widthRatio: Int,
    private val heightRatio: Int
) : AspectRatioValue {
    override fun expression(): String {
        return "$widthRatio/$heightRatio"
    }
}

@SeeURL("http://trac.ffmpeg.org/wiki/Scaling")
class Sar(
    private val sar: Int,
) : AspectRatioValue {
    override fun expression(): String {
        return "sar=$sar"
    }
}

