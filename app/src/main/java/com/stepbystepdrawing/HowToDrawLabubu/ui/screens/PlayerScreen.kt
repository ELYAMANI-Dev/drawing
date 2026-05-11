package com.stepbystepdrawing.HowToDrawLabubu.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.stepbystepdrawing.HowToDrawLabubu.ui.components.NavButton
import com.stepbystepdrawing.HowToDrawLabubu.ui.theme.IconGray
import com.stepbystepdrawing.HowToDrawLabubu.ui.theme.PrimaryBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun PlayerScreen(
    stepImageUrl: String,
    currentStep: Int,
    totalSteps: Int,
    onBackToGallery: () -> Unit,
    onBackStep: () -> Unit,
    onNextStep: () -> Unit,
    onGoToPreview: () -> Unit,
    onCompleteLesson: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top bar: ← | bold-blue number / gray total | share
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToGallery) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = IconGray,
                    modifier = Modifier.size(30.dp)
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentStep + 1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = PrimaryBlue
                )
                Text(
                    text = " / $totalSteps",
                    fontSize = 28.sp,
                    color = IconGray
                )
            }
            IconButton(
                onClick = {
                    scope.launch {
                        val sharedUri = withContext(Dispatchers.IO) {
                            createShareImageUri(context = context, imageUrl = stepImageUrl)
                        }
                        if (sharedUri == null) {
                            Toast.makeText(context, "Unable to share image", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, sharedUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share drawing step"))
                    }
                }
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = IconGray,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Progress bar
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / totalSteps.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = PrimaryBlue,
            trackColor = Color(0xFFE0E0E0)
        )

        // Drawing image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(stepImageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(false)
                    .build(),
                contentDescription = "Step ${currentStep + 1}",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
            )
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
            ) {
                val cellSize = 20.dp.toPx()
                val gridColor = Color(0xFF8D99AE).copy(alpha = 0.25f)

                var x = 0f
                while (x <= size.width) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                    x += cellSize
                }

                var y = 0f
                while (y <= size.height) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += cellSize
                }
            }
        }

        // Copyright text
        Text(
            text = "© all rights reserved",
            modifier = Modifier.padding(start = 16.dp, bottom = 10.dp),
            fontSize = 13.sp,
            color = Color.Black
        )

        // Three nav buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavButton(
                icon = Icons.Default.ArrowBack,
                tint = if (currentStep > 0) PrimaryBlue else IconGray,
                bgColor = Color.White,
                widthDp = 104,
                borderColor = PrimaryBlue,
                onClick = { if (currentStep > 0) onBackStep() }
            )
            NavButton(
                icon = Icons.Default.Refresh,
                tint = IconGray,
                bgColor = Color.White,
                widthDp = 80,
                borderColor = PrimaryBlue,
                onClick = onGoToPreview
            )
            NavButton(
                icon = if (currentStep < totalSteps - 1) Icons.Default.ArrowForward else Icons.Default.PlayArrow,
                tint = PrimaryBlue,
                bgColor = Color.White,
                widthDp = 104,
                borderColor = PrimaryBlue,
                onClick = {
                    if (currentStep < totalSteps - 1) onNextStep() else onCompleteLesson()
                }
            )
        }
    }
}

private suspend fun createShareImageUri(context: android.content.Context, imageUrl: String): android.net.Uri? {
    val request = ImageRequest.Builder(context)
        .data(imageUrl)
        .allowHardware(false)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .build()

    val result = context.imageLoader.execute(request) as? SuccessResult ?: return null
    val bitmap: Bitmap = result.drawable.toBitmap()

    val shareDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
    val imageFile = File(shareDir, "step_${UUID.randomUUID()}.png")
    FileOutputStream(imageFile).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
