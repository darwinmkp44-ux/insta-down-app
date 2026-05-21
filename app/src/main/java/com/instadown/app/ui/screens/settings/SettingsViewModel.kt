package com.instadown.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.instadown.app.data.datastore.AppSettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsManager: AppSettingsManager) : ViewModel() {

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _subfolder = MutableStateFlow("InstaDown")
    val subfolder: StateFlow<String> = _subfolder.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.apiKeyFlow.collect {
                _apiKey.value = it
            }
        }
        viewModelScope.launch {
            settingsManager.subfolderFlow.collect {
                _subfolder.value = it
            }
        }
    }

    fun saveSettings(newApiKey: String, newSubfolder: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            settingsManager.saveApiKey(newApiKey)
            settingsManager.saveSubfolder(newSubfolder)
            onComplete()
        }
    }
}

class SettingsViewModelFactory(private val settingsManager: AppSettingsManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
