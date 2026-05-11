package com.stepbystepdrawing.HowToDrawLabubu.data

import org.json.JSONArray
import org.json.JSONObject

/** Drives which headline/copy the error screen shows (not everything is “no internet”). */
enum class UnavailableKind {
    /** System reports no usable network — show “no internet” UI. */
    NO_DEVICE_INTERNET,

    /** HTTP/IO failure that usually means network or DNS. */
    NETWORK_OR_TRANSPORT,

    /** HTTP error, empty body, empty catalog, bad JSON — not the same as offline. */
    CONTENT_OR_SERVER,
}

object DrawingSession {
    sealed interface State {
        data class Ready(
            val appTitle: String,
            val cards: List<DrawingCard>,
            val details: Map<String, DrawingDetails>,
        ) : State

        data class Unavailable(
            val message: String,
            val kind: UnavailableKind = UnavailableKind.CONTENT_OR_SERVER,
        ) : State
    }

    @Volatile
    private var state: State? = null

    fun setReady(
        appTitle: String,
        cards: List<DrawingCard>,
        details: Map<String, DrawingDetails>,
    ) {
        state = State.Ready(appTitle, cards, details)
    }

    fun setUnavailable(message: String, kind: UnavailableKind = UnavailableKind.CONTENT_OR_SERVER) {
        state = State.Unavailable(message, kind)
    }

    fun getState(): State = state
        ?: State.Unavailable("Content not loaded", UnavailableKind.CONTENT_OR_SERVER)

    /** Null only before Splash has finished loading (or after [reset]). */
    fun peekState(): State? = state

    fun reset() {
        state = null
    }
}

fun parsePublicAppJson(json: String): Triple<String, List<DrawingCard>, Map<String, DrawingDetails>> {
    val trimmed = json.trim().removePrefix("\uFEFF")
    if (trimmed.isEmpty()) {
        return Triple("Drawing", emptyList(), emptyMap())
    }
    val probe = trimmed.firstOrNull()
    if (probe != '{' && probe != '[') {
        throw IllegalStateException("Response was not JSON (likely a proxy or block page).")
    }
    val root = JSONObject(trimmed)
    val appTitle = root.optString("title").ifBlank { root.optString("id", "Drawing") }
    val characters = root.optJSONArray("characters")
        ?: root.optJSONObject("data")?.optJSONArray("characters")
        ?: root.optJSONArray("lessons")
        ?: JSONArray()
    val cards = mutableListOf<DrawingCard>()
    val details = mutableMapOf<String, DrawingDetails>()
    for (i in 0 until characters.length()) {
        val c = characters.optJSONObject(i) ?: continue
        val id = c.optString("id")
        if (id.isBlank()) continue
        val title = c.optString("title", id).ifBlank { id }
        val thumbnailUrl = c.optString("thumbnailUrl")
        val totalSteps = c.optInt("totalSteps", 0)
        cards += DrawingCard(
            id = id,
            title = title,
            thumbnailUrl = thumbnailUrl,
            totalSteps = totalSteps
        )
        val stepsJson = c.optJSONArray("steps") ?: continue
        val steps = mutableListOf<DrawingStep>()
        for (j in 0 until stepsJson.length()) {
            val s = stepsJson.optJSONObject(j) ?: continue
            val stepNum = s.optInt("step", j + 1)
            val imageUrl = s.optString("imageUrl")
            if (imageUrl.isBlank()) continue
            steps += DrawingStep(step = stepNum, imageUrl = imageUrl)
        }
        details[id] = DrawingDetails(id = id, title = title, steps = steps)
    }
    return Triple(appTitle, cards, details)
}
