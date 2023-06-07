package matt.ffmpeg.filtergraph.setpts

import matt.ffmpeg.eval.FfmpegConstant
import matt.ffmpeg.eval.FfmpegExpression
import matt.ffmpeg.eval.GlobalConstants
import matt.ffmpeg.filtergraph.FilterChainDsl
import matt.ffmpeg.filtergraph.FiltergraphFilter
import matt.ffmpeg.filtergraph.FiltergraphValue
import matt.ffmpeg.filtergraph.withInputsAndOutputs
import matt.ffmpeg.filtergraph.withValue
import matt.lang.anno.SeeURL

object FfmpegSetPts : FiltergraphFilter<SetPts> {
    override val name = "setpts"
}

@Suppress("PropertyName")
class SetPtsConstants : GlobalConstants() {
    val N = FfmpegConstant("N")
    val TB = FfmpegConstant("TB") /*TB is duration of one tick*/
}

fun FilterChainDsl.setPts(
    inputs: List<String> = listOf(),
    outputs: List<String> = listOf(),
    command: SetPtsConstants.() -> FfmpegExpression,
) {
    filters += FfmpegSetPts.withValue(StaticSetPts(SetPtsConstants().command()))
        .withInputsAndOutputs(inputs = inputs, outputs = outputs)
}

interface SetPts : FiltergraphValue

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html#toc-setpts_002c-asetpts")
class StaticSetPts(private val expr: FfmpegExpression) : SetPts {
    override fun expression() = expr.expression
}