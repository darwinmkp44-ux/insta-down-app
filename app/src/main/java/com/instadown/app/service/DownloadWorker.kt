package com.instadown.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.instadown.app.R
import com.instadown.app.data.api.FastSaverApiService
import com.instadown.app.data.database.AppDatabase
import com.instadown.app.data.datastore.AppSettingsManager
import com.instadown.app.data.model.DownloadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Worker executing the FastSaver API fetch and file downloading in background.
 * Implements Android Foreground Service (via WorkManager) to show a persistent progress notification.
 */
class DownloadWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_URL = "key_url"
        const val KEY_TEMP_ID = "key_temp_id"
        
        private const val NOTIFICATION_CHANNEL_ID = "instadown_downloads"
        private const val NOTIFICATION_CHANNEL_NAME = "InstaDown Downloads"
        private const val NOTIFICATION_ID = 101
    }

    private val db = AppDatabase.getDatabase(appContext)
    private val dao = db.downloadDao()
    private val settings = AppSettingsManager(appContext)
    
    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val url = inputData.getString(KEY_URL) ?: return@withContext Result.failure()
        val tempId = inputData.getString(KEY_TEMP_ID) ?: return@withContext Result.failure()

        // Create the notification channel
        createNotificationChannel()

        // Show starting notification
        setForeground(getForegroundInfo("Iniciando download..."))

        // Read Settings
        val apiKey = settings.apiKeyFlow.first()
        val subfolder = settings.subfolderFlow.first()

        if (apiKey.isEmpty()) {
            updateTempRowToFailed(tempId, url, "API Key não configurada. Vá em configurações.")
            showFinishedNotification("Download falhou", "FastSaver API Key não configurada.")
            return@withContext Result.failure()
        }

        var downloadId = tempId
        try {
            // 1. Fetch metadata from FastSaver API
            val apiService = FastSaverApiService.create()
            val apiResponse = apiService.fetchMedia(apiKey, url)

            if (!apiResponse.ok || apiResponse.downloadUrl.isNullOrEmpty()) {
                updateTempRowToFailed(tempId, url, "Erro da API FastSaver: ok=false ou link de download vazio.")
                showFinishedNotification("Download falhou", "Não foi possível recuperar o vídeo do post.")
                return@withContext Result.failure()
            }

            // Real ID from Instagram post metadata
            val realId = apiResponse.id ?: UUID().toString()
            downloadId = realId
            val downloadUrl = apiResponse.downloadUrl
            val thumbnailUrl = apiResponse.thumbnailUrl ?: ""
            val caption = apiResponse.caption ?: "Instagram Video"
            val duration = apiResponse.duration ?: 0

            // 2. Prepare database: delete temp row and insert official row with DOWNLOADING state
            dao.deleteDownloadById(tempId)
            val downloadEntity = DownloadEntity(
                id = realId,
                url = url,
                caption = caption,
                filePath = null,
                thumbnailUrl = thumbnailUrl,
                timestamp = System.currentTimeMillis(),
                duration = duration,
                fileSize = 0L,
                status = "DOWNLOADING",
                progress = 0
            )
            dao.insertDownload(downloadEntity)

            // 3. Initiate actual HTTP file download
            val client = OkHttpClient()
            val request = Request.Builder().url(downloadUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                dao.updateProgress(realId, 0, "FAILED")
                showFinishedNotification("Download falhou", "Erro HTTP ao baixar o arquivo de mídia.")
                return@withContext Result.failure()
            }

            val body = response.body
            if (body == null) {
                dao.updateProgress(realId, 0, "FAILED")
                showFinishedNotification("Download falhou", "Corpo do arquivo vazio.")
                return@withContext Result.failure()
            }

            val contentLength = body.contentLength()
            val inputStream: InputStream = body.byteStream()

            // 4. Stream bytes to file in Downloads/subfolder
            val filename = "instadown_${realId}.mp4"
            val outputStreamInfo = createDownloadOutputStream(subfolder, filename)

            if (outputStreamInfo == null) {
                dao.updateProgress(realId, 0, "FAILED")
                showFinishedNotification("Download falhou", "Não foi possível criar o arquivo de saída.")
                return@withContext Result.failure()
            }

            val (outputStream, savedUri, localPath) = outputStreamInfo
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L
            var lastProgressUpdate = 0L
            var lastProgressPercent = 0

            outputStream.use { out ->
                inputStream.use { input ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        out.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        // Throttle progress updates (every 500ms or when percentage changes)
                        val currentTime = System.currentTimeMillis()
                        val progressPercent = if (contentLength > 0) {
                            ((totalBytesRead * 100) / contentLength).toInt()
                        } else 0
                        
                        if (progressPercent > lastProgressPercent && (currentTime - lastProgressUpdate > 300 || progressPercent == 100)) {
                            lastProgressPercent = progressPercent
                            lastProgressUpdate = currentTime
                            
                            // Update Room DB progress
                            dao.updateProgress(realId, progressPercent, "DOWNLOADING")
                            
                            // Update system progress notification
                            setForeground(getForegroundInfo("Baixando: $progressPercent%", progressPercent))
                        }
                    }
                }
            }

            // 5. Download Completed Successfully
            val savedFilePath = localPath ?: savedUri.toString()
            dao.updateFilePath(realId, savedFilePath, "COMPLETED")
            dao.updateProgress(realId, 100, "COMPLETED")

            showFinishedNotification("Download concluído", caption)
            return@withContext Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            // Make sure we mark the download as failed
            dao.updateProgress(downloadId, 0, "FAILED")
            showFinishedNotification("Download falhou", e.localizedMessage ?: "Erro inesperado.")
            return@withContext Result.failure()
        }
    }

    private suspend fun updateTempRowToFailed(tempId: String, url: String, errorMessage: String) {
        val tempEntity = DownloadEntity(
            id = tempId,
            url = url,
            caption = errorMessage,
            filePath = null,
            thumbnailUrl = null,
            timestamp = System.currentTimeMillis(),
            duration = 0,
            fileSize = 0L,
            status = "FAILED",
            progress = 0
        )
        dao.insertDownload(tempEntity)
    }

    // Helper to generate a unique ID
    private fun UUID(): java.util.UUID = java.util.UUID.randomUUID()

    /**
     * Creates an output stream targeting the standard Downloads directory under a custom subfolder.
     * Uses MediaStore for Android 10+ (does not require storage permissions).
     * Falls back to legacy File API for Android 9 and below.
     * Returns a Triple of (OutputStream, URI, LocalPathString).
     */
    private fun createDownloadOutputStream(subfolder: String, filename: String): Triple<OutputStream, Uri, String?>? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Modern MediaStore API (Android 10+)
                val resolver = appContext.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, filename)
                    put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
                    put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$subfolder")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues) ?: return null
                val outputStream = resolver.openOutputStream(uri) ?: return null
                Triple(outputStream, uri, null)
            } else {
                // Legacy File API (Android 9 and below)
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadDir, subfolder)
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                val file = File(targetDir, filename)
                val outputStream = FileOutputStream(file)
                val uri = Uri.fromFile(file)
                Triple(outputStream, uri, file.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return getForegroundInfo("Iniciando download...")
    }

    private fun getForegroundInfo(message: String, progress: Int = 0): ForegroundInfo {
        val notification = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("InstaDown")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(100, progress, progress == 0) // Indeterminate progress initially
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun showFinishedNotification(title: String, text: String) {
        val notification = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificações de progresso de downloads do InstaDown"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
