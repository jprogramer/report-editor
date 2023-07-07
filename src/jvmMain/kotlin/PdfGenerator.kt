import com.itextpdf.styledxmlparser.resolver.resource.IResourceRetriever
import dz.nexatech.reporter.client.common.withIO
import dz.nexatech.reporter.client.core.PdfConverter
import kotlinx.coroutines.runBlocking
import java.io.*
import java.net.URL
import java.util.*

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
        fontsLoader = { fontsData }
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

        private const val RESOURCES_PREFIX = "/web/"

        val fontsData: Collection<ByteArray> by lazy {
            val result = LinkedList<ByteArray>()
            for (fontDir in fontDirectories) {
                loadFontDir(fontDir, result)
            }
            result
        }

        private fun loadFontDir(name: String, result: MutableList<ByteArray>) {
            val loader = PdfGenerator::class.java.classLoader
            addFontFile(loader, name, "Bold.ttf", result)
            addFontFile(loader, name, "Regular.ttf", result)
        }

        private fun addFontFile(
            loader: ClassLoader,
            dirName: String,
            fileName: String,
            result: MutableList<ByteArray>,
        ) {
            val asset = "fonts/$dirName/$fileName"
            try {
                val inputStream = loader.getResourceAsStream(asset)
                if (inputStream != null) {
                    result.add(inputStream.readAllBytes())
                    log.debug("font asset loaded: $asset")
                } else {
                    log.warn("font asset not found: $asset")
                }
            } catch (e: Exception) {
                log.error("error while loading font asset: $asset", e)
            }
        }
    }
}
