package com.example.triptales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.triptales.data.repository.AuthRepository
import com.example.triptales.ui.auth.LoginScreen
import com.example.triptales.ui.auth.RegisterScreen
import com.example.triptales.ui.map.TripMapScreen
import com.example.triptales.ui.post.CreatePostScreen
import com.example.triptales.ui.post.PostDetailScreen
import com.example.triptales.ui.profile.ProfileScreen
import com.example.triptales.ui.theme.TripTalesTheme
import com.example.triptales.ui.trip.CreateTripScreen
import com.example.triptales.ui.trip.TripDetailScreen
import com.example.triptales.ui.trip.TripsScreen
import org.koin.androidx.compose.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TripTalesTheme {
                TripTalesAppContent()
            }
        }
    }
}

@Composable
fun TripTalesAppContent() {
    val navController = rememberNavController()
    val authRepository: AuthRepository = get()

    // Start destination based on login status
    val startDestination = if (authRepository.isLoggedIn()) {
        "trips"
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("trips") {
            TripsScreen(navController)
        }
        composable("create_trip") {
            CreateTripScreen(navController)
        }
        composable("trip/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toIntOrNull() ?: 0
            TripDetailScreen(navController, tripId)
        }
        composable("create_post/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toIntOrNull() ?: 0
            CreatePostScreen(navController, tripId)
        }
        composable("post/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: 0
            PostDetailScreen(navController, postId)
        }
        composable("profile") {
            ProfileScreen(navController)
        }
        composable("map/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId")?.toIntOrNull() ?: 0
            TripMapScreen(navController, tripId)
        }
    }
}