package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdService
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdManager
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.SoundManager
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class PreDrawingQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

private val PRE_DRAWING_QUESTIONS = listOf(
    PreDrawingQuestion("What should you draw first?", listOf("Details", "Outline shape", "Colors", "Background"), 1),
    PreDrawingQuestion("Light pencil strokes are used for?", listOf("Final lines", "Sketching guidelines", "Shading", "Erasing"), 1),
    PreDrawingQuestion("Which helps with proportions?", listOf("Grid method", "Random lines", "Thick markers", "Speed"), 0),
    PreDrawingQuestion("What makes a drawing pop?", listOf("Only outlines", "Contrast & shading", "Flat color", "Small size"), 1),
    PreDrawingQuestion("How to fix mistakes?", listOf("Start over", "Erase & adjust", "Ignore them", "Use paint"), 1),
    PreDrawingQuestion("What is symmetry in drawing?", listOf("Random shapes", "Mirror balance", "Dark areas", "Speed technique"), 1),
    PreDrawingQuestion("Best way to improve drawing?", listOf("Never practice", "Practice regularly", "Only watch", "Trace always"), 1),
    PreDrawingQuestion("What is a reference image?", listOf("A blank page", "An image to guide your drawing", "A random photo", "An eraser"), 1),
    PreDrawingQuestion("Why use basic shapes first?", listOf("It's faster", "Builds structure & proportion", "It looks nice", "No reason"), 1),
    PreDrawingQuestion("What's the purpose of an outline?", listOf("Decoration", "Defines the shape boundary", "Add color", "Hide errors"), 1),
)

@Composable
fun PreDrawingQuizScreen(
    characterName: String,
    onPass: () -> Unit,
    onCancel: () -> Unit,
) {
    val activity = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()
    val questions = remember { PRE_DRAWING_QUESTIONS.shuffled().take(3) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var answered by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    if (finished) {
        // Always pass — the quiz is for engagement, not blocking
        LaunchedEffect(Unit) {
            delay(1500)
            if (AdManager.isAdsEnabled) {
                activity?.let { AdService.showInterstitial(it) }
            }
            delay(300)
            onPass()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                if (score >= 2) "✅ Great job!" else "👍 Nice try!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(12.dp))
            Text("$score / ${questions.size} correct", fontSize = 18.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Text("Starting drawing...", fontSize = 14.sp, color = TextSecondary)
        }
        return
    }

    val question = questions[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        // Header
        Text("Quick Quiz!", fontSize = 12.sp, color = TextSecondary)
        Spacer(Modifier.height(4.dp))
        Text(
            "Before drawing: $characterName",
            fontSize = 14.sp,
            color = PrimaryBlue,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(24.dp))

        // Progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            questions.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            if (i <= currentIndex) PrimaryBlue else PrimaryBlueDim,
                            RoundedCornerShape(5.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Question
        Text(
            question.question,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // Options
        question.options.forEachIndexed { index, option ->
            val bgColor = when {
                !answered -> PrimaryBlueDim
                index == question.correctIndex -> Color(0xFFD4EDDA)
                index == selectedAnswer -> Color(0xFFF8D7DA)
                else -> PrimaryBlueDim
            }
            val borderColor = when {
                !answered -> PrimaryBlue
                index == question.correctIndex -> Color(0xFF28A745)
                index == selectedAnswer -> Color(0xFFDC3545)
                else -> PrimaryBlue
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .background(bgColor, RoundedCornerShape(12.dp))
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp))
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
                    .padding(vertical = 14.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    option,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        if (answered) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryBlue, RoundedCornerShape(14.dp))
                    .clickable {
                        if (AdManager.isAdsEnabled) {
                            activity?.let { a -> scope.launch { AdService.showInterstitial(a) } }
                        }
                        if (currentIndex < questions.size - 1) {
                            currentIndex++
                            selectedAnswer = -1
                            answered = false
                        } else {
                            finished = true
                        }
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (currentIndex < questions.size - 1) "Next" else "Start Drawing! 🎨",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Skip option
        Text(
            "Skip quiz",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.clickable { onPass() }
        )
        Spacer(Modifier.height(16.dp))
    }
}
