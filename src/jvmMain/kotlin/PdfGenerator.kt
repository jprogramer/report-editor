import Configs.Companion.RES_PREFIX
import com.google.common.io.Files
import dz.nexatech.reporter.client.core.PdfConverter
import kotlinx.coroutines.runBlocking
import java.io.*
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class PdfGenerator(
    val configs: Configs,
    val workDir: String,
    val inputFile: File,
    val onError: (String) -> Unit,
) {

    companion object {
        private val log = logger()
        private val executorService = ThreadPoolExecutor(
            2, Runtime.getRuntime().availableProcessors() * 2,
            60L, TimeUnit.SECONDS,
            SynchronousQueue()
        )

        private val HTML_RES_PATH = Regex("['\"][\\\\/]$RES_PREFIX[^'\"]+['\"]")
    }

    private fun templateParser(): TemplateParser = TemplateParser(
        configs = configs,
        templateContentLoader = {
            if (it == inputFile.name) BufferedInputStream(FileInputStream(inputFile)) else null
        },
        templateExistenceChecker = { it == inputFile.name },
    )

    private val resourceLoader = ResourcesLoader(workDir)
    private val pdfConverter = PdfConverter(
        resourceLoader = resourceLoader,
        fontsLoader = { configs.loadFont(workDir) }
    )

    fun generateHtml(destinationFile: File) {
        compileHtml(destinationFile) { html ->
            val updatedHtml = html.replace(HTML_RES_PATH) {
                val value = it.value
                if (value.length > 5) {
                    val resPath = value.substring(1 , value.length - 1)
                    val resourceStream = resourceLoader.openResource(resPath)

                    val updatedPath = if (resourceStream != null) {
                        val copiedRes = File.createTempFile("res", File(resPath).name)
                        Files.write(resourceStream.readAllBytes(), copiedRes)
                        copiedRes.absolutePath
                    } else {
                        resPath + "_not_found"
                    }

                    val quotes = value[0]
                    quotes + updatedPath + quotes
                } else {
                    value
                }
            }
            Files.write(updatedHtml.toByteArray(), destinationFile)
        }
    }

    fun generatePDF(destinationFile: File) {
        compileHtml(destinationFile) { html ->
            val outputStream = BufferedOutputStream(FileOutputStream(destinationFile))
            runBlocking {
                pdfConverter.generatePDF(outputStream, html)
            }
        }
    }

    private fun compileHtml(destinationFile: File, writer: (String) -> Unit) {
        executorService.submit {
            try {
                writer.invoke(templateParser().evaluateState(inputFile.name))
                openFile(destinationFile)
            } catch (e: Exception) {
                log.error("error while generating: ${destinationFile.absolutePath}", e)
                onError("Error: " + e.message)
            }
        }
    }
}
