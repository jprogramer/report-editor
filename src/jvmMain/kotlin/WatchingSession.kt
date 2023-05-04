import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class WatchingSession(
    workDir: String,
    htmlFileName: String,
    private val onError: (String) -> Unit = {},
    private val onUpdate: () -> Unit = {},
) {
    companion object {
        private val log = logger()
    }

    val pdfGenerator = PdfGenerator(workDir)

    private val closed = AtomicBoolean()

    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var nextUpdate: ScheduledFuture<*>? = null


    private val workDirPath: Path = Paths.get(workDir)
    private val inputFile = File(workDir, "$htmlFileName.html")

    private val watcher: ExecutorService = Executors.newSingleThreadExecutor().also {
        it.submit {
            try {
                if (workDirPath.exists() and workDirPath.isDirectory()) {
                    if (inputFile.exists()) {
                        update()

                        FileSystems.getDefault().newWatchService().use { watchService ->
                            workDirPath.register(
                                watchService,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_DELETE,
                                StandardWatchEventKinds.ENTRY_MODIFY
                            )

                            while (true) {
                                val key = watchService.take()
                                if (key.pollEvents().isNotEmpty()) {
                                    update()
                                }

                                val valid = key.reset()
                                if (!valid) {
                                    log.warn("watching session get invalidated!")
                                    break
                                }
                            }
                        }
                    } else {
                        warn("input file not found: ${inputFile.absolutePath}")
                    }
                } else {
                    warn("invalid work directory: $workDirPath")
                }
            } catch (e: InterruptedException) {
                onError("")
                log.debug("watching session interrupted")
            } catch (e: Exception) {
                onError("'" + e.javaClass.simpleName + "' exception with message: '" + e.message + "'")
                log.error("watching session stopped", e)
            }
        }
    }

    private fun warn(message: String) {
        onError(message)
        log.warn(message)
    }

    private val updater = Runnable {
        val output = File.createTempFile("$htmlFileName-preview", ".pdf")
        pdfGenerator.generatePDF(inputFile, output)
        SwingUtilities.invokeLater(onUpdate)
    }

    private fun update() {
        if (!closed.get()) {
            nextUpdate?.cancel(false)
            nextUpdate = null
            nextUpdate = scheduler.schedule(updater, 90L, TimeUnit.MILLISECONDS)
        } else {
            throw InterruptedException()
        }
    }

    fun close() {
        if (!closed.getAndSet(true)) {
            watcher.shutdownNow()
            scheduler.shutdownNow()
        }
    }
}
