package com.stepbystepdrawing.HowToDrawLabubu.data

import android.content.Context
import android.content.SharedPreferences

class FavoritesStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): Set<String> =
        prefs.getStringSet(KEY_IDS, null)?.let { HashSet(it) } ?: emptySet()

    fun toggle(id: String): Set<String> {
        val next = load().toMutableSet()
        if (!next.add(id)) next.remove(id)
        prefs.edit().putStringSet(KEY_IDS, HashSet(next)).apply()
        return next
    }

    companion object {
        private const val PREFS_NAME = "drawingsteps_favorites"
        private const val KEY_IDS = "favorite_drawing_ids"
    }
}
