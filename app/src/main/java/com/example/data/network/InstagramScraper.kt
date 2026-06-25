package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class InstagramScraper {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun extractVideoUrl(reelUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // We use a generic desktop/mobile browser User-Agent to trick basic scraping blockers.
            val request = Request.Builder()
                .url(reelUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: ""

            // Look for OpenGraph video tag or Twitter player tag
            val ogVideoRegex = "<meta\\s+property=\"og:video\"\\s+content=\"([^\"]+)\"".toRegex()
            val twitterVideoRegex = "<meta\\s+name=\"twitter:player:stream\"\\s+content=\"([^\"]+)\"".toRegex()
            
            val matchResult = ogVideoRegex.find(html) ?: twitterVideoRegex.find(html)

            if (matchResult != null) {
                val videoUrl = matchResult.groupValues[1]
                Result.success(videoUrl.replace("&amp;", "&"))
            } else {
                Result.failure(Exception("Could not extract video URL. Instagram might require login (Private Reel or Bot Block)."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
