package com.instadown.app

import android.app.Application
import androidx.work.Configuration
import com.instadown.app.data.database.AppDatabase
import com.instadown.app.data.datastore.AppSettingsManager
import com.instadown.app.data.repository.DownloadRepository

/**
 * Main Application class establishing the Singletons for Database and Repository.
 */
class InstaDownApp : Application(), Configuration.Provider {

    // Lazy initialization of database
    val database by lazy { AppDatabase.getDatabase(this) }

    // Lazy initialization of AppSettingsManager
    val settingsManager by lazy { AppSettingsManager(this) }

    // Lazy initialization of download repository
    val repository by lazy { DownloadRepository(this, database.downloadDao()) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
    }
}


