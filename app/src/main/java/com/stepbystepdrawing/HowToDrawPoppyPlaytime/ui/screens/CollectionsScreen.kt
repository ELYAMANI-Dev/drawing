package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.Icon
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
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingCard
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlueDim
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextPrimary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextSecondary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.CardBorderLight
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components.TopIconRow

data class Collection(
    val id: String,
    val name: String,
    val description: String,
    val characterIds: List<String>,
    val bonusXp: Int,
)

fun buildCollections(cards: List<DrawingCard>): List<Collection> {
    if (cards.size < 10) return emptyList()
    val collections = mutableListOf<Collection>()
    val chunkSize = 8
    val names = listOf(
        "Chapter 1" to "The beginning of the story",
        "Chapter 2" to "New characters appear",
        "Chapter 3" to "The adventure continues",
        "Villains" to "The bad guys collection",
        "Heroes" to "The good guys collection",
        "Rare Characters" to "Hard to find characters",
        "Fan Favorites" to "Most popular drawings",
        "Secret Collection" to "Hidden characters",
    )
    cards.chunked(chunkSize).forEachIndexed { idx, chunk ->
        if (idx < names.size) {
            collections += Collection(
                id = "col_$idx",
                name = names[idx].first,
                description = names[idx].second,
                characterIds = chunk.map { it.id },
                bonusXp = (idx + 1) * 100,
            )
        }
    }
    return collections
}

@Composable
fun CollectionsScreen(
    cards: List<DrawingCard>,
    completedDrawings: Set<String>,
    onBack: () -> Unit,
    onSelectLesson: (String) -> Unit,
) {
    val collections = buildCollections(cards)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopIconRow(showBack = true, onBack = onBack, showTrailingActions = false)
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Collections", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Complete sets to earn bonus XP!", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(collections) { col ->
                val completed = col.characterIds.count { it in completedDrawings }
                val total = col.characterIds.size
                val isComplete = completed >= total
                val progress = completed.toFloat() / total.coerceAtLeast(1)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            1.5.dp,
                            if (isComplete) Color(0xFF4CAF50) else CardBorderLight,
                            RoundedCornerShape(14.dp)
                        )
                        .background(if (isComplete) Color(0xFFE8F5E9) else Color.White)
                        .clickable {
                            val next = col.characterIds.firstOrNull { it !in completedDrawings }
                            if (next != null) onSelectLesson(next)
                        }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isComplete) Icons.Filled.CheckCircle else Icons.Filled.Collections,
                            contentDescription = null,
                            tint = if (isComplete) Color(0xFF4CAF50) else PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(col.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text(col.description, fontSize = 12.sp, color = TextSecondary)
                        }
                        Text(
                            "$completed/$total",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isComplete) Color(0xFF4CAF50) else TextSecondary
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (isComplete) Color(0xFF4CAF50) else PrimaryBlue,
                        trackColor = PrimaryBlueDim,
                    )
                    if (isComplete) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Completed! +${col.bonusXp} XP earned",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}
