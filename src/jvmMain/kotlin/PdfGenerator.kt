import com.google.common.collect.ImmutableCollection
import com.google.common.collect.ImmutableList
import com.google.common.io.Files
import com.itextpdf.styledxmlparser.resolver.resource.IResourceRetriever
import dz.nexatech.reporter.client.common.withIO
import dz.nexatech.reporter.client.core.PdfConverter
import kotlinx.coroutines.runBlocking
import java.io.*
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class PdfGenerator(val workDir: String, val inputFile: File) {

    private val templateParser: TemplateParser = TemplateParser(
        templateContentLoader = {
            if (it == inputFile.name) BufferedInputStream(FileInputStream(inputFile)) else null
        },
        templateExistenceChecker = { it == inputFile.name },
    )

    private val pdfConverter = PdfConverter(
        resourceLoader = object : IResourceRetriever {

            private val loader = this.javaClass.classLoader

            override fun getInputStreamByUrl(url: URL?): InputStream? {
                if (url != null) {
                    val resPath = url.path
                    val file = File(workDir, resPath)
                    if (file.exists()) {
                        return BufferedInputStream(FileInputStream(file))
                    }

                    val resourceInputSteam = loader.getResourceAsStream(resPath.removePrefix("/"))
                    if (resourceInputSteam != null) {
                        return BufferedInputStream(resourceInputSteam)
                    }

                    log.warn("resource not found: ${file.absolutePath}")
                }
                return null
            }


            override fun getByteArrayByUrl(url: URL?): ByteArray? =
                getInputStreamByUrl(url)?.readAllBytes()
        },
        fontsLoader = { loadFont(workDir, mainFontName) }
    )

    fun generatePDF(output: File) {
        runBlocking {
            withIO {
                try {
                    val html = templateParser.evaluateState(inputFile.name)
                    pdfConverter.generatePDF(BufferedOutputStream(FileOutputStream(output)), html)
                    openFile(output)
                } catch (e: Exception) {
                    log.error("error while generating: ${output.absolutePath}", e)
                }
            }
        }
    }

    companion object {

        private val log = logger()

        private val loader = PdfGenerator::class.java.classLoader

        private val fontsCache = ConcurrentHashMap<String, ImmutableCollection<ByteArray>>()

        fun loadFont(workDir: String, fontName: String): ImmutableCollection<ByteArray> =
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
}
