package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WebViewDownloaderScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        var initialUrl = ""
        if (intent?.action == android.content.Intent.ACTION_SEND) {
            if ("text/plain" == intent.type) {
                intent.getStringExtra(android.content.Intent.EXTRA_TEXT)?.let { sharedText ->
                    // Extract URL from shared text
                    val urlRegex = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\((?:[^\\s()<>]+|\\([^\\s()<>]+\\))*\\))+(?:\\((?:[^\\s()<>]+|\\([^\\s()<>]+\\))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))".toRegex()
                    val matchResult = urlRegex.find(sharedText)
                    if (matchResult != null) {
                        initialUrl = matchResult.value
                    } else {
                        initialUrl = sharedText
                    }
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") { backStackEntry ->
                            val extractedUrl = backStackEntry.savedStateHandle.get<String>("extracted_video_url")

                            HomeScreen(
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToWebView = { url -> 
                                    val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
                                    navController.navigate("webview/$encodedUrl")
                                },
                                onNavigateToRooms = { navController.navigate("rooms_gateway") },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToMissions = { navController.navigate("missions") },
                                onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                                restoredExtractedUrl = extractedUrl,
                                initialSharedUrl = initialUrl
                            )
                        }
                        composable("rooms_gateway") {
                            com.example.ui.screens.RoomGatewayScreen(
                                onNavigateBack = { navController.navigateUp() },
                                onNavigateToRoom = { roomId -> navController.navigate("chat_room/$roomId") }
                            )
                        }
                        composable(
                            "chat_room/{roomId}",
                            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                            com.example.ui.screens.ChatRoomScreen(
                                roomId = roomId,
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }
                        composable("history") {
                            com.example.ui.screens.HistoryScreen(
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }
                        composable("missions") {
                            com.example.ui.screens.MissionsScreen(
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }
                        composable("leaderboard") {
                            com.example.ui.screens.LeaderboardScreen(
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.navigateUp() }
                            )
                        }
                        composable(
                            "webview/{url}",
                            arguments = listOf(navArgument("url") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val url = backStackEntry.arguments?.getString("url") ?: ""
                            val decodedUrl = java.net.URLDecoder.decode(url, "UTF-8")
                            
                            WebViewDownloaderScreen(
                                instagramUrl = decodedUrl,
                                onNavigateBack = { navController.navigateUp() },
                                onVideoExtracted = { downloadUrl ->
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("extracted_video_url", downloadUrl)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

