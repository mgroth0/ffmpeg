package matt.ffmpeg.filtergraph

import matt.lang.anno.SeeURL
import matt.prim.str.joinWithCommas

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html")
class Filtergraph(vararg val filters: FiltergraphFilter) {
    fun arg(): String = filters.joinWithCommas()
}

interface FiltergraphFilter {
    fun arg(): String
}

class Scale(private val width: ScaleDimension, private val height: ScaleDimension) : FiltergraphFilter {
    init {
        require(listOf(width, height).filterIsInstance<MaintainAspectRatioDivisibleBy>().size < 2)
    }

    override fun arg(): String {
        return "scale=$width:$height"
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

@SeeURL("http://trac.ffmpeg.org/wiki/Scaling")
class PixelAspectRatio(private val widthRatio: Int, private val heightRatio: Int) : FiltergraphFilter {
    override fun arg(): String {
        return "setsar=$widthRatio/$heightRatio"
    }
}


interface FfmpegSelect : FiltergraphFilter {
    override fun arg() = "select=${selectValue}"
    val selectValue: String
}

class StaticSelect(frames: List<Int>) : FfmpegSelect {
    val rawValue = frames.joinToString(separator = "+") { "eq(n\\,$it)" }
    override val selectValue = "'$rawValue'"
}

class VariableNameSelect(variableName: String) : FfmpegSelect {
    override val selectValue: String = "\$$variableName"
}

/*

interface Crop {
    val selectValue: String
}

class StaticCrop(frames: List<Int>) : FfmpegSelect {
    val rawValue = frames.joinToString(separator = "+") { "eq(n\\,$it)" }
    override val selectValue = "'$rawValue'"
}

class VariableNameCrop(variableName: String) : FfmpegSelect {
    override val selectValue: String = "\$$variableName"
}*/
