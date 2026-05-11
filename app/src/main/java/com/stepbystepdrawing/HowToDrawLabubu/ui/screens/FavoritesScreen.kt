package com.stepbystepdrawing.HowToDrawLabubu.ui.screens

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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepbystepdrawing.HowToDrawLabubu.data.DrawingCard
import com.stepbystepdrawing.HowToDrawLabubu.ui.components.TopIconRow
import com.stepbystepdrawing.HowToDrawLabubu.ui.theme.CardBorderLight
import com.stepbystepdrawing.HowToDrawLabubu.ui.theme.TextPrimary
import com.stepbystepdrawing.HowToDrawLabubu.ui.theme.TextSecondary

@Composable
fun FavoritesScreen(
    favoriteCards: List<DrawingCard>,
    onBack: () -> Unit,
    onSelectLesson: (String) -> Unit,
) {
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
                showBack = true,
                onBack = onBack,
                showTrailingActions = false
            )
            Text(
                text = "MY FAVORITES",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(CardBorderLight)
            )
        }

        if (favoriteCards.isEmpty()) {
            Text(
                text = "No favorites yet. Star a lesson from the gallery or from the preview screen to see it here.",
                color = TextSecondary,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 28.dp, vertical = 48.dp)
            )
        } else {
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
                items(items = favoriteCards, key = { it.id }) { card ->
                    LessonGridCard(
                        card = card,
                        onSelect = onSelectLesson
                    )
                }
            }
        }
    }
}
