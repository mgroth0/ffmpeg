package matt.ffmpeg.filtergraph.select

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
    filters += FfmpegSelect.withValue(StaticSelect(frames)).withInputsAndOutputs(inputs = inputs, outputs = outputs)
}

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html#select_002c-aselect")
class StaticSelect(frames: List<SelectedFrame>) : FiltergraphValue {
    val rawValue = frames.joinToString(separator = "+") { it.expression } + ":${frames.size}"
    override fun expression() = rawValue
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


data class SelectedFrame(
    val frame: Int,
) {
    val expression = "if(eq(n\\,$frame)\\,n+1\\,0)"
}

