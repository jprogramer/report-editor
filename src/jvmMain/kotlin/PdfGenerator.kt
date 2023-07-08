import com.itextpdf.styledxmlparser.resolver.resource.IResourceRetriever
import dz.nexatech.reporter.client.common.withIO
import dz.nexatech.reporter.client.core.PdfConverter
import kotlinx.coroutines.runBlocking
import java.io.*
import java.net.URL

class PdfGenerator(
    configs: Configs,
    val workDir: String,
    val inputFile: File,
) {

    companion object {
        private val log = logger()
    }

    private val templateParser: TemplateParser = TemplateParser(
        configs = configs,
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
        fontsLoader = { configs.loadFont(workDir) }
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
}
