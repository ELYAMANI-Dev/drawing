package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Boy
import androidx.compose.material.icons.filled.Girl
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingCard
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.Gender
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.SkillLevel
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.UserProfile
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlueDim
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextPrimary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextSecondary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.CardBorderLight

private const val TOTAL_STEPS = 6

@Composable
fun QuizOnboardingScreen(
    cards: List<DrawingCard>,
    onComplete: (UserProfile) -> Unit,
) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.OTHER) }
    var age by remember { mutableFloatStateOf(12f) }
    var skillLevel by remember { mutableStateOf(SkillLevel.BEGINNER) }
    val favorites = remember { mutableStateListOf<String>() }
    var dailyGoal by remember { mutableIntStateOf(3) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LinearProgressIndicator(
            progress = { (step + 1).toFloat() / TOTAL_STEPS },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = PrimaryBlue,
            trackColor = PrimaryBlueDim,
        )
        Spacer(Modifier.height(24.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            },
            modifier = Modifier.weight(1f),
            label = "quiz"
        ) { currentStep ->
            when (currentStep) {
                0 -> NameStep(name) { name = it }
                1 -> GenderStep(gender) { gender = it }
                2 -> AgeStep(age) { age = it }
                3 -> SkillStep(skillLevel) { skillLevel = it }
                4 -> FavoritesStep(cards, favorites)
                5 -> DailyGoalStep(dailyGoal) { dailyGoal = it }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (step < TOTAL_STEPS - 1) {
                    step++
                } else {
                    onComplete(
                        UserProfile(
                            name = name.trim(),
                            gender = gender,
                            age = age.toInt(),
                            skillLevel = skillLevel,
                            favorites = favorites.toList(),
                            dailyGoal = dailyGoal,
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(14.dp),
            enabled = when (step) {
                0 -> name.isNotBlank()
                4 -> favorites.size >= 1
                else -> true
            }
        ) {
            Text(
                text = if (step < TOTAL_STEPS - 1) "Next" else "Start Drawing!",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun NameStep(name: String, onChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("What\'s your name?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("We\'ll personalize your experience", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(32.dp))
        BasicTextField(
            value = name,
            onValueChange = { if (it.length <= 20) onChange(it) },
            textStyle = TextStyle(fontSize = 22.sp, textAlign = TextAlign.Center, color = TextPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlueDim, RoundedCornerShape(12.dp))
                .padding(16.dp),
            singleLine = true,
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.Center) {
                    if (name.isEmpty()) Text("Your name...", color = TextSecondary, fontSize = 22.sp)
                    inner()
                }
            }
        )
    }
}

@Composable
private fun GenderStep(selected: Gender, onSelect: (Gender) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Are you a...", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GenderOption(Icons.Filled.Boy, "Boy", selected == Gender.BOY) { onSelect(Gender.BOY) }
            GenderOption(Icons.Filled.Girl, "Girl", selected == Gender.GIRL) { onSelect(Gender.GIRL) }
        }
    }
}

@Composable
private fun GenderOption(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, if (isSelected) PrimaryBlue else CardBorderLight, RoundedCornerShape(16.dp))
            .background(if (isSelected) PrimaryBlueDim else Color.White)
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(48.dp), tint = if (isSelected) PrimaryBlue else TextSecondary)
        Spacer(Modifier.height(8.dp))
        Text(label, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun AgeStep(age: Float, onChange: (Float) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("How old are you?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(32.dp))
        Text("${age.toInt()}", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
        Spacer(Modifier.height(16.dp))
        Slider(
            value = age,
            onValueChange = onChange,
            valueRange = 4f..60f,
            steps = 55,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
        )
    }
}

@Composable
private fun SkillStep(selected: SkillLevel, onSelect: (SkillLevel) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Drawing level?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("This helps us suggest the right lessons", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(24.dp))
        SkillLevel.entries.forEach { level ->
            val isSelected = level == selected
            val label = when (level) {
                SkillLevel.BEGINNER -> "Beginner"
                SkillLevel.INTERMEDIATE -> "Intermediate"
                SkillLevel.PRO -> "Pro Artist"
            }
            val desc = when (level) {
                SkillLevel.BEGINNER -> "I\'m just starting out"
                SkillLevel.INTERMEDIATE -> "I draw sometimes"
                SkillLevel.PRO -> "I draw every day"
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(2.dp, if (isSelected) PrimaryBlue else CardBorderLight, RoundedCornerShape(14.dp))
                    .background(if (isSelected) PrimaryBlueDim else Color.White)
                    .clickable { onSelect(level) }
                    .padding(16.dp)
            ) {
                Column {
                    Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text(desc, fontSize = 13.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun FavoritesStep(cards: List<DrawingCard>, favorites: MutableList<String>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Pick your favorites!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text("Select at least 3 characters you want to draw", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(cards.take(30)) { card ->
                val isSel = card.id in favorites
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, if (isSel) PrimaryBlue else CardBorderLight, RoundedCornerShape(12.dp))
                        .background(if (isSel) PrimaryBlueDim else Color.White)
                        .clickable {
                            if (isSel) favorites.remove(card.id)
                            else if (favorites.size < 10) favorites.add(card.id)
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = if (isSel) PrimaryBlue else TextSecondary
                        )
                        Text(
                            text = card.title.take(12),
                            fontSize = 10.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            color = TextPrimary,
                        )
                    }
                }
            }
        }
        Text("${favorites.size} selected", fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
private fun DailyGoalStep(goal: Int, onChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Daily drawing goal", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("How many drawings do you want to finish each day?", fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(1, 3, 5).forEach { g ->
                val isSel = g == goal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(2.dp, if (isSel) PrimaryBlue else CardBorderLight, RoundedCornerShape(14.dp))
                        .background(if (isSel) PrimaryBlueDim else Color.White)
                        .clickable { onChange(g) }
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text("$g", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = if (isSel) PrimaryBlue else TextPrimary)
                    Text("per day", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}
