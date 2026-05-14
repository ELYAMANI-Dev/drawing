package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Looks3
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingCard
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingDifficultyFilter
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.cleanTitle
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components.CardThumbnailSpinner
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.CardBorderLight
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.IconGray
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlueDim
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextPrimary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextSecondary

private fun DrawingDifficultyFilter.rowIcon(): ImageVector = when (this) {
    DrawingDifficultyFilter.All -> Icons.Filled.Apps
    DrawingDifficultyFilter.Easy -> Icons.Filled.LooksOne
    DrawingDifficultyFilter.Normal -> Icons.Filled.LooksTwo
    DrawingDifficultyFilter.Hard -> Icons.Filled.Looks3
}

@Composable
internal fun DifficultyFilterRow(
    selected: DrawingDifficultyFilter,
    onSelect: (DrawingDifficultyFilter) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SELECT DIFFICULTY",
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DrawingDifficultyFilter.entries.forEach { filter ->
                val isSelected = filter == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) PrimaryBlue else CardBorderLight,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(if (isSelected) PrimaryBlueDim else Color.White)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(filter) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = filter.rowIcon(),
                        contentDescription = filter.contentDescription,
                        tint = if (isSelected) PrimaryBlue else IconGray,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun FeaturedLessonRow(
    cards: List<DrawingCard>,
    onSelect: (String) -> Unit
) {
    val context = LocalContext.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        lazyRowItems(cards) { card ->
            Card(
                modifier = Modifier
                    .width(150.dp)
                    .height(92.dp)
                    .border(1.dp, CardBorderLight, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(card.id) },
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = cleanTitle(card.title).uppercase(),
                            color = TextPrimary,
                            maxLines = 1,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${card.totalSteps}",
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "steps",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    LessonCardThumbnail(
                        context = context,
                        imageUrl = card.thumbnailUrl,
                        contentDescription = cleanTitle(card.title),
                        contentScale = ContentScale.Fit,
                        loadingIconSize = 22.dp,
                        modifier = Modifier
                            .size(54.dp)
                            .align(Alignment.Bottom),
                    )
                }
            }
        }
    }
}

@Composable
internal fun LessonGridCard(
    card: DrawingCard,
    isLocked: Boolean = false,
    onSelect: (String) -> Unit,
) {
    val context = LocalContext.current
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.9f)
                .border(1.dp, if (isLocked) CardBorderLight else CardBorderLight, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .alpha(if (isLocked) 0.5f else 1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(card.id) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${card.totalSteps}",
                        color = Color(0xFFF59E0B),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Steps",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LessonCardThumbnail(
                        context = context,
                        imageUrl = card.thumbnailUrl,
                        contentDescription = cleanTitle(card.title),
                        contentScale = ContentScale.Fit,
                        loadingIconSize = 36.dp,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Text(
                    text = cleanTitle(card.title).uppercase(),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }
        if (isLocked) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Locked",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
internal fun LessonCardThumbnail(
    context: android.content.Context,
    imageUrl: String,
    contentDescription: String?,
    contentScale: ContentScale,
    loadingIconSize: Dp,
    modifier: Modifier = Modifier,
) {
    if (imageUrl.isBlank()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "—",
                color = TextSecondary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        return
    }
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(128, 128)
            .precision(coil.size.Precision.INEXACT)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(false)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        loading = {
            CardThumbnailSpinner(
                iconSize = loadingIconSize,
                modifier = Modifier.fillMaxSize(),
            )
        },
        error = {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "!",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
    )
}
