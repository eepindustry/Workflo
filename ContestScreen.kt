package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.components.GlassCard

import com.example.ui.utils.Language
import com.example.ui.utils.getText

@Composable
fun ContestScreen(user: User, language: Language, contestants: List<User>, contestStartTime: Long?) {
    val totalUsersNeeded = 1000
    val currentUsers = contestants.size 

    var timeLeft by remember { mutableStateOf(0L) }
    
    LaunchedEffect(contestStartTime) {
        if (contestStartTime != null) {
            val endTime = contestStartTime + (30L * 24 * 60 * 60 * 1000)
            while(true) {
                val current = System.currentTimeMillis()
                if (current < endTime) {
                    timeLeft = endTime - current
                } else {
                    timeLeft = 0
                }
                kotlinx.coroutines.delay(60000)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = getText("Contest", language),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (contestStartTime != null && timeLeft > 0) {
            val days = timeLeft / (1000 * 60 * 60 * 24)
            val hours = (timeLeft / (1000 * 60 * 60)) % 24
            val minutes = (timeLeft / (1000 * 60)) % 60
            
            Text(
                text = "Contest is ending in $days days $hours hours $minutes minutes",
                color = Color.Green,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        } else if (contestStartTime != null && timeLeft <= 0) {
            Text(
                text = "Contest Ended. Waiting to restart...",
                color = Color(0xFFFFB74D),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            val remaining = if (totalUsersNeeded - currentUsers > 0) totalUsersNeeded - currentUsers else 0
            Text(
                text = getText("Contest is waiting to join", language) + " ($remaining " + getText("Out of 1000 players", language) + ")",
                color = Color(0xFFFFB74D),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (user.currentLevel < 100) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = getText("You need to reach Level 100 to join the Contest.\nYour current level: ", language) + "${user.currentLevel}",
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            GlassCard(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(getText("Rank", language), color = Color.LightGray, modifier = Modifier.weight(1f))
                    Text(getText("User", language), color = Color.LightGray, modifier = Modifier.weight(2f))
                    Text(getText("Level", language), color = Color.LightGray, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Text(getText("Winning Prize", language), color = Color.LightGray, modifier = Modifier.weight(1.5f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                val displayList = contestants
                
                itemsIndexed(displayList) { index, participant ->
                    val isMe = participant.googleId == user.googleId
                    val bgColor = if (isMe) Color(0xFF4FC3F7).copy(alpha = 0.2f) else Color.Transparent
                    
                    val prize = when (index) {
                        0 -> 12000
                        1 -> 7000
                        2 -> 4000
                        else -> 0
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${index + 1}",
                            color = if (index < 3) Color(0xFFFFD700) else Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (isMe) getText("You", language) else participant.name,
                            color = Color.White,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "${participant.currentLevel}",
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = if (prize > 0) "$prize" else "-",
                            color = Color(0xFF81D4FA),
                            modifier = Modifier.weight(1.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Divider(color = Color.White.copy(alpha = 0.1f))
                }
            }
        }
    }
}
