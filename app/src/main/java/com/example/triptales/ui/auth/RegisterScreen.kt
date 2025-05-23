package com.example.triptales.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.triptales.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import retrofit2.HttpException
import org.json.JSONObject

// Stati di registrazione dell'applicazione
sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

// ViewModel per la gestione dello stato di registrazione
class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun register(username: String, email: String, password: String, confirmPassword: String) {
        // Validazione lato client migliorata
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _registerState.value = RegisterState.Error("All fields are required")
            return
        }

        if (username.length < 3) {
            _registerState.value = RegisterState.Error("Username must be at least 3 characters long")
            return
        }

        if (password.length < 8) {
            _registerState.value = RegisterState.Error("Password must be at least 8 characters long")
            return
        }

        if (password != confirmPassword) {
            _registerState.value = RegisterState.Error("Passwords do not match")
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            android.util.Log.d("RegisterViewModel", "Starting registration for username: $username, email: $email")

            authRepository.register(username, email, password).fold(
                onSuccess = {
                    android.util.Log.d("RegisterViewModel", "Registration successful")
                    _registerState.value = RegisterState.Success
                },
                onFailure = { e ->
                    android.util.Log.e("RegisterViewModel", "Registration failed", e)

                    val errorMessage = when (e) {
                        is HttpException -> {
                            try {
                                // Prova a parsare il messaggio di errore dal server
                                val errorBody = e.response()?.errorBody()?.string()
                                if (errorBody != null) {
                                    val json = JSONObject(errorBody)
                                    val errors = mutableListOf<String>()

                                    // Estrai tutti gli errori
                                    if (json.has("username")) {
                                        val usernameErrors = json.getJSONArray("username")
                                        for (i in 0 until usernameErrors.length()) {
                                            errors.add("Username: ${usernameErrors.getString(i)}")
                                        }
                                    }

                                    if (json.has("email")) {
                                        val emailErrors = json.getJSONArray("email")
                                        for (i in 0 until emailErrors.length()) {
                                            errors.add("Email: ${emailErrors.getString(i)}")
                                        }
                                    }

                                    if (json.has("password")) {
                                        val passwordErrors = json.getJSONArray("password")
                                        for (i in 0 until passwordErrors.length()) {
                                            errors.add("Password: ${passwordErrors.getString(i)}")
                                        }
                                    }

                                    if (errors.isNotEmpty()) {
                                        errors.joinToString("\n")
                                    } else {
                                        "Registration failed: ${e.message}"
                                    }
                                } else {
                                    "Registration failed: HTTP ${e.code()}"
                                }
                            } catch (parseException: Exception) {
                                "Registration failed: ${e.message}"
                            }
                        }
                        else -> {
                            when {
                                e.message?.contains("network") == true -> "Network error: Check your connection"
                                e.message?.contains("timeout") == true -> "Request timeout: Try again"
                                else -> "Registration failed: ${e.message}"
                            }
                        }
                    }

                    _registerState.value = RegisterState.Error(errorMessage)
                }
            )
        }
    }
}

@Composable
fun RegisterScreen(navController: NavHostController) {
    val viewModel: RegisterViewModel = koinViewModel()
    val registerState by viewModel.registerState.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            placeholder = { Text("Minimum 3 characters") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            isError = username.isNotEmpty() && username.length < 3,
            supportingText = if (username.isNotEmpty() && username.length < 3) {
                { Text("Username must be at least 3 characters", color = MaterialTheme.colorScheme.error) }
            } else null
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("your@email.com") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("Minimum 8 characters") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            isError = password.isNotEmpty() && password.length < 8,
            supportingText = if (password.isNotEmpty() && password.length < 8) {
                { Text("Password must be at least 8 characters", color = MaterialTheme.colorScheme.error) }
            } else null
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            isError = confirmPassword.isNotEmpty() && password != confirmPassword,
            supportingText = if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                { Text("Passwords do not match", color = MaterialTheme.colorScheme.error) }
            } else null
        )

        Button(
            onClick = { viewModel.register(username, email, password, confirmPassword) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            enabled = registerState !is RegisterState.Loading &&
                    username.length >= 3 &&
                    password.length >= 8 &&
                    email.isNotBlank() &&
                    password == confirmPassword
        ) {
            if (registerState is RegisterState.Loading) {
                CircularProgressIndicator()
            } else {
                Text("Register")
            }
        }

        TextButton(onClick = { navController.navigateUp() }) {
            Text("Already have an account? Sign in")
        }

        // Handle register state
        when (registerState) {
            is RegisterState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
            is RegisterState.Success -> {
                LaunchedEffect(Unit) {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            }
            is RegisterState.Error -> {
                Text(
                    text = (registerState as RegisterState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            else -> { /* Idle state */ }
        }
    }
}