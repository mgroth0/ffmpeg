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
    constructor(int: Int) : this(int.toString())
}


abstract class FFmpegExpressionFunction(
    val name: String
) : FfmpegExpression {
    abstract val arguments: List<FfmpegExpression>
    final override val expression: String get() = "$name(${arguments.joinToString("\\,") { it.expression }})"
}


class If(
    condition: FfmpegExpression,
    then: FfmpegExpression,
    val `else`: FfmpegExpression? = null
) : FFmpegExpressionFunction("if") {
    override val arguments = listOfNotNull(
        condition,
        then,
        `else`
    )
}

class Eq(
    first: FfmpegExpression,
    second: FfmpegExpression,
) : FFmpegExpressionFunction("eq") {
    override val arguments = listOf(
        first,
        second,
    )
}

class Div(
    first: FfmpegExpression,
    second: FfmpegExpression,
) : FfmpegExpression {
    override val expression =
        "${first.expression}/${second.expression}"
}
