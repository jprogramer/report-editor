import com.itextpdf.styledxmlparser.resolver.resource.IResourceRetriever
import dz.nexatech.reporter.client.core.PdfConverter
import kotlinx.coroutines.runBlocking
import java.io.*
import java.net.URL
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class PdfGenerator(
    val configs: Configs,
    val workDir: String,
    val inputFile: File,
) {

    companion object {
        private val log = logger()
        private val executorService = ThreadPoolExecutor(
            2, Runtime.getRuntime().availableProcessors() * 2,
            60L, TimeUnit.SECONDS,
            SynchronousQueue()
        )
    }

    private fun templateParser(): TemplateParser = TemplateParser(
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

    fun generatePDF(destinationFile: File) {
        executorService.submit {
            try {
                val html = templateParser().evaluateState(inputFile.name)
                val outputStream = BufferedOutputStream(FileOutputStream(destinationFile))
                runBlocking {
                    pdfConverter.generatePDF(outputStream, html)
                }
                openFile(destinationFile)
            } catch (e: Exception) {
                log.error("error while generating: ${destinationFile.absolutePath}", e)
            }
        }
    }
}
