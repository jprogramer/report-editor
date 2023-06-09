import dz.nexatech.reporter.client.core.ReporterExtension
import dz.nexatech.reporter.client.core.TemplateLoader
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.template.PebbleTemplate
import java.io.InputStream
import java.io.StringWriter
import java.util.function.Function
import java.util.function.Predicate

class TemplateParser(
    val configs: Configs,
    val templateContentLoader: Function<String, InputStream?>,
    val templateExistenceChecker: Predicate<String>,
) {

    private val log = logger()

    private val templateLoader = TemplateLoader(
        templateContentLoader = templateContentLoader,
        templateExistenceChecker = templateExistenceChecker,
    )

    @Volatile
    private var pebbleEngine: PebbleEngine? = null

    fun evaluateState(templateName: String): String = try {
        val pebbleTemplate = compileTemplateBlocking(templateName)
        val writer = StringWriter()
        pebbleTemplate.evaluate(writer, configs.loadEnvironment(), Configs.settings.templateLocale)
        writer.toString()
    } catch (e: Exception) {
        log.error("error while evaluating the template", e)
        "Exception: ${e.message}"
    }

    private fun compileTemplateBlocking(templateName: String): PebbleTemplate {
        val currentEngine = pebbleEngine
        if (currentEngine == null) {
            val newEngine = PebbleEngine.Builder()
                .newLineTrimming(false)
                .autoEscaping(true)
                .extension(ReporterExtension)
                .loader(templateLoader)
                .build()
            pebbleEngine = newEngine
            return newEngine.getTemplate(templateName)
        } else {
            return currentEngine.getTemplate(templateName)
        }
    }
}