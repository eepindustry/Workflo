package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.components.GlassCard
import com.example.ui.components.GradientButton

import com.example.ui.utils.Language
import com.example.ui.utils.getText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(user: User, language: Language) {
    var showRedeemPopup by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = getText("Wallet", language),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = getText("Winning Prize", language),
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${user.wcoins} WCoins",
                    color = Color(0xFF4FC3F7),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "1 WCoin = 1 Rupee",
            color = Color.Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        GradientButton(
            onClick = { showRedeemPopup = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            enabled = user.wcoins > 0
        ) {
            Text(
                text = if (user.wcoins > 0) getText("Redeem WCoins", language) else "Insufficient Balance",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showRedeemPopup) {
        RedeemPopup(
            user = user,
            language = language,
            onDismiss = { showRedeemPopup = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemPopup(user: User, language: Language, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var mobileNumber by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B263B),
        title = {
            Text(
                text = getText("Redeem WCoins", language),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Name",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = user.name,
                    onValueChange = {},
                    enabled = false, // Automatically fetched, cannot change
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.White,
                        disabledBorderColor = Color.Gray,
                        disabledContainerColor = Color.DarkGray.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Enter your mobile number for payment",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    placeholder = { Text("Mobile Number", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4FC3F7),
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Your amount will be in your account in 24 hours to 48 hours",
                    color = Color(0xFFFFB74D), // Warning orange
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val message = "I want to redeem my amount (${user.wcoins})\n\nName: ${user.name}\nNumber: $mobileNumber"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://wa.me/918376854878?text=" + Uri.encode(message))
                    context.startActivity(intent)
                    onDismiss()
                },
                enabled = mobileNumber.isNotBlank()
            ) {
                Text("Confirm", color = Color(0xFF4FC3F7), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
