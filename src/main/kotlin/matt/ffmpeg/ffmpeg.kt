package matt.ffmpeg

import matt.ffmpeg.ffmpegPath.homebrewFFmpeg
import matt.ffmpeg.ffmpegPath.justFFmpeg
import matt.ffmpeg.filtergraph.FilterChain
import matt.ffmpeg.filtergraph.FilterGraph
import matt.lang.If
import matt.lang.anno.SeeURL
import matt.lang.opt
import matt.lang.optArray
import matt.model.data.file.FilePath
import matt.shell.Shell
import matt.shell.ShellProgramPathContext.HomeBrew
import matt.shell.ShellProgramPathContext.InPath

enum class FFmpegPixelFormat {
    rgb8
}

const val DEFAULT_MAX_STREAMS = 1000

enum class ffmpegPath(path: String? = null) {
    justFFmpeg("ffmpeg"), homebrewFFmpeg("/opt/homebrew/bin/ffmpeg");

    val command = path ?: name
}

enum class FFMpegLogLevel {
    quiet,
    panic,
    fatal,
    error,
    warning,
    info,
    verbose,
    debug,
    trace
}

@SeeURL("https://ffmpeg.org/ffmpeg.html")
fun <R> Shell<R>.ffmpeg(
    input: FilePath,
    encoderPixelFormat: FFmpegPixelFormat? = null,
    output: FilePath,
    outputFps: Int? = null,
    overwrite: Boolean,
    filtergraph: FilterChain? = null,
    complexFiltergraph: FilterGraph? = null,
    hardwareAcceleration: String? = null,
    decoderCodec: String? = null,
    threads: Int? = null,
    markExtractedImagesAccordingToSourceFrameNumber: Boolean = false,
    suppressDuplication: Boolean = false,
    maxStreams: Int? = null, /*does not seem to work*/
    encoderTimeBase: Int? = null,
    autoScale: Boolean? = null,
    reinitFilter: Boolean? = null,
    logLevel: FFMpegLogLevel? = null,
): R {
    require(listOfNotNull(filtergraph, complexFiltergraph).size <= 1)
    val ffmpegPath = when (programPathContext) {
        InPath   -> justFFmpeg
        HomeBrew -> homebrewFFmpeg
    }
    return sendCommand(
        ffmpegPath.command,
        *optArray(logLevel) {
            arrayOf("-loglevel", this.name)
        },
        if (overwrite) "-y" else "-n",
        *optArray(maxStreams) {
            @SeeURL("https://stackoverflow.com/questions/64490002/ffmpeg-consider-increasing-probesize-error-but-it-is-never-satisfied")
            /*needs to be both input and output*/
            arrayOf("-max_streams", maxStreams.toString())
        },
        *optArray(hardwareAcceleration) {
            @SeeURL("https://stackoverflow.com/a/63585334/6596010")
            arrayOf("-hwaccel", this)
        },
        *optArray(decoderCodec) {
            @SeeURL("https://stackoverflow.com/a/63585334/6596010")
            arrayOf("-c:v", this)
        },
        *optArray(reinitFilter) { arrayOf("-reinit_filter", if (this) "1" else "0") },
        "-i",
        input.filePath,
        *optArray(maxStreams) {
            @SeeURL("https://stackoverflow.com/questions/64490002/ffmpeg-consider-increasing-probesize-error-but-it-is-never-satisfied")
            /*needs to be both input and output*/
            arrayOf("-max_streams", maxStreams.toString())
        },
        *optArray(encoderPixelFormat) { arrayOf("-pxl_fmt", "+${name}") },
        *optArray(outputFps) { arrayOf("-r", toString()) },

        *optArray(filtergraph) { arrayOf("-vf", arg()) },
        *optArray(complexFiltergraph) { arrayOf("-filter_complex", arg()) },
        *optArray(threads) { arrayOf("-threads", threads.toString()) },
        *opt(autoScale) {
            if (this) "-autoscale" else "-noautoscale"
            /*arrayOf("-autoscale", threads.toString()) */
        },

        *If(markExtractedImagesAccordingToSourceFrameNumber).then(
            @SeeURL("https://stackoverflow.com/a/38259151/6596010")
            "-frame_pts",
            "1"
        ),
        *optArray(encoderTimeBase) {
            arrayOf(
                "-enc_time_base",
                this.toString()
            )
        },
        /*VSYNC ARGUMENT IS DEPRECATED!*/
        /**If(suppressDuplication).then(
        @SeeURL("https://stackoverflow.com/a/38259151/6596010")
        "-vsync",
        "0"
        ),*/
        *If(suppressDuplication).then(
            @SeeURL("https://stackoverflow.com/a/38259151/6596010")
            "-fps_mode",
            "passthrough"
        ),
        output.filePath
    )
}

