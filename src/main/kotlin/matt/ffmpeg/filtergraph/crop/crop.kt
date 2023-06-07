package matt.ffmpeg.filtergraph.crop

import matt.ffmpeg.eval.FfmpegConstant
import matt.ffmpeg.eval.GlobalConstants
import matt.ffmpeg.filtergraph.FilterChainDsl
import matt.ffmpeg.filtergraph.FiltergraphFilter
import matt.ffmpeg.filtergraph.FiltergraphValue
import matt.ffmpeg.filtergraph.suppliedByCommand
import matt.ffmpeg.filtergraph.withInputsAndOutputs
import matt.ffmpeg.filtergraph.withValue
import matt.lang.anno.SeeURL

fun FilterChainDsl.commandSuppliedCrop() {
    filters += Crop.suppliedByCommand()
}

fun FilterChainDsl.crop(
    crop: CropBoxLike?,
    keepAspect: Int? = null,
    exact: Int? = null,
    inputs: List<String> = listOf(),
    outputs: List<String> = listOf()
) {
    filters += Crop.withValue(StaticCrop(crop, keepAspect = keepAspect, exact = exact))
        .withInputsAndOutputs(inputs = inputs, outputs = outputs)
}


object Crop : FiltergraphFilter<StaticCrop> {
    override val name = "crop"
}

infix fun <A, B> A.toIfBIsNotNull(that: B?): Pair<A, B>? = that?.let { Pair(this, that) }

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html#crop")
class StaticCrop(
    private val box: CropBoxLike?,
    private val keepAspect: Int? = null,
    private val exact: Int? = null
) : FiltergraphValue {
    override fun expression() = listOf(
        "w" toIfBIsNotNull box?.width,
        "h" toIfBIsNotNull box?.height,
        "x" toIfBIsNotNull box?.x,
        "y" toIfBIsNotNull box?.y,
        "keep_aspect" toIfBIsNotNull keepAspect?.toString(),
        "exact" toIfBIsNotNull exact?.toString()
    ).filterNotNull().joinToString(separator = ":") {
        "${it.first}=${it.second}"
    }
}

class CropConstants : GlobalConstants() {
    val n = FfmpegConstant("n")
}

interface CropBoxLike {
    val x: Any?
    val y: Any?
    val width: Any?
    val height: Any?
}

data class CropBox(
    override val x: Int? = null,
    override val y: Int? = null,
    override val width: Int? = null,
    override val height: Int? = null
) : CropBoxLike

data class CropBoxExpression(
    override val x: String?,
    override val y: String?,
    override val width: String?,
    override val height: String?
) : CropBoxLike