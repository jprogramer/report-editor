import Configs.Companion.Settings.Companion.DEFAULT_MAIN_FONT_NAME
import Configs.Companion.Settings.Companion.DEFAULT_PADDING_UNIT
import Configs.Companion.Settings.Companion.DEFAULT_TEMPLATE_FILE
import Configs.Companion.Settings.Companion.DEFAULT_TEMPLATE_LANG
import Configs.Companion.Settings.Companion.DEFAULT_WORK_DIR
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.common.collect.ImmutableCollection
import com.google.common.collect.ImmutableList
import com.google.common.io.Files
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dz.nexatech.reporter.client.common.AbstractLocalizer.Companion.ARABIC_LOCALE
import dz.nexatech.reporter.client.common.AbstractLocalizer.Companion.ENGLISH_LOCALE
import dz.nexatech.reporter.client.common.AbstractLocalizer.Companion.FRENCH_LOCALE
import dz.nexatech.reporter.client.common.Texts
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Configs {

    companion object {
        private const val configsDir = "configs"

        private val gson = Gson()
        private val envFile = File(configsDir, "env.json")
        private val envTypeToken = object : TypeToken<MutableMap<String, Any>>() {}

        private val log = logger()
        private val loader = Configs::class.java.classLoader

        val settings by lazy {
            val defaultSettings = Settings()

            val configFile = File(configsDir, "settings.properties")
            if (configFile.exists()) {
                val configs = Properties()
                BufferedInputStream(FileInputStream(configFile)).use {
                    configs.load(it)
                }

                Settings(
                    paddingUnitDp = configs.getProperty("padding_unit", DEFAULT_PADDING_UNIT),
                    templateLang = configs.getProperty("template_lang", DEFAULT_TEMPLATE_LANG),
                    initWorkDir = configs.getProperty("init_work_dir", DEFAULT_WORK_DIR),
                    initTemplateFile = configs.getProperty("init_template_file", DEFAULT_TEMPLATE_FILE),
                    mainFontName = configs.getProperty("main_font_name", DEFAULT_MAIN_FONT_NAME),
                )
            } else {
                Settings()
            }
        }

        class Settings(
            paddingUnitDp: String = DEFAULT_PADDING_UNIT,
            templateLang: String = DEFAULT_TEMPLATE_LANG,
            val initWorkDir: String = DEFAULT_WORK_DIR,
            val initTemplateFile: String = DEFAULT_TEMPLATE_FILE,
            val mainFontName: String = DEFAULT_MAIN_FONT_NAME,
        ) {
            val paddingUnit: Dp = (paddingUnitDp.toIntOrNull() ?: DEFAULT_PADDING_UNIT.toInt()).dp

            val templateLocale: Locale = when (templateLang) {
                Texts.LANG_EN -> ENGLISH_LOCALE
                Texts.LANG_AR -> ARABIC_LOCALE
                else -> FRENCH_LOCALE
            }

            companion object {
                const val DEFAULT_PADDING_UNIT = "4"
                const val DEFAULT_TEMPLATE_LANG = "fr"
                val DEFAULT_WORK_DIR = System.getProperty("user.home")
                const val DEFAULT_TEMPLATE_FILE = "test_template.html"
                const val DEFAULT_MAIN_FONT_NAME = "Noto Naskh Arabic"
            }
        }
    }

    private val fontsCache = ConcurrentHashMap<String, ImmutableCollection<ByteArray>>()

    fun loadEnvironment(): Map<String, Any> = gson.fromJson(FileReader(envFile), envTypeToken).apply {
        put("main_font_name", settings.mainFontName)
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