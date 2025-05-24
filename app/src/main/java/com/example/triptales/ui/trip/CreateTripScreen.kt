package com.example.triptales.ui.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.triptales.data.model.Trip
import com.example.triptales.data.model.User
import com.example.triptales.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// Stati per la creazione di un viaggio
sealed class CreateTripState {
    object Idle : CreateTripState()
    object Loading : CreateTripState()
    object Success : CreateTripState()
    data class Error(val message: String) : CreateTripState()
}

// ViewModel per la creazione di un viaggio
class CreateTripViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _createTripState = MutableStateFlow<CreateTripState>(CreateTripState.Idle)
    val createTripState: StateFlow<CreateTripState> = _createTripState

    fun createTrip(name: String, description: String, startDate: String, endDate: String) {
        if (name.isBlank() || description.isBlank() || startDate.isBlank() || endDate.isBlank()) {
            _createTripState.value = CreateTripState.Error("All fields are required")
            return
        }

        _createTripState.value = CreateTripState.Loading

        viewModelScope.launch {
            // Crea un oggetto Trip temporaneo per la creazione
            val newTrip = Trip(
                id = 0, // L'ID sarà assegnato dal server
                name = name,
                description = description,
                startDate = startDate,
                endDate = endDate,
                createdBy = User(0, "", "", null), // Placeholder, il server gestirà l'utente corrente
                members = emptyList()
            )

            tripRepository.createTrip(newTrip).fold(
                onSuccess = {
                    _createTripState.value = CreateTripState.Success
                },
                onFailure = { e ->
                    _createTripState.value = CreateTripState.Error(e.message ?: "Failed to create trip")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(navController: NavHostController) {
    val viewModel: CreateTripViewModel = koinViewModel()  // 🔧 CAMBIATO DA viewModel() A koinViewModel()
    val createTripState by viewModel.createTripState.collectAsState()

    var tripName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Trip") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Plan your next adventure",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = tripName,
                onValueChange = { tripName = it },
                label = { Text("Trip Name") },
                placeholder = { Text("e.g., Summer Vacation 2024") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Describe your trip...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 4
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Start Date") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("End Date") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.createTrip(tripName, description, startDate, endDate)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = createTripState !is CreateTripState.Loading
            ) {
                if (createTripState is CreateTripState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Text("Create Trip")
                }
            }

            // Handle create trip state
            when (val state = createTripState) {
                is CreateTripState.Success -> {
                    LaunchedEffect(Unit) {
                        navController.navigate("trips") {
                            popUpTo("create_trip") { inclusive = true }
                        }
                    }
                }
                is CreateTripState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                else -> { /* idle or loading state */ }
            }
        }
    }
}