package matt.ffmpeg

import matt.ffmpeg.filtergraph.Filtergraph
import matt.lang.If
import matt.lang.anno.SeeURL
import matt.lang.optArray
import matt.model.data.file.FilePath
import matt.shell.Shell

enum class FFmpegPixelFormat {
    rgb8
}


@SeeURL("https://ffmpeg.org/ffmpeg.html")
fun <R> Shell<R>.ffmpeg(
    input: FilePath,
    encoderPixelFormat: FFmpegPixelFormat? = null,
    output: FilePath,
    outputFps: Int? = null,
    overwrite: Boolean,
    filtergraph: Filtergraph? = null,
    hardwareAcceleration: String? = null,
    decoderCodec: String? = null,
    threads: Int? = null,
    markExtractedImagesAccordingToSourceFrameNumber: Boolean = false,
    suppressDuplication: Boolean = false,
): R = sendCommand(
    this::ffmpeg.name,
    if (overwrite) "-y" else "-n",
    *optArray(hardwareAcceleration) {
        @SeeURL("https://stackoverflow.com/a/63585334/6596010")
        arrayOf("-hwaccel", this)
    },
    *optArray(decoderCodec) {
        @SeeURL("https://stackoverflow.com/a/63585334/6596010")
        arrayOf("-c:v", this)
    },
    "-i",
    input.filePath,
    *optArray(encoderPixelFormat) { arrayOf("-pxl_fmt", "+${name}") },
    *optArray(outputFps) { arrayOf("-r", toString()) },
    *optArray(filtergraph) { arrayOf("-vf", arg()) },
    *optArray(threads) { arrayOf("-threads", threads.toString()) },


    *If(markExtractedImagesAccordingToSourceFrameNumber).then(
        @SeeURL("https://stackoverflow.com/a/38259151/6596010")
        "-frame_pts",
        "1"
    ),
    *If(suppressDuplication).then(
        @SeeURL("https://stackoverflow.com/a/38259151/6596010")
        "-vsync",
        "0"
    ),
    output.filePath
)

