package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingCard
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextPrimary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextSecondary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components.TopIconRow
import kotlinx.coroutines.launch

private val WHEEL_COLORS = listOf(
    Color(0xFF7DD3FC), Color(0xFFF59E0B), Color(0xFF4CAF50),
    Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFFFF5722),
    Color(0xFF00BCD4), Color(0xFF8BC34A),
)

@Composable
fun SpinWheelScreen(
    lockedCards: List<DrawingCard>,
    canSpin: Boolean,
    onWatchAdToSpin: () -> Unit,
    onWon: (DrawingCard) -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    var spinning by remember { mutableStateOf(false) }
    var wonCard by remember { mutableStateOf<DrawingCard?>(null) }
    val prizes = remember { lockedCards.shuffled().take(8) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopIconRow(showBack = true, onBack = onBack, showTrailingActions = false)
        Text("Spin to Unlock!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Watch an ad to spin the wheel", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sliceAngle = 360f / prizes.size.coerceAtLeast(1)
                prizes.forEachIndexed { i, _ ->
                    rotate(rotation.value + i * sliceAngle) {
                        drawArc(
                            color = WHEEL_COLORS[i % WHEEL_COLORS.size],
                            startAngle = -sliceAngle / 2,
                            sweepAngle = sliceAngle,
                            useCenter = true,
                            size = Size(size.width, size.height),
                        )
                    }
                }
            }
            // Pointer
            Canvas(modifier = Modifier.size(20.dp).align(Alignment.TopCenter)) {
                drawCircle(Color.Red, radius = size.minDimension / 2)
            }
        }

        Spacer(Modifier.height(8.dp))
        if (wonCard != null) {
            Text(
                "You won: ${wonCard!!.title}!",
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                if (!spinning && canSpin) {
                    onWatchAdToSpin()
                    spinning = true
                    scope.launch {
                        val targetIdx = (0 until prizes.size).random()
                        val sliceAngle = 360f / prizes.size
                        val target = 360f * 5 + targetIdx * sliceAngle
                        rotation.snapTo(0f)
                        rotation.animateTo(
                            target,
                            animationSpec = tween(3000, easing = FastOutSlowInEasing)
                        )
                        spinning = false
                        val won = prizes[targetIdx]
                        wonCard = won
                        onWon(won)
                    }
                }
            },
            enabled = canSpin && !spinning,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text(
                if (!canSpin) "Come back tomorrow!" else if (spinning) "Spinning..." else "Watch Ad & Spin",
                fontWeight = FontWeight.Bold, color = Color.White,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}
