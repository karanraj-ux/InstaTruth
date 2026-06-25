package com.example.data.network

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

class VideoDownloader(private val context: Context) {
    fun downloadVideo(videoUrl: String, fileName: String = "FactCheck_Reel_${System.currentTimeMillis()}.mp4") {
        val request = DownloadManager.Request(Uri.parse(videoUrl))
            .setTitle("Downloading Instagram Reel")
            .setDescription("Saving video for offline storage or forensic analysis")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }
}
