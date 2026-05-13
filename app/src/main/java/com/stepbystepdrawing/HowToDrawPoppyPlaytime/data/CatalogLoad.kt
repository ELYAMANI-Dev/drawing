package com.stepbystepdrawing.HowToDrawPoppyPlaytime.data

import android.content.Context
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.R

internal sealed class CatalogLoad {
    data object Success : CatalogLoad()
    data object Empty : CatalogLoad()
    data class Error(val message: String) : CatalogLoad()
}

/**
 * Parses JSON and updates [DrawingSession]. Does not set [DrawingSession] on [CatalogLoad.Empty].
 */
internal fun loadCatalogFromJson(context: Context, json: String): CatalogLoad {
    return try {
        val (appTitle, cards, details) = parsePublicAppJson(json)
        if (cards.isEmpty()) {
            CatalogLoad.Empty
        } else {
            DrawingSession.setReady(
                appTitle = appTitle,
                cards = cards,
                details = details,
            )
            CatalogLoad.Success
        }
    } catch (e: Exception) {
        CatalogLoad.Error(
            e.message ?: context.getString(R.string.splash_error_generic),
        )
    }
}
