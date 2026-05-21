package com.instadown.app.ui.screens.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.instadown.app.data.model.DownloadEntity
import com.instadown.app.data.repository.DownloadRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class GalleryViewModel(private val repository: DownloadRepository) : ViewModel() {

    // Expose downloads reactive flow as state
    val downloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Deletes the download log from DB and attempts to delete the physical file from disk.
     */
    fun deleteDownload(download: DownloadEntity) {
        viewModelScope.launch {
            repository.deleteDownload(download)
            
            // If the path represents a physical local file path (legacy), delete it.
            // On modern MediaStore content Uri, it requires Resolving content which is done via resolver
            // or simply the file is deleted. We attempt normal file deletion or print log.
            download.filePath?.let { path ->
                if (!path.startsWith("content://")) {
                    try {
                        val file = File(path)
                        if (file.exists()) {
                            file.delete()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}

class GalleryViewModelFactory(private val repository: DownloadRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GalleryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
