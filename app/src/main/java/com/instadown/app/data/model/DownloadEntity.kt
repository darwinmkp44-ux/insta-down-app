package com.instadown.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity to store details of downloads (completed, failed, or in progress).
 */
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,           // Unique ID (from Instagram post or generated uuid)
    val url: String,                      // Instagram post URL
    val caption: String,                  // Post caption or custom title
    val filePath: String?,                // Local absolute path to the saved video file
    val thumbnailUrl: String?,            // HTTP URL or local cached path for the thumbnail
    val timestamp: Long,                  // Epoch timestamp in milliseconds
    val duration: Int,                    // Video duration in seconds (0 if unknown/image)
    val fileSize: Long,                   // Video file size in bytes
    val status: String,                   // Status: "PENDING", "DOWNLOADING", "COMPLETED", "FAILED"
    val progress: Int                     // Percentage: 0 to 100
)
