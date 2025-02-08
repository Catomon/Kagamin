import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import chu.monscout.kagamin.audio.DenpaPlayer
import chu.monscout.kagamin.audio.DenpaTrack
import java.awt.Desktop
import java.net.URI
import java.util.Locale
import kotlin.math.roundToInt

expect fun <T : DenpaTrack> createDenpaTrack(uri: String, name: String): T

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No Snackbar Host State provided")
}

@Composable
expect fun DenpaFilePicker(
    show: MutableState<Boolean>,
    denpaPlayer: DenpaPlayer<DenpaTrack>,
    currentPlaylistName: String
)

fun formatMilliseconds(ms: Long): String {
    val minutes = (ms / 1000) / 60
    val seconds = (ms / 1000) % 60
    return if (ms > 3596400000L) "999:99:99" else String.format("%1d:%02d", minutes, seconds)
}

fun openGitHub() {
    openInBrowser(URI.create("https://github.com/Catomon"))
}

fun openInBrowser(uri: URI) {
    val osName by lazy(LazyThreadSafetyMode.NONE) { System.getProperty("os.name").lowercase(Locale.getDefault()) }
    val desktop = Desktop.getDesktop()
    when {
        Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE) -> desktop.browse(uri)
        "mac" in osName -> Runtime.getRuntime().exec("open $uri")
        "nix" in osName || "nux" in osName -> Runtime.getRuntime().exec("xdg-open $uri")
        //else -> throw RuntimeException("cannot open $uri")
    }
}

fun isValidFileName(name: String): Boolean {
    val forbiddenNames = listOf(
        "CON", "PRN", "AUX", "NUL",
        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM0",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", "LPT0"
    )
    val specialChars = listOf("<", ">", ":", "\"", "/", "\\", "|", "?", "*")

    return !(forbiddenNames.any { name.contains(it, true) } || specialChars.any {
        name.contains(it, true)
    })
}

private val smoothPaint = Paint().apply {
    filterQuality = FilterQuality.High

    asFrameworkPaint().apply {
        this.isAntiAlias = false
    }
}

class SmoothPainter(
    private val image: ImageBitmap,
    private val srcOffset: IntOffset = IntOffset.Zero,
    private val srcSize: IntSize = IntSize(image.width, image.height),
) : Painter() {

    init {
        Paint()
    }

    private val size: IntSize = validateSize(srcOffset, srcSize)
    override fun DrawScope.onDraw() {
        drawImage(
            image,
            srcOffset,
            srcSize,
            dstSize = IntSize(
                this@onDraw.size.width.roundToInt(),
                this@onDraw.size.height.roundToInt()
            ),
            filterQuality = FilterQuality.High,
        )
    }

    /**
     * Return the dimension of the underlying [ImageBitmap] as it's intrinsic width and height
     */
    override val intrinsicSize: Size get() = size.toSize()

    private fun validateSize(srcOffset: IntOffset, srcSize: IntSize): IntSize {
        require(
            srcOffset.x >= 0 &&
                    srcOffset.y >= 0 &&
                    srcSize.width >= 0 &&
                    srcSize.height >= 0 &&
                    srcSize.width <= image.width &&
                    srcSize.height <= image.height
        )
        return srcSize
    }
}