package matt.ffmpeg.filtergraph.sendcmd

import matt.ffmpeg.filtergraph.FilterChainDsl
import matt.ffmpeg.filtergraph.FiltergraphFilter
import matt.ffmpeg.filtergraph.FiltergraphValue
import matt.ffmpeg.filtergraph.withInputsAndOutputs
import matt.ffmpeg.filtergraph.withValue
import matt.lang.anno.SeeURL
import matt.model.code.SimpleCodeGenerator
import matt.model.code.SimpleFormatCode
import matt.model.data.file.FilePath
import matt.prim.str.mybuild.lineDelimitedString
import kotlin.time.Duration
import kotlin.time.DurationUnit.SECONDS

object FfmpegSendCmd : FiltergraphFilter<SendCmd> {
    override val name = "sendcmd"
}

fun FilterChainDsl.sendcmd(
    command: FfmpegCommand,
    inputs: List<String> = listOf(),
    outputs: List<String> = listOf()
) {
    filters += FfmpegSendCmd.withValue(StaticSendCmd(command)).withInputsAndOutputs(inputs = inputs, outputs = outputs)
}

fun FilterChainDsl.sendcmdFile(
    file: FilePath,
    inputs: List<String> = listOf(),
    outputs: List<String> = listOf()
) {
    filters += FfmpegSendCmd.withValue(FileSendCmd(file)).withInputsAndOutputs(inputs = inputs, outputs = outputs)
}

interface SendCmd : FiltergraphValue

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html#sendcmd_002c-asendcmd")
class StaticSendCmd(private val command: FfmpegCommand) : SendCmd {
    override fun expression() = "c=${command.code}"
}

@SeeURL("https://ffmpeg.org/ffmpeg-filters.html#sendcmd_002c-asendcmd")
class FileSendCmd(private val file: FilePath) : SendCmd {
    override fun expression() = "f=${file.filePath}"
}


fun ffmpegCommand(op: FfmpegCommandRootDsl.() -> Unit): FfmpegCommand {
    return FfmpegCommandRootDsl().apply(op).generate()
}

@DslMarker
annotation class FfmpegCommandDsl

@FfmpegCommandDsl
class FfmpegCommandRootDsl : SimpleCodeGenerator<FfmpegCommand> {
    private val intervals = mutableListOf<FfmpegCommandInterval>()
    fun interval(duration: Duration, op: FfmpegCommandIntervalDsl.() -> Unit) {
        intervals += FfmpegCommandIntervalDsl(duration).apply(op).generate()
    }

    override fun generate(): FfmpegCommand {
        return FfmpegCommand(*intervals.toTypedArray())
    }
}

@FfmpegCommandDsl
class FfmpegCommandIntervalDsl(private val interval: Duration) {
    private val commands = mutableListOf<FfmpegSingleCommand>()

    fun enter(op: FfmpegEventedCommandIntervalDsl.() -> Unit) {
        commands += FfmpegEventedCommandIntervalDsl(interval, FfmpegIntervalEvent.enter).apply(op).generate()
    }

    fun leave(op: FfmpegEventedCommandIntervalDsl.() -> Unit) {
        commands += FfmpegEventedCommandIntervalDsl(interval, FfmpegIntervalEvent.leave).apply(op).generate()
    }

    fun generate(): FfmpegCommandInterval {
        return FfmpegCommandInterval(interval, commands.toList())
    }
}

@FfmpegCommandDsl
class FfmpegEventedCommandIntervalDsl(private val interval: Duration, private val event: FfmpegIntervalEvent) {
    private val commands = mutableListOf<FfmpegSingleCommand>()

    private fun command(
        @Suppress("SameParameterValue") receiver: String,
        command: String,
        arg: String
    ) {
        commands += FfmpegSingleCommand(event = event, receiver = receiver, command = command, argument = arg)
    }

    val crop get() = Crop()

    inner class Crop {
        private fun cropCommand(command: String, arg: String) = command("crop", command, arg)
        infix fun x(x: Int) = cropCommand("x", x.toString())
        infix fun y(y: Int) = cropCommand("y", y.toString())
    }



    fun generate(): List<FfmpegSingleCommand> {
        return commands
    }
}


class FfmpegCommand(private vararg val intervals: FfmpegCommandInterval) : SimpleFormatCode<FfmpegCommand> {
    override val code: String
        get() = lineDelimitedString {
            require(intervals.isNotEmpty())
            intervals.forEach {
                +it.code()
            }
        }

    override fun formatted(): FfmpegCommand {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return code
    }
}


data class FfmpegCommandInterval(val interval: Duration, val commands: List<FfmpegSingleCommand>) {
    fun code(): String {

        require(commands.isNotEmpty())

        return if (commands.size == 1) {
            "${interval.toDouble(SECONDS)} ${commands.single().code()}"
        } else {
            lineDelimitedString {
                +"${interval.toDouble(SECONDS)}"
                commands.dropLast(1).forEach {
                    +"\t${it.code()},"
                }
                +"\t${commands.last().code()}"
            }
        }


    }

    override fun toString(): String {
        return code()
    }
}


enum class FfmpegIntervalEvent {
    enter, leave
}

data class FfmpegSingleCommand(
    val event: FfmpegIntervalEvent,
    val receiver: String,
    val command: String,
    val argument: String
) {
    fun code() = "[${event.name}] $receiver $command $argument"

    override fun toString(): String {
        return code()
    }
}