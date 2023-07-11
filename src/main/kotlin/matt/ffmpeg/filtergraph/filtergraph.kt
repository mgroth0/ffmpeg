package matt.ffmpeg.filtergraph

import matt.lang.anno.SeeURL
import matt.lang.require.requireStartsWith
import matt.model.data.file.FilePath
import matt.prim.str.joinWithCommas
import matt.prim.str.takeIfNotBlank


fun filterGraph(op: FilterGraphDsl.() -> Unit) = FilterGraphDsl().apply(op).build()


class FilterGraphDsl {
    internal val chains = mutableListOf<StaticFilterChain>()
    fun chain(op: FilterChainDsl.() -> Unit): StaticFilterChain {
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

class VariableFilterChain(val variableName: String) : FilterChain {
    override fun arg() = "\$$variableName"
}

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html")
class StaticFilterGraph(private vararg val filterChains: StaticFilterChain) : FilterGraph {
    override fun arg(): String = filterChains.joinToString(separator = ";") { it.arg() }
}

fun filterChain(op: FilterChainDsl.() -> Unit) = FilterChainDsl().apply(op).build()


class FilterChainDsl {
    internal val filters = mutableListOf<SuppliedFilter>()
    internal fun build() = StaticFilterChain(*filters.toTypedArray())
}

interface FilterChain {
    fun arg(): String
}

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html")
class StaticFilterChain(private vararg val filters: SuppliedFilter) : FilterChain {
    override fun arg(): String = filters.joinWithCommas { it.argument() }
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


fun SuppliedFilter.argument() = (valueExpression.takeIfNotBlank()?.let { "=$it" } ?: "").let {
    "${inputs.joinToString(separator = "") { "[$it]" }}${filter.name}$it${outputs.joinToString(separator = "") { "[$it]" }}"
}


infix fun <V : FiltergraphValue> FiltergraphFilter<V>.withValue(value: V) = StaticSuppliedFilter(this, value)

fun <V : FiltergraphValue> FiltergraphFilter<V>.suppliedByCommand() = CommandSuppliedFilter(this)


class CommandSuppliedFilter<V : FiltergraphValue, F : FiltergraphFilter<V>>(
    override val filter: F
) : SimpleFilter() {


    override val valueExpression = ""


}

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
    init {/*must be absolute*/
        requireStartsWith(file.filePath, "/")
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
) : SuppliedFilter {
    override val filter: FiltergraphFilter<*> = simpleFilter.filter
    override val valueExpression: String get() = simpleFilter.valueExpression
}



