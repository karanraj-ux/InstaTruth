package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.network.VideoDownloader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToWebView: (String) -> Unit,
    onNavigateToRooms: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMissions: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    restoredExtractedUrl: String?,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val resultMessage by viewModel.resultMessage.collectAsStateWithLifecycle()
    val extractedVideoUrl by viewModel.extractedVideoUrl.collectAsStateWithLifecycle()
    val analysisReport by viewModel.analysisReport.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(restoredExtractedUrl) {
        if (restoredExtractedUrl != null) {
            viewModel.setExtractedUrl(restoredExtractedUrl)
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FactCheck AI", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSettings, modifier = Modifier.testTag("settings_button")) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Settings, // Placeholder for app logo
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Analyze Reels for Context",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paste an Instagram Reel URL to crowd-source and AI fact-check it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onNavigateToHistory,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("History")
                }
                OutlinedButton(
                    onClick = onNavigateToRooms,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Community")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onNavigateToMissions,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Missions")
                }
                OutlinedButton(
                    onClick = onNavigateToLeaderboard,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Scoreboard")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = urlInput,
                onValueChange = { viewModel.updateUrlInput(it) },
                label = { Text("Instagram Reel URL") },
                placeholder = { Text("https://www.instagram.com/reel/...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("url_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onNavigateToWebView(urlInput) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("extract_button"),
                enabled = urlInput.isNotBlank() && status == FactCheckStatus.IDLE
            ) {
                Text("Extract Video", style = MaterialTheme.typography.titleMedium)
            }
            
            if (resultMessage != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = resultMessage!!,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (extractedVideoUrl != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val downloader = VideoDownloader(context)
                        downloader.downloadVideo(extractedVideoUrl!!)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("download_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Download MP4 to Gallery")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Choose AI Analysis Mode:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.analyzeWithGemini("Fact Check") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("analyze_factcheck_button"),
                        enabled = status == FactCheckStatus.IDLE || status == FactCheckStatus.COMPLETE || status == FactCheckStatus.ERROR,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Fact Check")
                    }
                    Button(
                        onClick = { viewModel.analyzeWithGemini("Deepfake Detect") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("analyze_deepfake_button"),
                        enabled = status == FactCheckStatus.IDLE || status == FactCheckStatus.COMPLETE || status == FactCheckStatus.ERROR,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Deepfake")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.analyzeWithGemini("Wealth Expose") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("analyze_wealth_button"),
                        enabled = status == FactCheckStatus.IDLE || status == FactCheckStatus.COMPLETE || status == FactCheckStatus.ERROR,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text("Wealth Expose", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Button(
                        onClick = { viewModel.analyzeWithGemini("Roast Mode") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("analyze_roast_button"),
                        enabled = status == FactCheckStatus.IDLE || status == FactCheckStatus.COMPLETE || status == FactCheckStatus.ERROR,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("Roast Mode", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            if (status == FactCheckStatus.ANALYZING || status == FactCheckStatus.SCORING) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (status == FactCheckStatus.ANALYZING) "Connecting to Gemini..." else "Scoring Truthfulness...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (analysisReport != null) {
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Analysis Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = analysisReport!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

