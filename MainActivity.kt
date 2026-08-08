package com.example

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.example.ui.navigation.Screen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var isVpnActive by mutableStateOf(false)
    private var isDnsActive by mutableStateOf(false)

    private fun checkNetworkSecurity() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
        val linkProps = connectivityManager.getLinkProperties(activeNetwork)
        
        isVpnActive = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            isDnsActive = linkProps?.privateDnsServerName != null
        }
    }

    override fun onResume() {
        super.onResume()
        checkNetworkSecurity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        MobileAds.initialize(this) {}
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val isConnected by viewModel.isConnected.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()
                val loginError by viewModel.loginError.collectAsState()
                val currentLanguage by viewModel.currentLanguage.collectAsState()
                val contestants by viewModel.contestants.collectAsState()
                val contestStartTime by viewModel.contestStartTime.collectAsState()
                
                // Real-time check
                androidx.compose.runtime.LaunchedEffect(isConnected) {
                    checkNetworkSecurity()
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route
                    ) {
                        composable(Screen.Splash.route) {
                            SplashScreen(
                                isConnected = isConnected,
                                onFinish = {
                                    if (currentUser != null) {
                                        if (currentUser!!.name.isBlank()) {
                                            navController.navigate(Screen.Welcome.route) {
                                                popUpTo(Screen.Splash.route) { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate(Screen.Dashboard.route) {
                                                popUpTo(Screen.Splash.route) { inclusive = true }
                                            }
                                        }
                                    } else {
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        
                        composable(Screen.Login.route) {
                            // If currentUser changes, react
                            if (currentUser != null) {
                                androidx.compose.runtime.LaunchedEffect(currentUser) {
                                    if (currentUser!!.name.isBlank()) {
                                        navController.navigate(Screen.Welcome.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Login.route) { inclusive = true }
                                        }
                                    }
                                }
                            }
                            
                            LoginScreen(
                                loginError = loginError,
                                onGoogleLogin = { id, email -> viewModel.mockGoogleLogin(id, email) },
                                clearError = { viewModel.clearLoginError() }
                            )
                        }
                        
                        composable(Screen.Welcome.route) {
                            WelcomeScreen(
                                loginError = loginError,
                                onConfirm = { name -> viewModel.registerName(name) },
                                clearError = { viewModel.clearLoginError() }
                            )
                            
                            if (currentUser?.name?.isNotBlank() == true) {
                                androidx.compose.runtime.LaunchedEffect(currentUser) {
                                    navController.navigate(Screen.Dashboard.route) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                }
                            }
                        }
                        
                        composable(Screen.Dashboard.route) {
                            if (currentUser != null && currentUser!!.name.isNotBlank()) {
                                DashboardScreen(
                                    user = currentUser!!,
                                    language = currentLanguage,
                                    contestants = contestants,
                                    contestStartTime = contestStartTime,
                                    onLanguageChange = { viewModel.setLanguage(it) },
                                    onPlayQuiz = {
                                        navController.navigate(Screen.Quiz.route)
                                    }
                                )
                            }
                        }
                        
                        composable(Screen.Quiz.route) {
                            QuizScreen(
                                user = currentUser,
                                language = currentLanguage,
                                onGameEnd = { passed ->
                                    viewModel.updateScore(passed)
                                    navController.popBackStack()
                                },
                                onUseExtraLife = { onSuccess ->
                                    viewModel.useExtraLife(onSuccess)
                                }
                            )
                        }
                    }

                    // No Internet Overlay
                    if (!isConnected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.9f))
                                .clickable(
                                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Please check your internet connection",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    } else if (isVpnActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.9f))
                                .clickable(
                                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Please turn off your VPN",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    } else if (isDnsActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.9f))
                                .clickable(
                                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Please turn off your DNS",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    } else if (this@MainActivity.isInMultiWindowMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.9f))
                                .clickable(
                                    interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) { },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Split screen is not allowed. Please use full screen.",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
