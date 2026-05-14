package com.stepbystepdrawing.HowToDrawPoppyPlaytime.data

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GamificationEngine {
    private const val XP_PER_STEP = 10
    private const val XP_PER_DRAWING = 50
    private const val XP_DAILY_BONUS = 30
    private const val XP_STREAK_BONUS = 20

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun xpForLevel(level: Int): Int = level * 100 + (level - 1) * 50

    fun onStepCompleted(ctx: Context): UserProfile {
        val p = load(ctx)
        val newXp = p.xp + XP_PER_STEP
        val newLevel = computeLevel(newXp)
        return p.copy(xp = newXp, level = newLevel).also { save(ctx, it) }
    }

    fun onDrawingCompleted(ctx: Context, drawingId: String): UserProfile {
        var p = load(ctx)
        val td = today()

        // Reset daily counter if new day
        if (p.todayDate != td) {
            p = p.copy(todayDate = td, todayDrawingsCompleted = 0, spinsToday = 0)
        }

        // Update streak
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.US).let { fmt ->
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            fmt.format(cal.time)
        }
        val newStreak = if (p.lastActiveDay == yesterday || p.lastActiveDay == td) p.streak + 1 else 1

        var bonusXp = XP_PER_DRAWING
        if (p.todayDrawingsCompleted == 0) bonusXp += XP_DAILY_BONUS
        if (newStreak > 1) bonusXp += XP_STREAK_BONUS

        val newXp = p.xp + bonusXp
        val newLevel = computeLevel(newXp)
        val newCompleted = p.completedDrawings + drawingId
        val newTotal = p.totalDrawingsCompleted + 1
        val newTodayCount = p.todayDrawingsCompleted + 1

        p = p.copy(
            xp = newXp,
            level = newLevel,
            streak = newStreak,
            lastActiveDay = td,
            completedDrawings = newCompleted,
            totalDrawingsCompleted = newTotal,
            todayDrawingsCompleted = newTodayCount,
        )

        // Check achievements
        p = checkAchievements(p)
        save(ctx, p)
        return p
    }

    fun onSpinUsed(ctx: Context, wonCharacterId: String?): UserProfile {
        var p = load(ctx)
        val td = today()
        if (p.todayDate != td) p = p.copy(todayDate = td, spinsToday = 0)
        p = p.copy(spinsToday = p.spinsToday + 1)
        if (wonCharacterId != null) {
            p = p.copy(unlockedCharacters = p.unlockedCharacters + wonCharacterId)
        }
        save(ctx, p)
        return p
    }

    fun canSpin(ctx: Context): Boolean {
        val p = load(ctx)
        val td = today()
        return if (p.todayDate != td) true else p.spinsToday < 3
    }

    fun isUnlocked(ctx: Context, characterId: String, index: Int): Boolean {
        val p = load(ctx)
        // First 20 always free
        if (index < 20) return true
        return characterId in p.unlockedCharacters
    }

    private fun computeLevel(xp: Int): Int {
        var lvl = 1
        var required = xpForLevel(1)
        var accumulated = 0
        while (accumulated + required <= xp) {
            accumulated += required
            lvl++
            required = xpForLevel(lvl)
        }
        return lvl
    }

    private fun checkAchievements(p: UserProfile): UserProfile {
        val newAch = p.achievements.toMutableSet()
        if (p.totalDrawingsCompleted >= 1) newAch += "first_drawing"
        if (p.totalDrawingsCompleted >= 10) newAch += "ten_drawings"
        if (p.totalDrawingsCompleted >= 50) newAch += "fifty_drawings"
        if (p.streak >= 3) newAch += "streak_3"
        if (p.streak >= 7) newAch += "streak_7"
        if (p.streak >= 30) newAch += "streak_30"
        if (p.level >= 5) newAch += "level_5"
        if (p.level >= 10) newAch += "level_10"
        if (p.completedDrawings.size >= 20) newAch += "complete_20"
        return p.copy(achievements = newAch)
    }

    private fun load(ctx: Context) = UserProfileStore.load(ctx)
    private fun save(ctx: Context, p: UserProfile) = UserProfileStore.save(ctx, p)
}
