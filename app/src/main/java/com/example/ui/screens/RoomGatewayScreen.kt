package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.FactCheckApplication
import com.example.data.database.FactCheckRepository
import com.example.data.database.RoomSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class RoomGatewayViewModel(private val repository: FactCheckRepository) : ViewModel() {
    val rooms = repository.allRoomSessions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun createRoom(onRoomCreated: (String) -> Unit) {
        viewModelScope.launch {
            val code = UUID.randomUUID().toString().take(6).uppercase()
            repository.createRoom(code)
            onRoomCreated(code)
        }
    }

    fun joinRoom(code: String, onJoined: (String) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val room = repository.getRoomByCode(code.uppercase())
            if (room != null) {
                onJoined(room.roomCode)
            } else {
                onError()
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
                return RoomGatewayViewModel(
                    (application as FactCheckApplication).factCheckRepository
                ) as T
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomGatewayScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoom: (String) -> Unit,
    viewModel: RoomGatewayViewModel = viewModel(factory = RoomGatewayViewModel.Factory)
) {
    val rooms by viewModel.rooms.collectAsState()
    var joinCode by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Rooms Gateway") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    viewModel.createRoom { code ->
                        onNavigateToRoom(code)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Create New Room", style = MaterialTheme.typography.titleMedium)
            }

            HorizontalDivider()

            OutlinedTextField(
                value = joinCode,
                onValueChange = { 
                    joinCode = it
                    showError = false 
                },
                label = { Text("Enter Room Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            if (showError) {
                Text("Room not found. Please check the code.", color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    if (joinCode.isNotBlank()) {
                        viewModel.joinRoom(
                            code = joinCode,
                            onJoined = { onNavigateToRoom(it) },
                            onError = { showError = true }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Join Room", style = MaterialTheme.typography.titleMedium)
            }

            HorizontalDivider()
            
            Text("Previous Rooms", style = MaterialTheme.typography.titleMedium)
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rooms) { room ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToRoom(room.roomCode) }
                    ) {
                        Text(
                            text = "Room Code: ${room.roomCode}",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
