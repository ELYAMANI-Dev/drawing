package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.*

@Composable
fun DailyStreakCard(
    streak: Int,
    totalStars: Int,
    onSpinClick: () -> Unit,
    canSpin: Boolean,
    onQuizClick: () -> Unit,
    canPlayQuiz: Boolean,
    cooldownText: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(PrimaryBlueDim, RoundedCornerShape(16.dp))
            .border(2.dp, PrimaryBlue, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔥 $streak day streak",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "⭐ $totalStars",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Spin button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (canSpin) Color.White else Color(0xFFEEEEEE),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.5.dp,
                        if (canSpin) PrimaryBlue else Color.Gray,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = canSpin) { onSpinClick() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (canSpin) "🎡 Spin" else "🎡 Done",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canSpin) TextPrimary else TextMuted
                )
            }

            // Quiz button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (canPlayQuiz) Color.White else Color(0xFFEEEEEE),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.5.dp,
                        if (canPlayQuiz) PrimaryBlue else Color.Gray,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = canPlayQuiz) { onQuizClick() }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (canPlayQuiz) "🎨 Quiz" else "⏱️ $cooldownText",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canPlayQuiz) TextPrimary else TextMuted
                )
            }
        }
    }
}

@Composable
fun WeeklyChallengeCard(
    lessonsCompleted: Int,
    target: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(Color(0xFFFEF3C7), RoundedCornerShape(16.dp))
            .border(2.dp, Color(0xFFF59E0B), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏆 Weekly Challenge",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "$lessonsCompleted / $target lessons",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (lessonsCompleted.toFloat() / target).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = Color(0xFFF59E0B),
            trackColor = Color(0xFFFDE68A),
        )
        if (lessonsCompleted >= target) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "✅ Challenge complete! +10 ⭐",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        }
    }
}
