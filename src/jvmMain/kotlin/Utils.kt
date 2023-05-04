import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.Component
import java.awt.Desktop
import java.io.File
import javax.swing.JFileChooser

fun chooseDirectory(parent: Component): String? {
    val fileChooser = JFileChooser().apply {
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    }
    val result = fileChooser.showOpenDialog(parent)
    return if (result == JFileChooser.APPROVE_OPTION) fileChooser.selectedFile.absolutePath else null
}

fun openFile(file: File) {
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.OPEN)) {
            desktop.open(file)
        }
    }
}

fun Any.logger(): Logger = LoggerFactory.getLogger(this.javaClass)