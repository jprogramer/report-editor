import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.awt.Component
import java.util.*

class MainViewModel(val scope: CoroutineScope) {

    private var watchingSession: WatchingSession? = null

    var configs = Configs()

    private val _workDir: MutableState<String> = mutableStateOf(Configs.settings.initWorkDir)
    val workDir: State<String> = _workDir

    private val _htmlFileName: MutableState<String> = mutableStateOf(Configs.settings.initTemplateFile)
    val htmlFileName: State<String> = _htmlFileName

    private val _status = mutableStateOf("")
    val status: State<String> = _status

    init {
        scope.launch {
            refresh()
        }
    }

    fun openWorkDirChooser(parent: Component) {
        updateWorkDir(chooseDirectory(parent))
    }

    fun updateWorkDir(newWorkDir: String?) {
        if (newWorkDir != null) {
            scope.launch {
                _workDir.value = newWorkDir
                refresh()
            }
        }
    }

    fun updateHtmlFileName(newFileName: String?) {
        if (newFileName != null) {
            scope.launch {
                _htmlFileName.value = newFileName
                refresh()
            }
        }
    }

    fun refreshAll() {
        scope.launch {
            configs = Configs()
            refresh()
        }
    }

    fun refresh() {
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
                    scope.launch { _status.value = error }
                },
                onUpdate = {
                    scope.launch { _status.value = "last update: " + Date() }
                }
            )
        } else {
            _status.value = "can't preview with an empty path!"
        }
    }
}