import com.itextpdf.styledxmlparser.resolver.resource.IResourceRetriever
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL

class ResourcesLoader(val workDir: String): IResourceRetriever {

    companion object {
        private val log = logger()
    }

    private val loader = this.javaClass.classLoader

    override fun getInputStreamByUrl(url: URL?): InputStream? = openResource(url?.path)

    fun openResource(path: String?): BufferedInputStream? {
        if (path != null) {
            val file = File(workDir, path)
            if (file.exists()) {
                return BufferedInputStream(FileInputStream(file))
            }

            val resourceInputSteam = loader.getResourceAsStream(path.removePrefix("/"))
            if (resourceInputSteam != null) {
                return BufferedInputStream(resourceInputSteam)
            }

            log.warn("resource not found: ${file.absolutePath}")
        }
        return null
    }

    override fun getByteArrayByUrl(url: URL?): ByteArray? =
        getInputStreamByUrl(url)?.readAllBytes()
}
