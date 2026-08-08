package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.User
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import com.example.ui.components.GradientButton
import com.example.ui.components.GlassCard
import kotlinx.coroutines.delay
import kotlin.random.Random
import com.example.ui.utils.Language
import com.example.ui.utils.getText
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.rotate

enum class TeacherState {
    IDLE, HAPPY, ANGRY
}

@Composable
fun QuizScreen(
    user: User?,
    language: Language,
    onGameEnd: (Boolean) -> Unit, // true if passed, false if failed
    onUseExtraLife: (onSuccess: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var rewardedAd by remember { mutableStateOf<RewardedAd?>(null) }
    var isAdLoading by remember { mutableStateOf(false) }

    var currentMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentMillis = System.currentTimeMillis()
        }
    }

    val usedToday = remember(user?.extraLivesUsedToday, user?.lastExtraLiveDate, currentMillis) {
        if (user == null) 0
        else {
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = currentMillis
            val userCalendar = java.util.Calendar.getInstance().apply { timeInMillis = user.lastExtraLiveDate }
            val isSameDay = calendar.get(java.util.Calendar.YEAR) == userCalendar.get(java.util.Calendar.YEAR) &&
                            calendar.get(java.util.Calendar.DAY_OF_YEAR) == userCalendar.get(java.util.Calendar.DAY_OF_YEAR)
            if (isSameDay) user.extraLivesUsedToday else 0
        }
    }

    val adFreeStart = user?.adFreeRewardStartTime ?: 0L
    val isAdFree = currentMillis < adFreeStart + 3600_000L
    
    val timeUntilMidnight = remember(currentMillis) {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = currentMillis
        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val diff = calendar.timeInMillis - currentMillis
        val hours = (diff / 3600000).toInt()
        val mins = ((diff % 3600000) / 60000).toInt()
        val secs = ((diff % 60000) / 1000).toInt()
        String.format("%02d:%02d:%02d", hours, mins, secs)
    }
    
    val adFreeTimeLeft = remember(currentMillis, adFreeStart) {
        val diff = (adFreeStart + 3600_000L) - currentMillis
        if (diff > 0) {
            val mins = ((diff % 3600000) / 60000).toInt()
            val secs = ((diff % 60000) / 1000).toInt()
            String.format("%02d:%02d", mins, secs)
        } else {
            "00:00"
        }
    }

    fun loadAd() {
        if (isAdLoading || rewardedAd != null) return
        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, "ca-app-pub-9745183019260952/2776641456", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                isAdLoading = false
                rewardedAd = null
            }
            override fun onAdLoaded(ad: RewardedAd) {
                isAdLoading = false
                rewardedAd = ad
            }
        })
    }

    LaunchedEffect(Unit) { loadAd() }

    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var timeLeft by remember { mutableStateOf(60) }
    var isGameOver by remember { mutableStateOf(false) }
    var num1 by remember { mutableStateOf(0) }
    var num2 by remember { mutableStateOf(0) }
    var options by remember { mutableStateOf(listOf<Int>()) }
    var correctAnswer by remember { mutableStateOf(0) }
    var teacherState by remember { mutableStateOf(TeacherState.IDLE) }
    val goal = 50

    fun generateQuestion() {
        num1 = Random.nextInt(1, 20)
        num2 = Random.nextInt(1, 20)
        correctAnswer = num1 + num2
        val wrong1 = correctAnswer + Random.nextInt(1, 5)
        var wrong2 = correctAnswer - Random.nextInt(1, 5)
        if (wrong2 == wrong1 || wrong2 == correctAnswer) wrong2 -= 1
        options = listOf(correctAnswer, wrong1, wrong2).shuffled()
    }

    fun showRewardedAdAndGrantLife() {
        val currentAd = rewardedAd
        if (currentAd != null && activity != null) {
            rewardedAd = null // clear current ad state immediately so loadAd() can fetch a new one
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadAd() // Preload next ad
                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    loadAd() // Preload next ad
                }
            }
            currentAd.show(activity, OnUserEarnedRewardListener {
                timeLeft = 60
                lives = 3
                isGameOver = false
                generateQuestion()
                loadAd() // Preload next ad
            })
        } else {
            Toast.makeText(context, "Loading Ad, giving extra life...", Toast.LENGTH_SHORT).show()
            timeLeft = 60
            lives = 3
            isGameOver = false
            generateQuestion()
            loadAd()
        }
    }

    LaunchedEffect(Unit) { generateQuestion() }

    LaunchedEffect(timeLeft, isGameOver) {
        if (!isGameOver && timeLeft > 0) {
            delay(1000)
            timeLeft -= 1
            if (timeLeft == 0) isGameOver = true
        }
    }

    LaunchedEffect(teacherState) {
        if (teacherState != TeacherState.IDLE) {
            delay(1500)
            teacherState = TeacherState.IDLE
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.water_app_bg_1784706944679),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
        
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_water_logo_1784671474407),
                    contentDescription = "Logo",
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
                
                Row {
                    repeat(3) { index ->
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Heart",
                            tint = if (index < lives) Color.Red else Color.Gray,
                            modifier = Modifier.size(32.dp).padding(horizontal = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Teacher Character
            val teacherImage = when (teacherState) {
                TeacherState.IDLE -> R.drawable.teacher_idle_white_1784708748355
                TeacherState.HAPPY -> R.drawable.teacher_happy_white_1784708762793
                TeacherState.ANGRY -> R.drawable.teacher_angry_white_1784708774706
            }
            
            // Animation for teacher
            val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition()
            val rotation by infiniteTransition.animateFloat(
                initialValue = -5f,
                targetValue = 5f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.LinearEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                )
            )
            val bounce by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -20f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                )
            )
            
            val animModifier = when (teacherState) {
                TeacherState.IDLE -> Modifier
                TeacherState.HAPPY -> Modifier.offset(y = bounce.dp)
                TeacherState.ANGRY -> Modifier.rotate(rotation)
            }
            
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp).then(animModifier)) {
                Image(
                    painter = painterResource(id = teacherImage),
                    contentDescription = "Teacher",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
                if (teacherState == TeacherState.HAPPY) {
                    Text(getText("Good!", language), color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp))
                } else if (teacherState == TeacherState.ANGRY) {
                    Text(getText("Time's Up!", language), color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Timer Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                CircularProgressIndicator(
                    progress = { timeLeft / 60f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF81D4FA),
                    trackColor = Color.White.copy(alpha = 0.1f),
                    strokeWidth = 8.dp
                )
                Text(
                    text = String.format("00:%02d", timeLeft),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(getText("Score", language) + ":", color = Color.LightGray)
                    Text(score.toString(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(getText("Goal", language) + ":", color = Color.LightGray)
                    Text(goal.toString(), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = getText("Solve", language) + ": $num1 + $num2 = ?",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                options.forEachIndexed { index, option ->
                    val label = ('A' + index).toString()
                    OptionCard(label = getText("Option", language) + " $label:", value = option.toString()) {
                        if (!isGameOver) {
                            if (option == correctAnswer) {
                                score += 1
                                teacherState = TeacherState.HAPPY
                                if (score >= goal) {
                                    isGameOver = true
                                    onGameEnd(true)
                                } else {
                                    generateQuestion()
                                }
                            } else {
                                lives -= 1
                                teacherState = TeacherState.ANGRY
                                if (lives <= 0) {
                                    isGameOver = true
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }

        if (isGameOver && score < goal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(Color(0xFF1B263B), RoundedCornerShape(16.dp))
                        .padding(32.dp)
                ) {
                    Text(if (lives <= 0) getText("Out of Lives!", language) else getText("Time's Up!", language), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(getText("Score", language) + ": $score / $goal", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(24.dp))
                    if (usedToday >= 60) {
                        Text(
                            text = "Daily Limit Reached (60/60)",
                            color = Color(0xFFE57373),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "Time until reset: $timeUntilMidnight",
                            color = Color(0xFFFFB74D),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    } else if (isAdFree) {
                        Text(
                            text = "🎉 Ad-Free Extra Life Active!",
                            color = Color(0xFF81C784),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = "⏰ Time remaining: $adFreeTimeLeft",
                            color = Color(0xFFFFB74D),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    } else {
                        val usesRemaining = 5 - (user?.adLivesUsedInCycle ?: 0)
                        Text(
                            text = "Watch $usesRemaining more ad(s) to get 1 Hour Ad-Free!",
                            color = Color(0xFF4FC3F7),
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Daily Limit: $usedToday / 60",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    
                    if (usedToday < 60) {
                        GradientButton(
                            onClick = {
                                onUseExtraLife {
                                    if (isAdFree) {
                                        timeLeft = 60
                                        lives = 3
                                        isGameOver = false
                                        generateQuestion()
                                    } else {
                                        showRewardedAdAndGrantLife()
                                    }
                                }
                            },
                        ) {
                            Icon(
                                imageVector = if (isAdFree) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                contentDescription = "Extra Life",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAdFree) "Extra Live Ad Free" else getText("Use Extra Life (Watch Ad)", language),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            score = 0
                            timeLeft = 60
                            lives = 3
                            isGameOver = false
                            generateQuestion()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text(getText("Restart (Score 0)", language))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { onGameEnd(false) }) {
                        Text(getText("Quit Game", language), color = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun OptionCard(label: String, value: String, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .width(100.dp)
            .height(120.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp).fillMaxSize()
        ) {
            Text("∑π", color = Color(0xFF4FC3F7), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, color = Color.LightGray, fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
