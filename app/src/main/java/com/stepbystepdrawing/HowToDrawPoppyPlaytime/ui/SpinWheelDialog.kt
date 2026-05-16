package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.RetentionManager
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdManager
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdService
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.SoundManager
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class SpinReward(val label: String, val stars: Int) {
    STAR_1("⭐ 1", 1),
    STAR_2("⭐ 2", 2),
    STAR_3("⭐ 3", 3),
    STAR_5("⭐ 5", 5),
    STAR_10("⭐ 10", 10),
}

private val SEGMENTS = listOf(
    SpinReward.STAR_1,
    SpinReward.STAR_3,
    SpinReward.STAR_1,
    SpinReward.STAR_5,
    SpinReward.STAR_2,
    SpinReward.STAR_10,
    SpinReward.STAR_2,
    SpinReward.STAR_3,
)

private val SEGMENT_COLORS = listOf(
    Color(0xFF7DD3FC), Color(0xFFE0F2FE),
    Color(0xFF7DD3FC), Color(0xFFFDE68A),
    Color(0xFFE0F2FE), Color(0xFFFCA5A5),
    Color(0xFF7DD3FC), Color(0xFFE0F2FE),
)

@Composable
fun SpinWheelDialog(
    retentionManager: RetentionManager,
    onDismiss: () -> Unit,
    onRewardEarned: (SpinReward) -> Unit,
) {
    val activity = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    var spinning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<SpinReward?>(null) }

    Dialog(
        onDismissRequest = { if (!spinning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(2.dp, BorderStrong, RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎡 Daily Spin",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Spin to win stars!",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(Modifier.height(16.dp))

            // Wheel
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sweepAngle = 360f / SEGMENTS.size
                    SEGMENTS.forEachIndexed { index, _ ->
                        rotate(rotation.value + index * sweepAngle) {
                            drawArc(
                                color = SEGMENT_COLORS[index % SEGMENT_COLORS.size],
                                startAngle = -sweepAngle / 2,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = Size(size.width, size.height)
                            )
                        }
                    }
                    // Draw labels
                    val radius = size.width / 2 * 0.65f
                    SEGMENTS.forEachIndexed { index, reward ->
                        val angle = Math.toRadians((rotation.value + index * sweepAngle).toDouble())
                        val x = center.x + radius * cos(angle).toFloat()
                        val y = center.y + radius * sin(angle).toFloat()
                        drawContext.canvas.nativeCanvas.drawText(
                            reward.label,
                            x, y + 6f,
                            android.graphics.Paint().apply {
                                textSize = 28f
                                textAlign = android.graphics.Paint.Align.CENTER
                                color = android.graphics.Color.BLACK
                                isFakeBoldText = true
                            }
                        )
                    }
                    // Pointer triangle at top
                    val pointerPath = Path().apply {
                        moveTo(center.x, 0f)
                        lineTo(center.x - 16f, -30f)
                        lineTo(center.x + 16f, -30f)
                        close()
                    }
                    drawPath(pointerPath, Color.Red)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (result != null) {
                Text(
                    text = "You won ${result!!.label}!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlueDim, RoundedCornerShape(14.dp))
                        .border(2.dp, PrimaryBlue, RoundedCornerShape(14.dp))
                        .clickable {
                            onRewardEarned(result!!)
                            onDismiss()
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Collect", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (!spinning) PrimaryBlueDim else Color.LightGray,
                            RoundedCornerShape(14.dp)
                        )
                        .border(2.dp, if (!spinning) PrimaryBlue else Color.Gray, RoundedCornerShape(14.dp))
                        .clickable(enabled = !spinning) {
                            spinning = true
                            scope.launch {
                                // Show interstitial before spin
                                if (AdManager.isAdsEnabled && activity != null) {
                                    AdService.showInterstitial(activity)
                                }
                                // Spin animation
                                val targetIndex = (0 until SEGMENTS.size).random()
                                val sweepAngle = 360f / SEGMENTS.size
                                val targetRotation = 360f * (5 + (Math.random() * 3).toInt()) +
                                        (targetIndex * sweepAngle)
                                rotation.animateTo(
                                    targetValue = targetRotation,
                                    animationSpec = tween(
                                        durationMillis = 3500,
                                        easing = FastOutSlowInEasing
                                    )
                                )
                                SoundManager.playCelebration()
                                retentionManager.recordSpin()
                                val reward = SEGMENTS[targetIndex]
                                retentionManager.addStars(reward.stars)
                                result = reward
                            }
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (spinning) "Spinning..." else "SPIN!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
