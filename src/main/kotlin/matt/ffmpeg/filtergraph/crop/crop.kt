package matt.ffmpeg.filtergraph.crop

import matt.ffmpeg.filtergraph.FilterChainDsl
import matt.ffmpeg.filtergraph.FiltergraphFilter
import matt.ffmpeg.filtergraph.FiltergraphValue
import matt.ffmpeg.filtergraph.withInputsAndOutputs
import matt.ffmpeg.filtergraph.withValue


fun FilterChainDsl.crop(
    crop: CropBox,
    inputs: List<String>,
    outputs: List<String>
) {
    filters += Crop.withValue(StaticCrop(crop)).withInputsAndOutputs(inputs = inputs, outputs = outputs)
}


object Crop : FiltergraphFilter<StaticCrop> {
    override val name = "crop"
}

class StaticCrop(private val box: CropBox) : FiltergraphValue {
    override fun expression() = listOf(
        "w" to box.width,
        "h" to box.height,
        "x" to box.x,
        "y" to box.y
    ).joinToString(separator = ":") {
        "${it.first}=${it.second}"
    }
}


data class CropBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)