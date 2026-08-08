package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard

import com.example.ui.utils.Language
import com.example.ui.utils.getText

@Composable
fun InfoScreen(language: Language, onLanguageChange: (Language) -> Unit) {
    val isHindi = language == Language.HINDI

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("English", color = if (!isHindi) Color(0xFF4FC3F7) else Color.Gray)
            Switch(
                checked = isHindi,
                onCheckedChange = { if (it) onLanguageChange(Language.HINDI) else onLanguageChange(Language.ENGLISH) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4FC3F7)
                ),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text("हिंदी", color = if (isHindi) Color(0xFF4FC3F7) else Color.Gray)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = getText("Information", language),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(64.dp))
        
        GlassCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = getText("Thank you for joining Water family, please support us", language),
                color = Color.White,
                fontSize = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = getText("Message us for feedback:\ngodffm0202@gmail.com", language),
            color = Color.LightGray,
            fontSize = 16.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}
