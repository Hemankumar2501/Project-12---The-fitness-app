package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.auth.AuthScreen
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.Project12Theme
import com.example.ui.theme.ProjectBackground
import com.example.ui.viewmodel.AppViewModel
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MANDATORY: Configure App Check before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            Project12Theme {
                var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
                var isGuestMode by remember { mutableStateOf(false) }

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { auth ->
                        currentUser = auth.currentUser
                    }
                    Firebase.auth.addAuthStateListener(listener)
                    onDispose {
                        Firebase.auth.removeAuthStateListener(listener)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ProjectBackground)
                ) {
                    if (currentUser == null && !isGuestMode) {
                        AuthScreen(
                            onAuthSuccess = {
                                currentUser = Firebase.auth.currentUser
                            },
                            onContinueAsGuest = {
                                isGuestMode = true
                            }
                        )
                    } else {
                        AppNavigation(
                            viewModel = viewModel,
                            onSignOut = {
                                isGuestMode = false
                                currentUser = null
                            }
                        )
                    }
                }
            }
        }
    }
}
