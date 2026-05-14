package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.UserProfile
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlueDim
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextPrimary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextSecondary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.CardBorderLight
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components.TopIconRow

private data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
)

private val ALL_ACHIEVEMENTS = listOf(
    AchievementDef("first_drawing", "First Steps", "Complete your first drawing", Icons.Filled.Star),
    AchievementDef("ten_drawings", "Getting Good", "Complete 10 drawings", Icons.Filled.Star),
    AchievementDef("fifty_drawings", "Master Artist", "Complete 50 drawings", Icons.Filled.EmojiEvents),
    AchievementDef("streak_3", "On Fire!", "3-day streak", Icons.Filled.LocalFireDepartment),
    AchievementDef("streak_7", "Week Warrior", "7-day streak", Icons.Filled.LocalFireDepartment),
    AchievementDef("streak_30", "Unstoppable", "30-day streak", Icons.Filled.LocalFireDepartment),
    AchievementDef("level_5", "Rising Star", "Reach level 5", Icons.Filled.EmojiEvents),
    AchievementDef("level_10", "Drawing Pro", "Reach level 10", Icons.Filled.EmojiEvents),
    AchievementDef("complete_20", "Collector", "Complete 20 unique drawings", Icons.Filled.Star),
)

@Composable
fun AchievementsScreen(
    profile: UserProfile,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopIconRow(showBack = true, onBack = onBack, showTrailingActions = false)
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Achievements", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("${profile.achievements.size} / ${ALL_ACHIEVEMENTS.size} unlocked", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(ALL_ACHIEVEMENTS) { ach ->
                val unlocked = ach.id in profile.achievements
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, if (unlocked) PrimaryBlue else CardBorderLight, RoundedCornerShape(12.dp))
                        .background(if (unlocked) PrimaryBlueDim else Color.White)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (unlocked) PrimaryBlue else CardBorderLight),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (unlocked) ach.icon else Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column(modifier = Modifier.padding(start = 14.dp)) {
                        Text(ach.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (unlocked) TextPrimary else TextSecondary)
                        Text(ach.description, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
