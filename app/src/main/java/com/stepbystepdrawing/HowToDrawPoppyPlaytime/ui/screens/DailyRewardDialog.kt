package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlueDim
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextPrimary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextSecondary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.CardBorderLight

private data class DayReward(val day: Int, val xp: Int, val label: String, val isSpecial: Boolean = false)

private val REWARDS = listOf(
    DayReward(1, 20, "+20 XP"),
    DayReward(2, 30, "+30 XP"),
    DayReward(3, 40, "+40 XP"),
    DayReward(4, 50, "+50 XP"),
    DayReward(5, 75, "+75 XP"),
    DayReward(6, 100, "+100 XP"),
    DayReward(7, 200, "Character!", isSpecial = true),
)

@Composable
fun DailyRewardDialog(
    currentDay: Int,
    alreadyClaimedToday: Boolean,
    onClaim: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(2.dp, PrimaryBlue, RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text("Daily Rewards!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Come back every day!", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))

            // 7 day grid (2 rows)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    REWARDS.take(4).forEach { reward ->
                        DayBox(
                            reward = reward,
                            isClaimed = reward.day < currentDay,
                            isToday = reward.day == currentDay,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    REWARDS.drop(4).forEach { reward ->
                        DayBox(
                            reward = reward,
                            isClaimed = reward.day < currentDay,
                            isToday = reward.day == currentDay,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { if (!alreadyClaimedToday) onClaim(currentDay) },
                enabled = !alreadyClaimedToday,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    if (alreadyClaimedToday) "Come back tomorrow!" else "Claim Day $currentDay reward!",
                    fontWeight = FontWeight.Bold, color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DayBox(reward: DayReward, isClaimed: Boolean, isToday: Boolean, modifier: Modifier) {
    val bgColor = when {
        isClaimed -> Color(0xFF4CAF50)
        isToday -> PrimaryBlueDim
        else -> Color.White
    }
    val borderColor = when {
        isToday -> PrimaryBlue
        isClaimed -> Color(0xFF4CAF50)
        else -> CardBorderLight
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Day ${reward.day}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        when {
            isClaimed -> Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
            reward.isSpecial -> Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
            else -> Icon(Icons.Filled.CardGiftcard, null, tint = if (isToday) PrimaryBlue else TextSecondary, modifier = Modifier.size(16.dp))
        }
        Text(reward.label, fontSize = 8.sp, color = if (isClaimed) Color.White else TextSecondary, textAlign = TextAlign.Center)
    }
}
