package matt.ffmpeg.filtergraph.select

import matt.ffmpeg.eval.Eq
import matt.ffmpeg.eval.FfmpegConstant
import matt.ffmpeg.eval.GlobalConstants
import matt.ffmpeg.eval.If
import matt.ffmpeg.eval.RawFfmpegExpression
import matt.ffmpeg.filtergraph.FilterChainDsl
import matt.ffmpeg.filtergraph.FiltergraphFilter
import matt.ffmpeg.filtergraph.FiltergraphValue
import matt.ffmpeg.filtergraph.withFile
import matt.ffmpeg.filtergraph.withInputsAndOutputs
import matt.ffmpeg.filtergraph.withValue
import matt.ffmpeg.filtergraph.withVariable
import matt.lang.anno.SeeURL
import matt.model.data.file.FilePath

object FfmpegSelect : FiltergraphFilter<StaticSelect> {
    override val name = "select"
}

fun FilterChainDsl.select(
    frames: List<SelectedFrame>,
    inputs: List<String>,
    outputs: List<String>
) {
    filters += FfmpegSelect.withValue(StaticSelect(
        frames,
        numOutputs = outputs.takeIf { it.isNotEmpty() }?.size ?: 1
    )).withInputsAndOutputs(inputs = inputs, outputs = outputs)
}

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html#select_002c-aselect")
class StaticSelect(frames: List<SelectedFrame>, numOutputs: Int) : FiltergraphValue {
    private val numOutputsPart = if (numOutputs == 1) "" else ":$numOutputs"
    private val rawValue = frames.joinToString(separator = "+") { it.expression.expression } + numOutputsPart
    override fun expression() = /*"print(n+25)\\;" +*/ rawValue
}


fun FilterChainDsl.selectFromFile(
    file: FilePath
) {
    filters += FfmpegSelect.withFile(file)
}

fun FilterChainDsl.selectFromVariable(
    variable: String
) {
    filters += FfmpegSelect.withVariable(variable)
}


class SelectConstants : GlobalConstants() {
    val n = FfmpegConstant("n")
}

data class SelectedFrame(
    val frame: Int,
    private val outputToNumberedStream: Boolean = false
) {
    private val returnValue = if (outputToNumberedStream) "n+1" else "1"
    val expression =
        SelectConstants().run { If(Eq(n, RawFfmpegExpression(frame.toString())), RawFfmpegExpression(returnValue)) }
}


