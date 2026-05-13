package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingCard
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingDifficultyFilter
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.UiState
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.matchesDifficulty
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdManager
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components.CenteredLoading
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components.CenteredMessage
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components.GalleryNativeAdRow
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components.TopIconRow
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.CardBorderLight
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlueDim
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextPrimary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextSecondary

@Composable
fun GalleryScreen(
    heroTitle: String,
    galleryState: UiState<List<DrawingCard>>,
    onOpenFavorites: () -> Unit,
    favoritesShortcutHighlighted: Boolean,
    onPlayRandom: () -> Unit,
    onShareApp: () -> Unit,
    onRequestExit: () -> Unit,
    onSelect: (String) -> Unit
) {
    when (galleryState) {
        is UiState.Loading -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            CenteredLoading("Loading tutorials…")
        }
        is UiState.Error -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            CenteredMessage(galleryState.message)
        }
        is UiState.Success -> {
            if (galleryState.data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    CenteredMessage("No drawings found.")
                }
            } else {
                var difficultyFilter by remember { mutableStateOf(DrawingDifficultyFilter.All) }
                val filteredCards = remember(galleryState.data, difficultyFilter) {
                    galleryState.data.filter { it.matchesDifficulty(difficultyFilter) }
                }
                val lessonsTitle = when (difficultyFilter) {
                    DrawingDifficultyFilter.All -> "ALL LESSONS"
                    DrawingDifficultyFilter.Easy -> "EASY LESSONS"
                    DrawingDifficultyFilter.Normal -> "NORMAL LESSONS"
                    DrawingDifficultyFilter.Hard -> "HARD LESSONS"
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        TopIconRow(
                            showBack = false,
                            onBack = {},
                            onOpenFavorites = onOpenFavorites,
                            favoritesShortcutHighlighted = favoritesShortcutHighlighted,
                            onShare = onShareApp,
                            onPlayClick = onPlayRandom,
                            onExitClick = onRequestExit
                        )
                        GalleryHeader(heroTitle = heroTitle)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(CardBorderLight)
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 14.dp,
                            end = 14.dp,
                            top = 10.dp,
                            bottom = 16.dp
                        )
                    ) {
                        item(span = { GridItemSpan(2) }) {
                            FeaturedLessonRow(
                                cards = galleryState.data.take(3),
                                onSelect = onSelect
                            )
                        }

                        item(span = { GridItemSpan(2) }) {
                            DifficultyFilterRow(
                                selected = difficultyFilter,
                                onSelect = { difficultyFilter = it }
                            )
                        }

                        if (AdManager.isAdsEnabled) {
                            item(span = { GridItemSpan(2) }) {
                                GalleryNativeAdRow(
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }

                        item(span = { GridItemSpan(2) }) {
                            Text(
                                text = lessonsTitle,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                            )
                        }

                        if (filteredCards.isEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                Text(
                                    text = "No lessons for this difficulty. Try another filter.",
                                    color = TextSecondary,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 32.dp)
                                )
                            }
                        } else {
                            items(
                                items = filteredCards,
                                key = { it.id }
                            ) { card ->
                                LessonGridCard(
                                    card = card,
                                    onSelect = onSelect
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryHeader(heroTitle: String) {
    val titleFontSize = 20.sp
    val titleWeight = FontWeight.Bold
    val displayHero = heroTitle.trim().ifBlank { "HERO" }.uppercase()
    val title = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = TextSecondary,
                fontWeight = titleWeight
            )
        ) {
            append("How To Draw ")
        }
        withStyle(
            style = SpanStyle(
                color = TextPrimary,
                fontWeight = titleWeight
            )
        ) {
            append(displayHero)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(top = 2.dp, bottom = 4.dp)
            .clip(RectangleShape)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val r = size.minDimension
            drawCircle(
                color = PrimaryBlueDim,
                radius = r * 0.38f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.20f, size.height * 0.48f)
            )
            drawCircle(
                color = PrimaryBlueDim,
                radius = r * 0.15f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.84f, size.height * 0.22f)
            )
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.22f),
                radius = r * 0.18f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.55f, size.height * 0.78f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = title,
            fontSize = titleFontSize,
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp, end = 12.dp)
        )
    }
}
