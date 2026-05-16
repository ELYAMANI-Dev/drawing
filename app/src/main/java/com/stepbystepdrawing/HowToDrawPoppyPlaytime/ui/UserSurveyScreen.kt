package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.*

private const val PREFS_NAME = "user_survey_prefs"
private const val KEY_COMPLETED = "survey_completed"

fun isSurveyCompleted(context: Context): Boolean {
    return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_COMPLETED, false)
}

private fun markSurveyCompleted(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit().putBoolean(KEY_COMPLETED, true).apply()
}

data class SurveyQuestion(
    val question: String,
    val emoji: String,
    val options: List<String>
)

private val SURVEY_QUESTIONS = listOf(
    SurveyQuestion(
        "How old are you?",
        "🎂",
        listOf("Under 13", "13-17", "18-24", "25-34", "35+")
    ),
    SurveyQuestion(
        "What's your gender?",
        "👤",
        listOf("Male", "Female", "Other")
    ),
    SurveyQuestion(
        "What's your drawing level?",
        "🎨",
        listOf("Beginner", "Intermediate", "Advanced")
    ),
    SurveyQuestion(
        "What do you enjoy drawing most?",
        "❤️",
        listOf("Characters", "Animals", "Landscapes", "Abstract", "Everything")
    ),
    SurveyQuestion(
        "How often do you draw?",
        "📅",
        listOf("Daily", "A few times a week", "Weekly", "Rarely")
    ),
)

@Composable
fun UserSurveyScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableIntStateOf(0) }
    val totalQuestions = SURVEY_QUESTIONS.size
    val question = SURVEY_QUESTIONS[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        // Progress
        Text(
            "${currentIndex + 1} / $totalQuestions",
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalQuestions },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = PrimaryBlue,
            trackColor = PrimaryBlueDim,
        )

        Spacer(Modifier.height(48.dp))

        // Emoji
        Text(question.emoji, fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))

        // Question
        Text(
            question.question,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Options
        question.options.forEach { option ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .background(PrimaryBlueDim, RoundedCornerShape(14.dp))
                    .border(2.dp, PrimaryBlue, RoundedCornerShape(14.dp))
                    .clickable {
                        if (currentIndex < totalQuestions - 1) {
                            currentIndex++
                        } else {
                            markSurveyCompleted(context)
                            onComplete()
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Skip
        Text(
            "Skip",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.clickable {
                markSurveyCompleted(context)
                onComplete()
            }
        )
        Spacer(Modifier.height(16.dp))
    }
}
