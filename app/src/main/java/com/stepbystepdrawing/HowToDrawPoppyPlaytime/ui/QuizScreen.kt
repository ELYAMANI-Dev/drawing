package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.RetentionManager
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdManager
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdService
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.SoundManager
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.*
import kotlinx.coroutines.launch

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

private val DRAWING_QUIZ_QUESTIONS = listOf(
    QuizQuestion("What is the first step in drawing a character?", listOf("Coloring", "Basic shapes", "Details", "Shading"), 1),
    QuizQuestion("Which tool is best for sketching?", listOf("Marker", "Pencil", "Brush", "Crayon"), 1),
    QuizQuestion("What does 'proportion' mean in drawing?", listOf("Color choice", "Size relationships", "Line weight", "Shading"), 1),
    QuizQuestion("Which shape is a head usually based on?", listOf("Triangle", "Square", "Circle/Oval", "Rectangle"), 2),
    QuizQuestion("What is hatching in drawing?", listOf("A type of paint", "Parallel lines for shading", "A brush type", "Erasing"), 1),
    QuizQuestion("What is the vanishing point used for?", listOf("Coloring", "Perspective", "Outlining", "Symmetry"), 1),
    QuizQuestion("Which pencil is softer: 2B or 2H?", listOf("2H", "2B", "Same", "Neither"), 1),
    QuizQuestion("What is a silhouette?", listOf("A detailed sketch", "An outline filled with solid color", "A type of paint", "A shading technique"), 1),
    QuizQuestion("How many basic shapes are used in character drawing?", listOf("1", "2", "3-5", "More than 10"), 2),
    QuizQuestion("What helps make a drawing look 3D?", listOf("Flat colors", "Shading and highlights", "Thin lines", "Small size"), 1),
    QuizQuestion("What is a gesture drawing?", listOf("A slow detailed sketch", "A quick pose capture", "A type of painting", "Drawing hands"), 1),
    QuizQuestion("What does 'foreshortening' mean?", listOf("Making things bigger", "Perspective distortion of length", "Adding colors", "Drawing lines"), 1),
    QuizQuestion("Which is the lightest pencil grade?", listOf("8B", "HB", "6H", "4B"), 2),
    QuizQuestion("What is cross-hatching?", listOf("Drawing X shapes", "Overlapping hatch lines", "A painting style", "Using a grid"), 1),
    QuizQuestion("What is the horizon line?", listOf("Top of the page", "Where sky meets ground", "A vertical line", "The darkest area"), 1),
)

@Composable
fun QuizScreen(
    retentionManager: RetentionManager,
    onClose: () -> Unit,
    onQuizComplete: (score: Int, total: Int) -> Unit,
) {
    val activity = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()
    val questions = remember { DRAWING_QUIZ_QUESTIONS.shuffled().take(8) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var answered by remember { mutableStateOf(false) }
    var quizFinished by remember { mutableStateOf(false) }

    if (quizFinished) {
        // Results screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎉 Quiz Complete!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "$score / ${questions.size}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (score >= questions.size * 0.7) "Great job! 🌟" else "Keep practicing! 🎨",
                fontSize = 18.sp,
                color = TextSecondary
            )
            Spacer(Modifier.height(8.dp))
            val starsEarned = score // 1 star per correct answer
            Text(
                text = "+$starsEarned ⭐ earned",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B)
            )
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlueDim, RoundedCornerShape(14.dp))
                    .border(2.dp, PrimaryBlue, RoundedCornerShape(14.dp))
                    .clickable {
                        retentionManager.recordQuizAttempt()
                        retentionManager.addStars(starsEarned)
                        onQuizComplete(score, questions.size)
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Collect & Close", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
        return
    }

    val question = questions[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🎨 Drawing Quiz", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                "⭐ $score",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / questions.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = PrimaryBlue,
            trackColor = PrimaryBlueDim,
        )

        Spacer(Modifier.height(4.dp))
        Text(
            "Question ${currentIndex + 1} of ${questions.size}",
            fontSize = 13.sp,
            color = TextMuted
        )

        Spacer(Modifier.height(24.dp))

        // Question
        Text(
            text = question.question,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // Options
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(question.options) { index, option ->
                val bgColor = when {
                    !answered -> if (index == selectedAnswer) PrimaryBlueDim else Color.White
                    index == question.correctIndex -> Color(0xFFD1FAE5)
                    index == selectedAnswer -> Color(0xFFFEE2E2)
                    else -> Color.White
                }
                val borderColor = when {
                    !answered -> if (index == selectedAnswer) PrimaryBlue else CardBorderLight
                    index == question.correctIndex -> Color(0xFF10B981)
                    index == selectedAnswer -> Color(0xFFEF4444)
                    else -> CardBorderLight
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor, RoundedCornerShape(14.dp))
                        .border(2.dp, borderColor, RoundedCornerShape(14.dp))
                        .clickable(enabled = !answered) {
                            selectedAnswer = index
                            answered = true
                            if (index == question.correctIndex) {
                                score++
                                SoundManager.playCorrect()
                            } else {
                                SoundManager.playWrong()
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = option,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Next / Finish button
        if (answered) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlueDim, RoundedCornerShape(14.dp))
                    .border(2.dp, PrimaryBlue, RoundedCornerShape(14.dp))
                    .clickable {
                        // Show ad every 3 questions
                        scope.launch {
                            if (AdManager.isAdsEnabled && (currentIndex + 1) % 3 == 0 && activity != null) {
                                AdService.showInterstitial(activity)
                            }
                            if (currentIndex < questions.size - 1) {
                                currentIndex++
                                selectedAnswer = -1
                                answered = false
                            } else {
                                // Show completion ad
                                if (AdManager.isAdsEnabled && activity != null) {
                                    AdService.showInterstitial(activity)
                                }
                                quizFinished = true
                            }
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentIndex < questions.size - 1) "Next →" else "See Results",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }
    }
}
