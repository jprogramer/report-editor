import Configs.Companion.RES_DIR_PATH
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities

class WatchingSession(
    configs: Configs,
    workDirPath: String,
    private val htmlFileName: String,
    private val onError: (String) -> Unit = {},
    private val onUpdate: () -> Unit = {},
) {
    companion object {
        private val log = logger()
    }

    private val closed = AtomicBoolean()

    private val scheduler: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var nextUpdate: ScheduledFuture<*>? = null

    private val workDir = File(workDirPath)
    private val resDir = File(workDirPath, RES_DIR_PATH)
    private val inputFile = File(workDirPath, htmlFileName)
    private val pdfGenerator = PdfGenerator(configs, workDirPath, inputFile, onError)

    private val watcher: ExecutorService = Executors.newSingleThreadExecutor().also {
        it.submit {
            try {
                if (workDir.exists() and workDir.isDirectory) {
                    if (inputFile.exists()) {
                        update()

                        FileSystems.getDefault().newWatchService().use { watchService ->
                            log.info("watching: $workDir")
                            workDir.toPath().register(
                                watchService,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_DELETE,
                                StandardWatchEventKinds.ENTRY_MODIFY
                            )

                            if (resDir.exists() and resDir.isDirectory) {
                                val files = resDir.listFiles()
                                if (files != null) {
                                    for (res in files) {
                                        log.info("watching: $res")
                                        res.toPath().register(
                                            watchService,
                                            StandardWatchEventKinds.ENTRY_CREATE,
                                            StandardWatchEventKinds.ENTRY_DELETE,
                                            StandardWatchEventKinds.ENTRY_MODIFY
                                        )
                                    }
                                }
                            }

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
        pdfGenerator.generatePDF(File.createTempFile("$htmlFileName-preview", ".pdf"))
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

    fun viewHtml() {
        pdfGenerator.generateHtml(File.createTempFile("$htmlFileName-preview", ".html"))
    }

    fun editHtml() {
        pdfGenerator.generateHtml(File.createTempFile("$htmlFileName-preview", ".txt"))
    }

    fun close() {
        if (!closed.getAndSet(true)) {
            onError("")
            watcher.shutdownNow()
            scheduler.shutdownNow()
        }
    }
}
