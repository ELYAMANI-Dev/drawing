package com.stepbystepdrawing.HowToDrawPoppyPlaytime.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object EngagementManager {
    private const val PREFS = "engagement"
    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    // =========== DAILY REWARD ===========

    /** Returns which day the user is on (1-7), resets after 7 or if they miss a day */
    fun getDailyRewardDay(ctx: Context): Int {
        val p = prefs(ctx)
        val lastClaimDate = p.getString("reward_last_claim", "") ?: ""
        val currentDay = p.getInt("reward_day", 1)
        val td = today()

        if (lastClaimDate.isEmpty()) return 1

        val yesterday = getYesterday()
        return when {
            lastClaimDate == td -> currentDay // Already claimed today
            lastClaimDate == yesterday -> currentDay // Can claim next day
            else -> 1 // Missed a day, reset
        }
    }

    fun hasClaimedToday(ctx: Context): Boolean {
        return prefs(ctx).getString("reward_last_claim", "") == today()
    }

    fun claimDailyReward(ctx: Context): Int {
        val p = prefs(ctx)
        val day = getDailyRewardDay(ctx)
        val xpRewards = listOf(20, 30, 40, 50, 75, 100, 200)
        val xp = xpRewards.getOrElse(day - 1) { 20 }

        p.edit().apply {
            putString("reward_last_claim", today())
            putInt("reward_day", if (day >= 7) 1 else day + 1)
            apply()
        }

        // Add XP
        val profile = UserProfileStore.load(ctx)
        UserProfileStore.save(ctx, profile.copy(xp = profile.xp + xp))

        return xp
    }

    fun shouldShowDailyReward(ctx: Context): Boolean {
        return !hasClaimedToday(ctx)
    }

    // =========== DAILY CHALLENGE ===========

    /** Get today's challenge drawing index (deterministic per day) */
    fun getDailyChallengeIndex(totalCards: Int): Int {
        if (totalCards <= 0) return 0
        val dayHash = today().hashCode()
        return Math.abs(dayHash) % totalCards
    }

    fun isDailyChallengeCompleted(ctx: Context): Boolean {
        return prefs(ctx).getString("challenge_completed_date", "") == today()
    }

    fun completeDailyChallenge(ctx: Context) {
        prefs(ctx).edit().putString("challenge_completed_date", today()).apply()
        // 3x bonus = extra 100 XP on top of normal completion
        val profile = UserProfileStore.load(ctx)
        UserProfileStore.save(ctx, profile.copy(xp = profile.xp + 100))
    }

    // =========== LUCKY POPUP ===========

    /** 1 in 3 chance on app open, max once per day */
    fun shouldShowLuckyPopup(ctx: Context): Boolean {
        val p = prefs(ctx)
        val lastLucky = p.getString("lucky_date", "") ?: ""
        if (lastLucky == today()) return false
        return Random.nextInt(3) == 0
    }

    fun markLuckyShown(ctx: Context) {
        prefs(ctx).edit().putString("lucky_date", today()).apply()
    }

    fun generateLuckyReward(): Triple<String, String, Int> {
        // Returns (title, description, xpAmount)
        return when (Random.nextInt(3)) {
            0 -> Triple("+50 Bonus XP!", "A surprise gift just for opening the app!", 50)
            1 -> Triple("Free Spin!", "You got a free spin on the wheel!", 0)
            else -> Triple("+30 XP Boost!", "Keep coming back for more surprises!", 30)
        }
    }

    fun claimLuckyReward(ctx: Context, xp: Int) {
        if (xp > 0) {
            val profile = UserProfileStore.load(ctx)
            UserProfileStore.save(ctx, profile.copy(xp = profile.xp + xp))
        }
    }

    // =========== STREAK NOTIFICATION ===========

    /** Schedule data for notification (to be used with OneSignal) */
    fun getStreakNotificationData(ctx: Context): Map<String, String>? {
        val profile = UserProfileStore.load(ctx)
        if (profile.streak < 2) return null
        return mapOf(
            "title" to "Don't lose your streak!",
            "message" to "You have a ${profile.streak}-day streak! Draw something today to keep it going!",
            "streak" to profile.streak.toString(),
        )
    }

    // =========== HINT TRACKING ===========

    fun getHintsUsedToday(ctx: Context): Int {
        val p = prefs(ctx)
        val hintDate = p.getString("hint_date", "") ?: ""
        if (hintDate != today()) return 0
        return p.getInt("hints_used", 0)
    }

    fun useHint(ctx: Context) {
        val p = prefs(ctx)
        val hintDate = p.getString("hint_date", "") ?: ""
        val current = if (hintDate == today()) p.getInt("hints_used", 0) else 0
        p.edit().apply {
            putString("hint_date", today())
            putInt("hints_used", current + 1)
            apply()
        }
    }

    fun canUseHint(ctx: Context): Boolean = getHintsUsedToday(ctx) < 5

    private fun getYesterday(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }
}
