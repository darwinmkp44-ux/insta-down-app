package com.instadown.app.data.repository

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.instadown.app.data.database.DownloadDao
import com.instadown.app.data.model.DownloadEntity
import com.instadown.app.service.DownloadWorker
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository coordinating local Room database operations and scheduling background downloads with WorkManager.
 */
class DownloadRepository(
    private val context: Context,
    private val downloadDao: DownloadDao
) {

    // Emits the list of all downloads ordered by timestamp descending
    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloadsFlow()

    /**
     * Gets a single download by its unique ID.
     */
    suspend fun getDownloadById(id: String): DownloadEntity? {
        return downloadDao.getDownloadById(id)
    }

    /**
     * Deletes a download log from the database.
     */
    suspend fun deleteDownload(download: DownloadEntity) {
        downloadDao.deleteDownload(download)
    }

    /**
     * Deletes a download log by ID.
     */
    suspend fun deleteDownloadById(id: String) {
        downloadDao.deleteDownloadById(id)
    }

    /**
     * Enqueues a background video download using WorkManager.
     * The network call to FastSaver API and actual byte stream are executed inside the Worker.
     */
    fun enqueueDownload(url: String) {
        // Generate a random temporary ID for tracking before API responds with real ID
        val tempId = "temp_${UUID.randomUUID()}"

        // Input data for the Worker
        val inputData = Data.Builder()
            .putString(DownloadWorker.KEY_URL, url)
            .putString(DownloadWorker.KEY_TEMP_ID, tempId)
            .build()

        // Create the WorkRequest
        val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(inputData)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST) // Request immediate execution
            .build()

        // Enqueue the work with a unique name based on the URL to prevent double downloading the exact same link
        val uniqueWorkName = "download_${url.hashCode()}"
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP, // Keep existing download if already running
            downloadWorkRequest
        )
    }
}
