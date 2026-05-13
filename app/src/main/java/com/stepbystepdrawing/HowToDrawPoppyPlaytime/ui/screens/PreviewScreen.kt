package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components.TopIconRow
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun PreviewScreen(
    previewImageUrl: String,
    totalStages: Int,
    isFavorite: Boolean,
    onBackToGallery: () -> Unit,
    onToggleFavorite: () -> Unit,
    onStartSteps: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopIconRow(
            showBack = true,
            onBack = onBackToGallery,
            showFavorite = true,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            onPlayClick = onStartSteps,
            onExitClick = onBackToGallery,
            onShare = {
                scope.launch {
                    val sharedUri = withContext(Dispatchers.IO) {
                        createShareImageUri(context = context, imageUrl = previewImageUrl)
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
                    context.startActivity(Intent.createChooser(shareIntent, "Share drawing"))
                }
            },
            horizontalArrangement = Arrangement.SpaceBetween
        )

        // "Stages: 15" — label black, number blue
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Stages: ", fontSize = 22.sp, color = Color.Black)
            Text(
                text = "$totalStages",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }

        // Centered drawing image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(previewImageUrl)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(false)
                    .build(),
                contentDescription = "Final result",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // Outlined circle play button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .border(BorderStroke(2.dp, PrimaryBlue), RoundedCornerShape(50))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onStartSteps
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start drawing",
                    modifier = Modifier.size(44.dp),
                    tint = PrimaryBlue
                )
            }
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
    val imageFile = File(shareDir, "preview_${UUID.randomUUID()}.png")
    FileOutputStream(imageFile).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
