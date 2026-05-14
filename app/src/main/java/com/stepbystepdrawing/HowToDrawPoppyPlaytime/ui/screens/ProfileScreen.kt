package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.GamificationEngine
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.UserProfile
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlueDim
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextPrimary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextSecondary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components.TopIconRow

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onAchievements: () -> Unit,
) {
    val xpForNext = GamificationEngine.xpForLevel(profile.level)
    val prevLevelXp = (1 until profile.level).sumOf { GamificationEngine.xpForLevel(it) }
    val currentLevelXp = profile.xp - prevLevelXp
    val xpProgress = (currentLevelXp.toFloat() / xpForNext).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopIconRow(showBack = true, onBack = onBack, showTrailingActions = false)
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlueDim),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = profile.name.take(1).uppercase().ifBlank { "?" },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(profile.name.ifBlank { "Artist" }, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Level ${profile.level}", fontSize = 14.sp, color = TextSecondary)

            Spacer(Modifier.height(20.dp))

            // XP Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("XP", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextSecondary)
                    Text("$currentLevelXp / $xpForNext", fontSize = 13.sp, color = TextSecondary)
                }
                LinearProgressIndicator(
                    progress = { xpProgress },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                    color = PrimaryBlue,
                    trackColor = PrimaryBlueDim,
                )
            }

            Spacer(Modifier.height(24.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(Icons.Filled.LocalFireDepartment, "${profile.streak}", "Streak", Color(0xFFFF6B35))
                StatItem(Icons.Filled.Star, "${profile.totalDrawingsCompleted}", "Drawings", PrimaryBlue)
                StatItem(Icons.Filled.EmojiEvents, "${profile.achievements.size}", "Badges", Color(0xFFF59E0B))
            }

            Spacer(Modifier.height(24.dp))

            // Daily progress
            Text("Today: ${profile.todayDrawingsCompleted} / ${profile.dailyGoal}", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (profile.todayDrawingsCompleted.toFloat() / profile.dailyGoal).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50),
                trackColor = PrimaryBlueDim,
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAchievements,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("View Achievements", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
        Text(label, fontSize = 11.sp, color = TextSecondary)
    }
}
