import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.awt.Component
import java.util.*
import javax.swing.SwingUtilities

class MainViewModel {

    private val _workDir: MutableState<String> = mutableStateOf("")
    val workDir: State<String> = _workDir

    private val _htmlFileName: MutableState<String> = mutableStateOf("templates/test")
    val htmlFileName: State<String> = _htmlFileName

    private val _status = mutableStateOf("")
    val status: State<String> = _status

    private var watchingSession: WatchingSession? = null

    init {
        refresh()
    }

    fun openWorkDirChooser(parent: Component) {
        updateWorkDir(chooseDirectory(parent))
    }

    fun updateWorkDir(newWorkDir: String?) {
        if (newWorkDir != null) {
            _workDir.value = newWorkDir
            refresh()
        }
    }

    fun updateHtmlFileName(newFileName: String?) {
        if (newFileName != null) {
            _htmlFileName.value = newFileName
            refresh()
        }
    }

    fun refresh() {
        assert(SwingUtilities.isEventDispatchThread())

        watchingSession?.close()
        watchingSession = null

        val currentWorkDir = workDir.value
        val currentHtmlFile = htmlFileName.value
        if (currentWorkDir.isNotEmpty() and currentHtmlFile.isNotEmpty()) {
            _status.value = "loading..."
            watchingSession = WatchingSession(
                currentWorkDir,
                currentHtmlFile,
                onError = { error ->
                    _status.value = error
                },
                onUpdate = {
                    _status.value = "last update: " + Date()
                }
            )
        } else {
            _status.value = "can't preview with an empty path!"
        }
    }
}