package matt.ffmpeg.filtergraph.scale

import matt.ffmpeg.filtergraph.FilterChainDsl
import matt.ffmpeg.filtergraph.FiltergraphFilter
import matt.ffmpeg.filtergraph.FiltergraphValue
import matt.ffmpeg.filtergraph.withValue

object Scale : FiltergraphFilter<ScaleValue> {
    override val name = "scale"
}

fun FilterChainDsl.scale(
    width: ScaleDimension, height: ScaleDimension
) {
    filters += Scale withValue ScaleValue(width = width, height = height)
}

class ScaleValue(private val width: ScaleDimension, private val height: ScaleDimension) : FiltergraphValue {

    init {
        require(listOf(width, height).filterIsInstance<MaintainAspectRatioDivisibleBy>().size < 2)
    }

    override fun expression(): String {
        return "$width:$height"
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
