package com.app.dockeep.ui

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.app.dockeep.data.files.FilesRepository
import com.app.dockeep.data.preferences.DataStoreRepository
import com.app.dockeep.model.DocumentItem
import com.app.dockeep.utils.Constants.COMPACT_VIEW_KEY
import com.app.dockeep.utils.Constants.CONFIRM_DELETE_KEY
import com.app.dockeep.utils.Constants.CONTENT_PATH_KEY
import com.app.dockeep.utils.Constants.DELETE_KEY
import com.app.dockeep.utils.Constants.FIRST_START_KEY
import com.app.dockeep.utils.Constants.FOLDERS_FIRST_KEY
import com.app.dockeep.utils.Constants.IS_GRID_VIEW_KEY
import com.app.dockeep.utils.Constants.SHOW_HIDDEN_FILES_KEY
import com.app.dockeep.utils.Constants.SORT_ORDER_KEY
import com.app.dockeep.utils.Constants.SORT_TYPE_KEY
import com.app.dockeep.utils.Constants.THEME_KEY
import com.app.dockeep.utils.Helper.extractUris
import com.app.dockeep.utils.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefRepo: DataStoreRepository,
    private val filesRepo: FilesRepository,
    application: Application
) : AndroidViewModel(application) {

   // var files by mutableStateOf(listOf<DocumentItem>())
    private val files = MutableStateFlow(listOf<DocumentItem>())
    val filess = files.asStateFlow()
   // val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    var folders = mutableStateOf(listOf<Pair<String, Uri>>())
    val loading = mutableStateOf(false)
    var launched = false

    private val _theme = MutableStateFlow(ThemeMode.AUTO)
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    val firstStart = mutableStateOf(true)
    val deleteOriginal = mutableStateOf(true)
    val compactView = mutableStateOf(false)
    val showHiddenFiles = mutableStateOf(false)
    val isGridView = mutableStateOf(false)
    val confirmDelete = mutableStateOf(true)
    val foldersFirst = mutableStateOf(true)

    init {
        getAppTheme()
        getFirstStart()
        getDeleteOriginal()
        getCompactView()
        getShowHiddenFiles()
        getGridView()
        getConfirmDelete()
        getFoldersFirst()

        viewModelScope.launch(Dispatchers.IO) {
            getContentPathUri()?.let { uri ->
                loadFiles(uri)
            }
        }
    }

    suspend fun getContentPathUri(): String? = prefRepo.getString(CONTENT_PATH_KEY)

    fun getSortType(): String = runBlocking { prefRepo.getString(SORT_TYPE_KEY) ?: "Name" }

    fun setSortType(type: String) {
        viewModelScope.launch {
            prefRepo.putString(SORT_TYPE_KEY, type)
            sortFiles(type)
        }
    }

    fun getAppTheme() = runBlocking {
        prefRepo.getString(THEME_KEY)?.let { _theme.value = ThemeMode.valueOf(it) }
            ?: ThemeMode.AUTO
    }

    fun setAppTheme(th: ThemeMode) {
        viewModelScope.launch {
            _theme.value = th
            prefRepo.putString(THEME_KEY, th.name)
        }
    }

    fun getFirstStart() =
        runBlocking { firstStart.value = prefRepo.getBool(FIRST_START_KEY) ?: true }

    fun setFirstStart(bool: Boolean) {
        viewModelScope.launch {
            prefRepo.putBool(FIRST_START_KEY, bool)
            firstStart.value = bool
        }
    }

    fun getDeleteOriginal() =
        runBlocking { deleteOriginal.value = prefRepo.getBool(DELETE_KEY) ?: false }

    fun setDeleteOriginal(bool: Boolean) {
        viewModelScope.launch {
            prefRepo.putBool(DELETE_KEY, bool)
            deleteOriginal.value = bool
        }
    }

    fun getCompactView() =
        runBlocking { compactView.value = prefRepo.getBool(COMPACT_VIEW_KEY) ?: false }

    fun setCompactView(bool: Boolean) {
        viewModelScope.launch {
            prefRepo.putBool(COMPACT_VIEW_KEY, bool)
            compactView.value = bool
        }
    }

    fun getShowHiddenFiles() =
        runBlocking { showHiddenFiles.value = prefRepo.getBool(SHOW_HIDDEN_FILES_KEY) ?: false }

    fun setShowHiddenFiles(bool: Boolean) {
        viewModelScope.launch {
            prefRepo.putBool(SHOW_HIDDEN_FILES_KEY, bool)
            showHiddenFiles.value = bool
            getContentPathUri()?.let { loadFiles(it) }
        }
    }

    fun getGridView() =
        runBlocking { isGridView.value = prefRepo.getBool(IS_GRID_VIEW_KEY) ?: false }

    fun setGridView(bool: Boolean) {
        viewModelScope.launch {
            prefRepo.putBool(IS_GRID_VIEW_KEY, bool)
            isGridView.value = bool
        }
    }

    fun getConfirmDelete() =
        runBlocking { confirmDelete.value = prefRepo.getBool(CONFIRM_DELETE_KEY) ?: true }

    fun setConfirmDelete(bool: Boolean) {
        viewModelScope.launch {
            prefRepo.putBool(CONFIRM_DELETE_KEY, bool)
            confirmDelete.value = bool
        }
    }

    fun getFoldersFirst() =
        runBlocking { foldersFirst.value = prefRepo.getBool(FOLDERS_FIRST_KEY) ?: true }

    fun setFoldersFirst(bool: Boolean) {
        viewModelScope.launch {
            prefRepo.putBool(FOLDERS_FIRST_KEY, bool)
            foldersFirst.value = bool
            sortFiles(getSortType())
        }
    }

    fun getSortOrder(): Boolean = runBlocking { prefRepo.getBool(SORT_ORDER_KEY) ?: true }

    fun setSortOrder(isAscending: Boolean) {
        viewModelScope.launch {
            prefRepo.putBool(SORT_ORDER_KEY, isAscending)
            sortFiles(getSortType())
        }
    }

    suspend fun rootExists(): Boolean =
        filesRepo.pathExists(getContentPathUri()?.toUri() ?: Uri.EMPTY)

    private suspend fun resolveFolderUri(folderUri: String): Uri {
        val uriString = folderUri.ifBlank { getContentPathUri() ?: "" }
        return uriString.toUri()
    }

    private suspend fun setLoading(boolean: Boolean) {
        withContext(Dispatchers.Main) {
            loading.value = boolean
        }
    }

    private suspend fun displayMessage(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun setContentPathUri(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) return
        val uri = result.data?.data ?: return
        launched = true
        viewModelScope.launch {
            filesRepo.setRootLocation(uri).toString().let {
                prefRepo.putString(CONTENT_PATH_KEY, it)
                setFirstStart(false)
                loadFiles(getContentPathUri() ?: "")
            }
        }
    }

    suspend fun loadFiles(folderUri: String = "") {
        if (files.value.isEmpty()) setLoading(true)

        val folder = resolveFolderUri(folderUri)

        val fileList = withContext(Dispatchers.IO) {
            filesRepo.listFilesInDirectory(folder).filter {
                if (showHiddenFiles.value) true else !it.name.startsWith(".")
            }
        }

        withContext(Dispatchers.Main) {
            files.value = fileList.toList()
            println("SEARCH ")
            sortFiles(getSortType())
        }

        setLoading(false)

        val folderList = withContext(Dispatchers.IO) {
            filesRepo.listAllDirectories(getContentPathUri()?.toUri() ?: Uri.EMPTY)
        }

        withContext(Dispatchers.Main) {
            folders.value = folderList
        }
    }


    fun loadAndCopyFiles(folderUri: String = "", result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) return

        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val uris = result.data?.extractUris() ?: return@launch
            val folder = resolveFolderUri(folderUri)
            filesRepo.copyFilesToFolder(folder, uris, deleteOriginal.value)
            loadFiles(folder.toString())
        }
    }

    fun searchFiles(query: String, parent: String) {
        viewModelScope.launch {
            if (query.isNotBlank()) {
                val result = withContext(Dispatchers.IO) {
                    filesRepo.searchFiles(
                        query, getContentPathUri()?.toUri() ?: Uri.EMPTY
                    )
                }
                withContext(Dispatchers.Main) {
                    files.value = result.toList()
                }

            } else {
                loadFiles(parent)
            }
        }
    }

    fun createFolder(parent: String = "", name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val folder = resolveFolderUri(parent)
            filesRepo.createDirectory(folder, name)
            loadFiles(folder.toString())
            displayMessage("Created successfully")
        }
    }

    fun deleteFiles(folder: String, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            uris.forEach { doc ->
                filesRepo.deleteDocument(doc)
            }
            loadFiles(folder)
            displayMessage("Deleted successfully")
        }
    }

    fun zipFiles(folder: String, uris: List<Uri>, name:String) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val root = resolveFolderUri(folder)
            filesRepo.createArchive(root, "$name.zip",uris)
            loadFiles(folder)
            displayMessage("Archived successfully")
        }
    }

    fun sortFiles(type: String) {
        val isAscending = getSortOrder()
        val keepFoldersFirst = foldersFirst.value

        val comparator = when (type) {
            "Size" -> compareBy<DocumentItem> { it.size }
            "Date" -> compareBy<DocumentItem> { it.date }
            else -> compareBy<DocumentItem> { it.name.lowercase() }
        }.let { if (isAscending) it else it.reversed() }

        val finalComparator = if (keepFoldersFirst) {
            compareByDescending<DocumentItem> { it.isFolder }.then(comparator)
        } else {
            comparator
        }

        files.value = files.value.sortedWith(finalComparator).toList()
    }

    fun renameFile(folder: String = "", doc: Uri, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            filesRepo.renameDocument(doc, name)
            loadFiles(folder)
            displayMessage("Renamed successfully")
        }
    }

    fun importFiles(
        uris: List<Uri>,
        folderUri: String = "",
        load: Boolean = true,
        remove: Boolean = false,
        src: String = ""
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val folder = resolveFolderUri(folderUri)

            filesRepo.copyFilesToFolder(folder, uris)
            if (remove) uris.forEach { filesRepo.deleteDocument(it) }

            val src = resolveFolderUri(src)

            setLoading(false)

            if (load) loadFiles(folderUri)
            if (remove) loadFiles(src.toString())
        }
    }
}