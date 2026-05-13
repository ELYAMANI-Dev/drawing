package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.R
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.UnavailableKind
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlueDim
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextMuted
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextPrimary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextSecondary

private val SignalBarGreen = Color(0xFF43A047)
private val SignalBadgeRed = Color(0xFFE53935)
private val WarningAmber = Color(0xFFFFA726)

@Composable
fun OfflineConnectionScreen(
    kind: UnavailableKind,
    detailMessage: String,
    onCloseApp: () -> Unit,
    onOpenNetworkSettings: () -> Unit,
    onRetry: () -> Unit,
) {
    var showWhyDialog by remember { mutableStateOf(false) }
    val showOfflineCopy = kind == UnavailableKind.NO_DEVICE_INTERNET ||
        kind == UnavailableKind.NETWORK_OR_TRANSPORT

    BackHandler { onCloseApp() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showOfflineCopy) {
                NoInternetSignalIcon(modifier = Modifier.padding(bottom = 16.dp))
            } else {
                ContentProblemIcon(modifier = Modifier.padding(bottom = 16.dp))
            }

            Text(
                text = stringResource(
                    if (showOfflineCopy) R.string.offline_title else R.string.content_error_title,
                ),
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(
                    if (showOfflineCopy) R.string.offline_subtitle else R.string.content_error_subtitle,
                ),
                color = TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            )

            if (detailMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = detailMessage,
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(PrimaryBlueDim, RoundedCornerShape(16.dp))
                        .border(2.dp, PrimaryBlue, RoundedCornerShape(16.dp))
                        .clickable { onRetry() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.offline_retry),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(PrimaryBlueDim, RoundedCornerShape(16.dp))
                        .border(2.dp, PrimaryBlue, RoundedCornerShape(16.dp))
                        .clickable { onOpenNetworkSettings() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.offline_open_network_settings),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showOfflineCopy) {
                Text(
                    text = stringResource(R.string.offline_why_link),
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { showWhyDialog = true },
                )
            }
        }
    }

    if (showWhyDialog) {
        AlertDialog(
            onDismissRequest = { showWhyDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.offline_why_dialog_title),
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.offline_why_dialog_body,
                        stringResource(R.string.app_name),
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = { showWhyDialog = false }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
        )
    }
}

@Composable
private fun ContentProblemIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(96.dp, 78.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(72.dp, 64.dp)) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height * 0.08f)
                lineTo(size.width * 0.92f, size.height * 0.88f)
                lineTo(size.width * 0.08f, size.height * 0.88f)
                close()
            }
            drawPath(path, color = WarningAmber)
            drawPath(
                path,
                color = PrimaryBlue,
                style = Stroke(width = size.minDimension * 0.06f),
            )
        }
        Text(
            text = "!",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.offset(y = (-4).dp),
        )
    }
}

@Composable
private fun NoInternetSignalIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(width = 96.dp, height = 78.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(72.dp, 62.dp)) {
            val barW = size.width * 0.11f
            val gap = size.width * 0.07f
            val heights = listOf(0.28f, 0.48f, 0.68f, 0.92f)
            var x = size.width * 0.12f
            heights.forEach { fracH ->
                val h = size.height * fracH
                drawRoundRect(
                    color = SignalBarGreen,
                    topLeft = Offset(x, size.height - h),
                    size = Size(barW, h),
                    cornerRadius = CornerRadius(barW / 2f, barW / 2f),
                )
                x += barW + gap
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 6.dp, y = 4.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(SignalBadgeRed)
                .border(2.dp, PrimaryBlue, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✕",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
