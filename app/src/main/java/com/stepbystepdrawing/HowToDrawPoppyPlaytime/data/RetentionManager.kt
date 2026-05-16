package com.stepbystepdrawing.HowToDrawPoppyPlaytime.data

import android.content.Context
import android.content.SharedPreferences
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Manages all retention/engagement features:
 * - Quiz cooldown (3h between attempts)
 * - Daily streak (consecutive days using the app)
 * - Spin wheel (1x per day)
 * - Offline comeback bonus
 * - Navigation ad counter (every 4th nav)
 * - Weekly challenge (complete N lessons)
 */
class RetentionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("retention_data", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_QUIZ_LAST_ATTEMPT = "quiz_last_attempt"
        private const val KEY_STREAK_COUNT = "streak_count"
        private const val KEY_STREAK_LAST_DATE = "streak_last_date"
        private const val KEY_STREAK_CLAIMED_TODAY = "streak_claimed_today"
        private const val KEY_SPIN_LAST_DATE = "spin_last_date"
        private const val KEY_SPIN_USED = "spin_used_today"
        private const val KEY_LAST_SESSION_END = "last_session_end"
        private const val KEY_NAV_COUNT = "nav_ad_count"
        private const val KEY_WEEKLY_LESSONS = "weekly_lessons_completed"
        private const val KEY_WEEKLY_START = "weekly_start_date"
        private const val KEY_TOTAL_STARS = "total_stars"
        private const val KEY_RATE_SHOWN = "rate_us_shown"

        private const val QUIZ_COOLDOWN_MS = 3 * 60 * 60 * 1000L // 3 hours
        private const val NAV_AD_INTERVAL = 4
        private const val WEEKLY_TARGET = 5
    }

    // ─── Quiz Cooldown ───────────────────────────────────────────────

    fun canPlayQuiz(): Boolean {
        val last = prefs.getLong(KEY_QUIZ_LAST_ATTEMPT, 0L)
        return System.currentTimeMillis() - last >= QUIZ_COOLDOWN_MS
    }

    fun getQuizCooldownRemainingMs(): Long {
        val last = prefs.getLong(KEY_QUIZ_LAST_ATTEMPT, 0L)
        val remaining = QUIZ_COOLDOWN_MS - (System.currentTimeMillis() - last)
        return if (remaining > 0) remaining else 0L
    }

    fun recordQuizAttempt() {
        prefs.edit().putLong(KEY_QUIZ_LAST_ATTEMPT, System.currentTimeMillis()).apply()
    }

    // ─── Daily Streak ────────────────────────────────────────────────

    fun getDailyStreak(): Int = prefs.getInt(KEY_STREAK_COUNT, 0)

    fun checkAndUpdateStreak(): Int {
        val today = LocalDate.now().toString()
        val lastDate = prefs.getString(KEY_STREAK_LAST_DATE, "") ?: ""

        return when {
            lastDate == today -> prefs.getInt(KEY_STREAK_COUNT, 0) // Already counted today
            lastDate == LocalDate.now().minusDays(1).toString() -> {
                // Consecutive day
                val newStreak = prefs.getInt(KEY_STREAK_COUNT, 0) + 1
                prefs.edit()
                    .putInt(KEY_STREAK_COUNT, newStreak)
                    .putString(KEY_STREAK_LAST_DATE, today)
                    .putBoolean(KEY_STREAK_CLAIMED_TODAY, false)
                    .apply()
                newStreak
            }
            else -> {
                // Streak broken, start fresh
                prefs.edit()
                    .putInt(KEY_STREAK_COUNT, 1)
                    .putString(KEY_STREAK_LAST_DATE, today)
                    .putBoolean(KEY_STREAK_CLAIMED_TODAY, false)
                    .apply()
                1
            }
        }
    }

    fun isStreakClaimedToday(): Boolean = prefs.getBoolean(KEY_STREAK_CLAIMED_TODAY, false)

    fun claimStreakBonus() {
        prefs.edit().putBoolean(KEY_STREAK_CLAIMED_TODAY, true).apply()
    }

    /** Stars bonus: day 3→1, day 5→2, day 7→3, day 10+→5 */
    fun getStreakBonus(streak: Int): Int = when {
        streak >= 10 -> 5
        streak >= 7 -> 3
        streak >= 5 -> 2
        streak >= 3 -> 1
        else -> 0
    }

    // ─── Spin Wheel ──────────────────────────────────────────────────

    fun canSpin(): Boolean {
        val today = LocalDate.now().toString()
        val lastSpinDate = prefs.getString(KEY_SPIN_LAST_DATE, "") ?: ""
        return lastSpinDate != today
    }

    fun recordSpin() {
        prefs.edit()
            .putString(KEY_SPIN_LAST_DATE, LocalDate.now().toString())
            .putBoolean(KEY_SPIN_USED, true)
            .apply()
    }

    // ─── Offline Comeback ────────────────────────────────────────────

    fun recordSessionEnd() {
        prefs.edit().putLong(KEY_LAST_SESSION_END, System.currentTimeMillis()).apply()
    }

    /** Returns bonus stars based on hours away: 3h→1, 6h→2, 12h→3, 24h+→5 */
    fun getOfflineComebackBonus(): Int {
        val lastEnd = prefs.getLong(KEY_LAST_SESSION_END, 0L)
        if (lastEnd == 0L) return 0
        val hoursAway = (System.currentTimeMillis() - lastEnd) / (1000 * 60 * 60)
        return when {
            hoursAway >= 24 -> 5
            hoursAway >= 12 -> 3
            hoursAway >= 6 -> 2
            hoursAway >= 3 -> 1
            else -> 0
        }
    }

    fun clearComebackBonus() {
        prefs.edit().putLong(KEY_LAST_SESSION_END, System.currentTimeMillis()).apply()
    }

    // ─── Navigation Ad Counter ───────────────────────────────────────

    fun incrementNavAndCheckAd(): Boolean {
        val count = prefs.getInt(KEY_NAV_COUNT, 0) + 1
        prefs.edit().putInt(KEY_NAV_COUNT, count).apply()
        return count % NAV_AD_INTERVAL == 0
    }

    // ─── Weekly Challenge ────────────────────────────────────────────

    fun getWeeklyProgress(): Pair<Int, Int> {
        refreshWeeklyIfNeeded()
        return prefs.getInt(KEY_WEEKLY_LESSONS, 0) to WEEKLY_TARGET
    }

    fun recordLessonCompleted() {
        refreshWeeklyIfNeeded()
        val current = prefs.getInt(KEY_WEEKLY_LESSONS, 0) + 1
        prefs.edit().putInt(KEY_WEEKLY_LESSONS, current).apply()
    }

    private fun refreshWeeklyIfNeeded() {
        val today = LocalDate.now()
        val startStr = prefs.getString(KEY_WEEKLY_START, "") ?: ""
        if (startStr.isBlank() || LocalDate.parse(startStr).plusDays(7) <= today) {
            prefs.edit()
                .putString(KEY_WEEKLY_START, today.toString())
                .putInt(KEY_WEEKLY_LESSONS, 0)
                .apply()
        }
    }

    // ─── Stars (virtual currency) ────────────────────────────────────

    fun getTotalStars(): Int = prefs.getInt(KEY_TOTAL_STARS, 0)

    fun addStars(amount: Int) {
        val newTotal = prefs.getInt(KEY_TOTAL_STARS, 0) + amount
        prefs.edit().putInt(KEY_TOTAL_STARS, newTotal).apply()
    }

    // ─── Rate Us ─────────────────────────────────────────────────────

    fun shouldShowRateUs(): Boolean {
        if (prefs.getBoolean(KEY_RATE_SHOWN, false)) return false
        return getDailyStreak() >= 3
    }

    fun markRateUsShown() {
        prefs.edit().putBoolean(KEY_RATE_SHOWN, true).apply()
    }
}
