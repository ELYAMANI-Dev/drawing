package com.stepbystepdrawing.HowToDrawPoppyPlaytime.data

data class DrawingCard(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val totalSteps: Int
)

/** Filter gallery by approximate lesson length (step count). */
enum class DrawingDifficultyFilter(val contentDescription: String) {
    All("All lessons"),
    Easy("Easy difficulty"),
    Normal("Normal difficulty"),
    Hard("Hard difficulty");

    fun matchesStepCount(totalSteps: Int): Boolean = when (this) {
        All -> true
        Easy -> totalSteps <= 20
        Normal -> totalSteps in 21..40
        Hard -> totalSteps >= 41
    }
}

fun DrawingCard.matchesDifficulty(filter: DrawingDifficultyFilter): Boolean =
    filter.matchesStepCount(totalSteps)

data class DrawingDetails(
    val id: String,
    val title: String,
    val steps: List<DrawingStep>
)

data class DrawingStep(
    val step: Int,
    val imageUrl: String
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

fun cleanTitle(raw: String): String {
    val tokens = raw.trim().split(" ").filter { it.isNotBlank() }
    if (tokens.isEmpty()) return "Drawing"
    val pruned = if (tokens.last().matches(Regex("[a-z0-9]{8,}", RegexOption.IGNORE_CASE)))
        tokens.dropLast(1) else tokens
    return pruned.joinToString(" ").replaceFirstChar { it.uppercase() }
}
