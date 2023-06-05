package matt.ffmpeg.filtergraph

import matt.lang.anno.SeeURL
import matt.model.data.file.FilePath
import matt.prim.str.joinWithCommas


fun filterGraph(op: FilterGraphDsl.() -> Unit) = FilterGraphDsl().apply(op).build()


class FilterGraphDsl {
    internal val chains = mutableListOf<FilterChain>()
    fun chain(op: FilterChainDsl.() -> Unit): FilterChain {
        val chain = filterChain(op)
        chains += filterChain(op)
        return chain
    }

    internal fun build() = StaticFilterGraph(*chains.toTypedArray())
}

interface FilterGraph {
    fun arg(): String
}

class VariableFilterGraph(val variableName: String) : FilterGraph {
    override fun arg() = "\$$variableName"
}

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html")
class StaticFilterGraph(private vararg val filterChains: FilterChain) : FilterGraph {
    override fun arg(): String = filterChains.joinToString(separator = ";") { it.arg() }
}

fun filterChain(op: FilterChainDsl.() -> Unit) = FilterChainDsl().apply(op).build()


class FilterChainDsl {
    internal val filters = mutableListOf<SuppliedFilter>()
    internal fun build() = FilterChain(*filters.toTypedArray())
}

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html")
class FilterChain(private vararg val filters: SuppliedFilter) {
    fun arg(): String = filters.joinWithCommas { it.argument() }
}

interface FiltergraphFilter<V : FiltergraphValue> {
    val name: String
}


interface SuppliedFilter {
    val filter: FiltergraphFilter<*>
    val valueExpression: String
    val inputs: List<String>
    val outputs: List<String>
}


fun SuppliedFilter.argument() =
    "${inputs.joinToString(separator = "") { "[$it]" }}${filter.name}=${valueExpression}${outputs.joinToString(separator = "") { "[$it]" }}"

infix fun <V : FiltergraphValue> FiltergraphFilter<V>.withValue(value: V) = StaticSuppliedFilter(this, value)

abstract class SimpleFilter : SuppliedFilter {
    override val inputs = listOf<String>()
    override val outputs = listOf<String>()
}

class StaticSuppliedFilter<V : FiltergraphValue, F : FiltergraphFilter<V>>(
    override val filter: F,
    val value: FiltergraphValue
) : SimpleFilter() {
    override val valueExpression = value.expression()

}


infix fun FiltergraphFilter<*>.withVariable(variable: String) = VariableSuppliedFilter(this, variable)

class VariableSuppliedFilter(
    override val filter: FiltergraphFilter<*>,
    variable: String
) : SimpleFilter() {
    override val valueExpression = "\"\$$variable\""
}

infix fun FiltergraphFilter<*>.withFile(file: FilePath) = FileSuppliedFilter(this, file)

class FileSuppliedFilter(
    override val filter: FiltergraphFilter<*>,
    val file: FilePath
) : SimpleFilter() {
    init {
        /*must be absolute*/
        require(file.filePath.startsWith("/"))
    }

    override val valueExpression = file.filePath
}


interface FiltergraphValue {
    fun expression(): String
}

fun SimpleFilter.withInputsAndOutputs(
    inputs: List<String>,
    outputs: List<String>
) = ComplexFilter(this, inputs = inputs, outputs = outputs)


class ComplexFilter(
    private val simpleFilter: SuppliedFilter,
    override val inputs: List<String>,
    override val outputs: List<String>
) :
    SuppliedFilter {
    override val filter: FiltergraphFilter<*> = simpleFilter.filter
    override val valueExpression: String get() = simpleFilter.valueExpression
}



