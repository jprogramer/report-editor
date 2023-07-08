import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.awt.Component
import java.util.*
import javax.swing.SwingUtilities

class MainViewModel {

    private var watchingSession: WatchingSession? = null

    var configs = Configs()

    private val _workDir: MutableState<String> = mutableStateOf(Configs.settings.initWorkDir)
    val workDir: State<String> = _workDir

    private val _htmlFileName: MutableState<String> = mutableStateOf(Configs.settings.initTemplateFile)
    val htmlFileName: State<String> = _htmlFileName

    private val _status = mutableStateOf("")
    val status: State<String> = _status

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

    fun refreshAll() {
        configs = Configs()
        refresh()
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
                configs,
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