package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.User
import com.example.ui.components.GlassCard
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Email

import com.example.ui.utils.Language
import com.example.ui.utils.getText

@Composable
fun DashboardScreen(
    user: User,
    language: Language,
    contestants: List<User>,
    contestStartTime: Long?,
    onLanguageChange: (Language) -> Unit,
    onPlayQuiz: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Progress") }

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
            modifier = Modifier.padding(16.dp).padding(bottom = 60.dp) // Leave space for bottom nav
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = Color.White,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray.copy(alpha = 0.5f))
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = user.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Rank:", color = Color.LightGray, fontSize = 12.sp)
                        Text("#${user.rank}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            when (selectedTab) {
                "Progress" -> ProgressTab(user = user, language = language, onPlayQuiz = onPlayQuiz)
                "Wallet" -> WalletScreen(user = user, language = language)
                "Contest" -> ContestScreen(user = user, language = language, contestants = contestants, contestStartTime = contestStartTime)
                "Info" -> InfoScreen(language = language, onLanguageChange = onLanguageChange)
                "Contact" -> SupportScreen(language = language)
            }
        }
        
        // Bottom Navigation
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = Color(0xFF1B263B)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BottomNavItem(getText("Progress", language), Icons.Default.List, selectedTab == "Progress") { selectedTab = "Progress" }
                BottomNavItem(getText("Wallet", language), Icons.Default.ShoppingCart, selectedTab == "Wallet") { selectedTab = "Wallet" }
                BottomNavItem(getText("Contest", language), Icons.Default.Person, selectedTab == "Contest") { selectedTab = "Contest" }
                BottomNavItem(getText("Info", language), Icons.Default.Info, selectedTab == "Info") { selectedTab = "Info" }
                BottomNavItem(getText("Contact", language), Icons.Default.Email, selectedTab == "Contact") { selectedTab = "Contact" }
            }
        }
    }
}

@Composable
fun ProgressTab(user: User, language: Language, onPlayQuiz: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Text(getText("Your current level: ", language) + "${user.currentLevel}", color = Color.LightGray, fontSize = 12.sp)
            Text(getText("Total time worked: ", language) + "${user.totalTimeWorkedSeconds / 3600}h ${(user.totalTimeWorkedSeconds % 3600) / 60}m", color = Color.LightGray, fontSize = 12.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Play Button
        com.example.ui.components.GradientButton(
            onClick = onPlayQuiz,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Make it much bigger
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "∑π",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = getText("Play Quiz", language),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Big Water Logo and Progress
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_water_logo_1784671474407),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val progress = (user.currentLevel.toFloat() / user.levelsToQualify).coerceIn(0f, 1f)
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape),
                color = Color(0xFF64B5F6),
                trackColor = Color.DarkGray,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Levels to qualify: ${user.levelsToQualify} levels",
                color = Color.LightGray,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Contest is waiting for minimum players to join 1000",
                color = Color(0xFFFFB74D),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ComingSoonTab(tabName: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$tabName is coming soon!", color = Color.LightGray, fontSize = 18.sp)
    }
}

@Composable
fun BottomNavItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) Color(0xFF4FC3F7) else Color.Gray
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Text(text = label, color = color, fontSize = 10.sp)
    }
}
