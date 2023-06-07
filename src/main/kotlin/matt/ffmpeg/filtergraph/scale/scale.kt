package matt.ffmpeg.filtergraph.scale

import matt.ffmpeg.filtergraph.FilterChainDsl
import matt.ffmpeg.filtergraph.FiltergraphFilter
import matt.ffmpeg.filtergraph.FiltergraphValue
import matt.ffmpeg.filtergraph.crop.toIfBIsNotNull
import matt.ffmpeg.filtergraph.scale.ScaleEval.init
import matt.ffmpeg.filtergraph.suppliedByCommand
import matt.ffmpeg.filtergraph.withValue
import matt.lang.anno.SeeURL

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html#scale-1")
object Scale : FiltergraphFilter<ScaleValue> {
    override val name = "scale"
}

fun FilterChainDsl.scale(
    width: ScaleDimension?,
    height: ScaleDimension?,
    eval: ScaleEval = DEFAULT_EVAL
) {
    filters += Scale withValue ScaleValue(width = width, height = height, eval = eval)
}

fun FilterChainDsl.scaleFromCommand(
) {
    filters += Scale.suppliedByCommand()
}

enum class ScaleEval {
    `init`, frame
}

private val DEFAULT_EVAL = `init`


class ScaleValue(
    private val width: ScaleDimension?,
    private val height: ScaleDimension?,
    private val eval: ScaleEval? = null
) : FiltergraphValue {

    init {
        require(listOf(width, height).filterIsInstance<MaintainAspectRatioDivisibleBy>().size < 2)
    }

    override fun expression() = listOf(
        "w" toIfBIsNotNull width?.arg(),
        "h" toIfBIsNotNull height?.arg(),
        "eval" toIfBIsNotNull eval?.name
    ).filterNotNull().joinToString(separator = ":") {
        "${it.first}=${it.second}"
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

object FromInputWidth : ScaleDimension {
    override fun arg() = "min(print(89)\\,0)+print(iw)"
}

object FromInputHeight : ScaleDimension {
    override fun arg() = "ih"
}