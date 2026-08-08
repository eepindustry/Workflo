package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.GradientButton

@Composable
fun LoginScreen(
    loginError: String?,
    onGoogleLogin: (String, String) -> Unit,
    clearError: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var termsAccepted by remember { mutableStateOf(false) }
    var showMockGoogleDialog by remember { mutableStateOf(false) }
    var mockEmail by remember { mutableStateOf("") }
    var showTermsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.water_app_bg_1784706944679),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp).align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_water_logo_1784671474407),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = {
                    if (termsAccepted) {
                        showMockGoogleDialog = true
                        
                        
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                enabled = termsAccepted
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("G", fontWeight = FontWeight.Bold, color = Color.Red)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            GradientButton(
                onClick = { /* Also triggers continue if logged in */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("CONTINUE", color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF4FC3F7),
                        uncheckedColor = Color.LightGray
                    )
                )
                Text(
                    text = "By clicking on tick box so you are agreeing to our",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Terms & Conditions\nPrivacy Policy",
                color = Color.White,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { showTermsDialog = true }
            )
        }
        
        if (loginError != null) {
            AlertDialog(
                onDismissRequest = clearError,
                title = { Text("Login Error") },
                text = { Text(loginError) },
                confirmButton = {
                    TextButton(onClick = clearError) { Text("OK") }
                }
            )
        }

        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                containerColor = Color(0xFF1B263B),
                title = { Text("Terms & Privacy Policy", color = Color.White) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Privacy Policy for Water App", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Welcome to Water Family. By using this application, you agree to our policies. " +
                            "We collect basic profile information (such as your Google ID and name) to provide a seamless " +
                            "leaderboard and competitive experience. We do not sell your personal data. We track your gameplay progress, " +
                            "levels, and earned WCoins securely. Payment redemptions require your mobile number, which is processed manually via WhatsApp.",
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Terms & Conditions", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "1. Fair Play: You must not use VPNs, DNS changers, Auto-clickers, or Split-screen features to gain an unfair advantage. Doing so may result in an immediate ban.\n" +
                            "2. Rewards: WCoins can be redeemed subject to review. We reserve the right to withhold rewards if fraudulent activity is suspected.\n" +
                            "3. 1 WCoin = 1 Rupee. Redemption requests take 24-48 hours to process.\n" +
                            "4. Ads: We use third-party ad networks (AdMob & Unity Ads) which may collect anonymized data as per their respective privacy policies.",
                            color = Color.LightGray
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTermsDialog = false }) {
                        Text("Close", color = Color.White)
                    }
                }
            )
        }

        if (showMockGoogleDialog) {
            AlertDialog(
                onDismissRequest = { showMockGoogleDialog = false },
                title = { Text("Choose an Account") },
                text = {
                    Column {
                        Text("Select a Google account to continue with.")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = mockEmail,
                            onValueChange = { mockEmail = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (mockEmail.isNotBlank()) {
                            showMockGoogleDialog = false
                            val rawAndroidId = android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)
                            val safeAndroidId = if (rawAndroidId.isNullOrBlank()) "unknown_device" else rawAndroidId
                            onGoogleLogin(safeAndroidId, mockEmail)
                        }
                    }) {
                        Text("Continue")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMockGoogleDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
