import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.common.collect.ImmutableCollection
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.io.Files
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Configs {

    private val log = logger()

    private val loader = Configs::class.java.classLoader

    private val fontsCache = ConcurrentHashMap<String, ImmutableCollection<ByteArray>>()

    val settings by lazy {
        Settings()
    }

    class Settings(
        val defaultPadding: Dp = 4.dp,
        val defaultWorkDir: String = "C:\\Users\\Joseph\\Desktop\\examples",
        val defaultFile: String = "example_01.html",
        val parsingLocale: Locale = Locale.FRANCE,
        val mainFontName: String = "Noto Naskh Arabic",
    )

    val environment: ImmutableMap<String, Any> by lazy {
        ImmutableMap.builder<String, Any>()
            .put("left_header", "Sevenit GmbH\nHauptstrabe 40\n77654 Offenburg")
            .put("right_header", "MonsieurJean Dupont\nAcheteur SA\nRue du Château\n34000 MONTPELLIER")
            .put("bill_number", "1001")
            .put("bill_date", "02 July 2023")
            .put("client_number", "321")
            .put("main_font_name", settings.mainFontName)
            .put("page_color", "#f2f2f2")
            .put("page_width", "595")
            .put(
                "products",
                ImmutableList.builder<ImmutableMap<String, String>>()
                    .add(
                        ImmutableMap.of(
                            "description", "Main-d'oeuvre",
                            "quantity", "30",
                            "unite", "h.",
                            "price", "40.0",
                            "tax", "20",
                        ),
                        ImmutableMap.of(
                            "description", "Tracteur",
                            "quantity", "1",
                            "unite", "pce.",
                            "price", "1800",
                            "tax", "20",
                        ),
                        ImmutableMap.of(
                            "description", "Bois de chauffage",
                            "quantity", "10",
                            "unite", "stére",
                            "price", "80.00",
                            "tax", "10",
                        ),
                    )
                    .build()
            )
            .build()
    }

    fun loadFont(workDir: String, fontName: String = settings.mainFontName): ImmutableCollection<ByteArray> =
        fontsCache.computeIfAbsent(fontName) {
            val fontDirName = fontName.replace(" ", "")
            val builder = ImmutableList.Builder<ByteArray>()
            loadFont(workDir, "fonts/$fontDirName/Bold.ttf", builder)
            loadFont(workDir, "fonts/$fontDirName/Regular.ttf", builder)
            builder.build()
        }

    private fun loadFont(
        workDir: String,
        fontPath: String,
        builder: ImmutableList.Builder<ByteArray>,
    ) {
        val fontFileBytes = loadFontFileBytes(File(workDir, fontPath))
        if (fontFileBytes != null) {
            builder.add(fontFileBytes)
        } else {
            val fontAssetBytes = loadFontAssetBytes(fontPath)
            if (fontAssetBytes != null) {
                builder.add(fontAssetBytes)
            }
        }
    }

    private fun loadFontFileBytes(fontFile: File): ByteArray? {
        if (fontFile.exists()) {
            try {
                return Files.toByteArray(fontFile)
            } catch (e: Exception) {
                log.error("error while loading font file: ${fontFile.absolutePath}", e)
            }
        } else {
            log.warn("font file not found: ${fontFile.absolutePath}")
        }

        return null
    }

    private fun loadFontAssetBytes(
        fontPath: String,
    ): ByteArray? {
        try {
            val inputStream = loader.getResourceAsStream(fontPath)
            if (inputStream != null) {
                val bytes = inputStream.readAllBytes()
                log.debug("font asset loaded: $fontPath")
                return bytes;
            } else {
                log.warn("font asset not found: $fontPath")
            }
        } catch (e: Exception) {
            log.error("error while loading font asset: $fontPath", e)
        }

        return null
    }
}