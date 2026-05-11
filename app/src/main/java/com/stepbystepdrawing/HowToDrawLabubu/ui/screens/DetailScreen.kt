package com.stepbystepdrawing.HowToDrawLabubu.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.stepbystepdrawing.HowToDrawLabubu.data.DrawingDetails
import com.stepbystepdrawing.HowToDrawLabubu.data.UiState
import com.stepbystepdrawing.HowToDrawLabubu.ui.components.CenteredLoading
import com.stepbystepdrawing.HowToDrawLabubu.ui.components.CenteredMessage
import com.stepbystepdrawing.HowToDrawLabubu.ui.components.TopIconRow

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun DetailScreen(
    detailState: UiState<DrawingDetails>,
    currentStep: Int,
    showResultFirst: Boolean,
    isFavorite: Boolean,
    onBackToGallery: () -> Unit,
    onBackStep: () -> Unit,
    onNextStep: () -> Unit,
    onToggleFavorite: () -> Unit,
    onStartSteps: () -> Unit,
    onGoToPreview: () -> Unit,
    onCompleteLesson: () -> Unit,
) {
    when (detailState) {
        is UiState.Loading -> Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            CenteredLoading("Loading steps…")
        }
        is UiState.Error   -> {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopIconRow(
                    showBack = true,
                    onBack = onBackToGallery,
                    showTrailingActions = false
                )
                CenteredMessage(detailState.message)
            }
        }
        is UiState.Success -> {
            val drawing = detailState.data

            if (drawing.steps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    CenteredMessage("No steps available.")
                }
                return
            }

            AnimatedContent(
                targetState = showResultFirst,
                transitionSpec = {
                    if (targetState)
                        slideInHorizontally(tween(240)) { -it } togetherWith slideOutHorizontally(tween(240)) { it }
                    else
                        slideInHorizontally(tween(240)) { it } togetherWith slideOutHorizontally(tween(240)) { -it }
                },
                label = "screen"
            ) { isPreview ->
                if (isPreview) {
                    PreviewScreen(
                        previewImageUrl = drawing.steps.last().imageUrl,
                        totalStages = drawing.steps.size,
                        isFavorite = isFavorite,
                        onBackToGallery = onBackToGallery,
                        onToggleFavorite = onToggleFavorite,
                        onStartSteps = onStartSteps
                    )
                } else {
                    PlayerScreen(
                        stepImageUrl = drawing.steps[currentStep].imageUrl,
                        currentStep = currentStep,
                        totalSteps = drawing.steps.size,
                        onBackToGallery = onBackToGallery,
                        onBackStep = onBackStep,
                        onNextStep = onNextStep,
                        onGoToPreview = onGoToPreview,
                        onCompleteLesson = onCompleteLesson,
                    )
                }
            }
        }
    }
}
