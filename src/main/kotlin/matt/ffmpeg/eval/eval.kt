package matt.ffmpeg.eval

import matt.lang.anno.SeeURL


@SeeURL("https://ffmpeg.org/ffmpeg-utils.html#Expression-Evaluation")
interface FfmpegExpression {
    val expression: String
}

open class GlobalConstants


class FfmpegConstant(private val symbol: String) : FfmpegExpression {
    override val expression get() = symbol
}

class RawFfmpegExpression(override val expression: String) : FfmpegExpression {
    constructor(int: Int): this(int.toString())
}

class If(
    condition: FfmpegExpression,
    then: FfmpegExpression,
    val `else`: FfmpegExpression? = null
) : FfmpegExpression {
    override val expression =
        "if(${condition.expression},${then.expression}${`else`?.let { ",${it.expression}" } ?: ""})"
}

class Eq(
    first: FfmpegExpression,
    second: FfmpegExpression,
) : FfmpegExpression {
    override val expression =
        "eq(${first.expression},${second.expression}"
}

class Div(
    first: FfmpegExpression,
    second: FfmpegExpression,
) : FfmpegExpression {
    override val expression =
        "${first.expression}/${second.expression}"
}