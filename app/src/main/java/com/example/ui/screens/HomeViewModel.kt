package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.FactCheckApplication
import com.example.data.UserPreferencesRepository
import com.example.data.network.Content
import com.example.data.network.GenerateContentRequest
import com.example.data.network.InstagramScraper
import com.example.data.network.Part
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

import com.example.data.database.FactCheckRepository
import com.example.data.database.FactCheckedVideo

enum class FactCheckStatus {
    IDLE, EXTRACTING, ANALYZING, SCORING, COMPLETE, ERROR
}

class HomeViewModel(
    private val repository: UserPreferencesRepository,
    private val dbRepository: FactCheckRepository
) : ViewModel() {
    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    private val _status = MutableStateFlow(FactCheckStatus.IDLE)
    val status: StateFlow<FactCheckStatus> = _status.asStateFlow()

    private val _resultMessage = MutableStateFlow<String?>(null)
    val resultMessage: StateFlow<String?> = _resultMessage.asStateFlow()

    private val _extractedVideoUrl = MutableStateFlow<String?>(null)
    val extractedVideoUrl: StateFlow<String?> = _extractedVideoUrl.asStateFlow()
    
    private val _analysisReport = MutableStateFlow<String?>(null)
    val analysisReport: StateFlow<String?> = _analysisReport.asStateFlow()

    private val _apiKeyStatus = MutableStateFlow("Checking API Key...")
    val apiKeyStatus: StateFlow<String> = _apiKeyStatus.asStateFlow()

    private val _currentModel = MutableStateFlow("gemini-2.5-flash")
    val currentModel: StateFlow<String> = _currentModel.asStateFlow()

    init {
        viewModelScope.launch {
            repository.apiKeyFlow.collect { key ->
                if (key.isNotBlank()) {
                    _apiKeyStatus.value = "Connected \uD83D\uDFE2"
                } else {
                    _apiKeyStatus.value = "Not Connected \uD83D\uDD34 (Add Key)"
                }
            }
        }
        viewModelScope.launch {
            repository.geminiModelFlow.collect { model ->
                _currentModel.value = model
            }
        }
    }

    private val scraper = InstagramScraper()

    fun updateUrlInput(url: String) {
        _urlInput.value = url
        _resultMessage.value = null
        _extractedVideoUrl.value = null
        _analysisReport.value = null
        _status.value = FactCheckStatus.IDLE
    }

    fun setExtractedUrl(url: String) {
        _extractedVideoUrl.value = url
        if (url.isNotBlank()) {
            _resultMessage.value = "✅ Extracted Video via Browser!"
            _status.value = FactCheckStatus.IDLE // Or allow them to start analysis
        }
    }

    fun analyzeWithGemini(promptType: String) {
        if (_extractedVideoUrl.value.isNullOrBlank()) return

        _status.value = FactCheckStatus.ANALYZING
        _analysisReport.value = null

        viewModelScope.launch {
            try {
                val apiKey = repository.apiKeyFlow.first()
                if (apiKey.isBlank()) {
                    _status.value = FactCheckStatus.ERROR
                    _resultMessage.value = "❌ Error: Gemini API Key is missing. Please add it in Settings."
                    return@launch
                }

                _resultMessage.value = "Downloading video for analysis..."

                val videoBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val req = okhttp3.Request.Builder().url(_extractedVideoUrl.value!!).build()
                    val res = RetrofitClient.okHttpClient.newCall(req).execute()
                    if (!res.isSuccessful) throw Exception("Failed to download video")
                    res.body?.bytes() ?: throw Exception("Empty video body")
                }

                _resultMessage.value = "Uploading to Gemini..."
                
                val reqBody = videoBytes.toRequestBody("video/mp4".toMediaType())
                val uploadRes = RetrofitClient.geminiService.uploadFile(apiKey, reqBody)
                var fileInfo = uploadRes.file

                _resultMessage.value = "Processing video on Gemini..."
                while (fileInfo.state == "PROCESSING") {
                    kotlinx.coroutines.delay(3000)
                    fileInfo = RetrofitClient.geminiService.getFile(fileInfo.name, apiKey)
                }

                if (fileInfo.state == "FAILED") {
                    throw Exception("Gemini failed to process the video.")
                }

                _status.value = FactCheckStatus.SCORING
                _resultMessage.value = "Analyzing video..."

                val prompt = when (promptType) {
                    "Deepfake Detect" -> """
                        You are an elite AI Forensic Analyst. Analyze this video for visual or audio anomalies, deepfake artifacts, unnatural movements, mismatched lip-sync, or AI-generated voices.
                        Structure your response precisely:
                        **Authenticity Score**: [0-100]
                        **Visual Artifacts**: [List any visual glitches, unnatural lighting, or deepfake signs]
                        **Audio Artifacts**: [List any robotic tones, mismatched audio, or AI voice signs]
                        **Final Conclusion**: [Is this real or AI generated?]
                    """.trimIndent()
                    "Wealth Expose" -> """
                        You are a financial reality checker and detective. The subject in this video might be flexing wealth, luxury items, or lifestyle. Analyze the video critically to determine if the flex is genuine or fake (e.g., rented cars, fake designer items, green screens).
                        Structure your response precisely:
                        **Reality Score**: [0-100]
                        **Flex Breakdown**: [What are they flexing?]
                        **Expose Details**: [Evidence of it being fake or rented, visual inconsistencies]
                        **Final Verdict**: [Genuine or Fake Flex?]
                    """.trimIndent()
                    "Roast Mode" -> """
                        You are a savage stand-up comedian. Roast the subject of this video in a fun, punchy, and highly observant way. Find the most cringeworthy, absurd, or try-hard elements and highlight them. Keep it witty and entertaining.
                        Structure your response precisely:
                        **Roast Score**: [0-100 (100 being completely roasted)]
                        **The Setup**: [What is the subject trying to do?]
                        **The Breakdown**: [Point out the funniest/cringiest details]
                        **Final Burn**: [A savage one-liner to end it]
                    """.trimIndent()
                    else -> """
                        You are a professional Fact Checking Agent. Analyze this video for factual accuracy. Identify any claims made, evaluate their truthfulness based on logical consistency and general knowledge.
                        Structure your response precisely:
                        **Truth Score**: [0-100]
                        **Claim Analysis**: [Identify the main claims and assess their validity]
                        **Missing Context**: [What context is left out of the video?]
                        **Final Verdict**: [True, False, Misleading, or Satire]
                    """.trimIndent()
                }

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(
                        Part(fileData = com.example.data.network.FileData("video/mp4", fileInfo.uri)),
                        Part(text = prompt)
                    )))
                )

                val response = RetrofitClient.geminiService.generateContent(_currentModel.value, apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                
                val reportText = text ?: "Analysis returned no results."
                _analysisReport.value = reportText
                
                dbRepository.insertVideo(
                    FactCheckedVideo(
                        url = _urlInput.value,
                        extractedVideoUrl = _extractedVideoUrl.value!!,
                        analysisReport = reportText
                    )
                )

                _status.value = FactCheckStatus.COMPLETE
                _resultMessage.value = "✅ Analysis Complete & Saved."

            } catch (e: Exception) {
                _status.value = FactCheckStatus.ERROR
                _resultMessage.value = "❌ AI Analysis failed:\n${e.localizedMessage}"
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                return HomeViewModel(
                    (application as FactCheckApplication).userPreferencesRepository,
                    application.factCheckRepository
                ) as T
            }
        }
    }
}
