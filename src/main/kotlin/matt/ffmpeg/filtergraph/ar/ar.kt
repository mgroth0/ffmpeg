package matt.ffmpeg.filtergraph.ar

import matt.ffmpeg.filtergraph.FilterChainDsl
import matt.ffmpeg.filtergraph.FiltergraphFilter
import matt.ffmpeg.filtergraph.FiltergraphValue
import matt.ffmpeg.filtergraph.withValue
import matt.lang.anno.SeeURL



object PixelAspectRatio : FiltergraphFilter<PixelAspectRatioValue> {
    override val name = "setsar"
}


fun FilterChainDsl.aspectRatio(
    width: Int, height: Int
) {
    filters += PixelAspectRatio withValue PixelAspectRatioValue(widthRatio = width, heightRatio = height)
}

@SeeURL("http://trac.ffmpeg.org/wiki/Scaling")
class PixelAspectRatioValue(private val widthRatio: Int, private val heightRatio: Int) : FiltergraphValue {
    override fun expression(): String {
        return "$widthRatio/$heightRatio"
    }
}

