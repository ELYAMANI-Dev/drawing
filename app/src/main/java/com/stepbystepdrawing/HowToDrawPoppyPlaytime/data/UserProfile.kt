package com.stepbystepdrawing.HowToDrawPoppyPlaytime.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

enum class Gender { BOY, GIRL, OTHER }
enum class SkillLevel { BEGINNER, INTERMEDIATE, PRO }

data class UserProfile(
    val name: String = "",
    val gender: Gender = Gender.OTHER,
    val age: Int = 0,
    val skillLevel: SkillLevel = SkillLevel.BEGINNER,
    val favorites: List<String> = emptyList(),
    val dailyGoal: Int = 3,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastActiveDay: String = "",
    val completedDrawings: Set<String> = emptySet(),
    val unlockedCharacters: Set<String> = emptySet(),
    val achievements: Set<String> = emptySet(),
    val totalDrawingsCompleted: Int = 0,
    val todayDrawingsCompleted: Int = 0,
    val todayDate: String = "",
    val spinsToday: Int = 0,
)

object UserProfileStore {
    private const val PREFS_NAME = "user_profile"
    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOnboarded(ctx: Context): Boolean = prefs(ctx).getBoolean("onboarded", false)

    fun setOnboarded(ctx: Context) = prefs(ctx).edit().putBoolean("onboarded", true).apply()

    fun save(ctx: Context, profile: UserProfile) {
        prefs(ctx).edit().apply {
            putString("name", profile.name)
            putString("gender", profile.gender.name)
            putInt("age", profile.age)
            putString("skillLevel", profile.skillLevel.name)
            putString("favorites", JSONArray(profile.favorites).toString())
            putInt("dailyGoal", profile.dailyGoal)
            putInt("xp", profile.xp)
            putInt("level", profile.level)
            putInt("streak", profile.streak)
            putString("lastActiveDay", profile.lastActiveDay)
            putString("completedDrawings", JSONArray(profile.completedDrawings.toList()).toString())
            putString("unlockedCharacters", JSONArray(profile.unlockedCharacters.toList()).toString())
            putString("achievements", JSONArray(profile.achievements.toList()).toString())
            putInt("totalDrawingsCompleted", profile.totalDrawingsCompleted)
            putInt("todayDrawingsCompleted", profile.todayDrawingsCompleted)
            putString("todayDate", profile.todayDate)
            putInt("spinsToday", profile.spinsToday)
            apply()
        }
    }

    fun load(ctx: Context): UserProfile {
        val p = prefs(ctx)
        return UserProfile(
            name = p.getString("name", "") ?: "",
            gender = runCatching { Gender.valueOf(p.getString("gender", "OTHER") ?: "OTHER") }.getOrDefault(Gender.OTHER),
            age = p.getInt("age", 0),
            skillLevel = runCatching { SkillLevel.valueOf(p.getString("skillLevel", "BEGINNER") ?: "BEGINNER") }.getOrDefault(SkillLevel.BEGINNER),
            favorites = runCatching { val arr = JSONArray(p.getString("favorites", "[]")); (0 until arr.length()).map { arr.getString(it) } }.getOrDefault(emptyList()),
            dailyGoal = p.getInt("dailyGoal", 3),
            xp = p.getInt("xp", 0),
            level = p.getInt("level", 1),
            streak = p.getInt("streak", 0),
            lastActiveDay = p.getString("lastActiveDay", "") ?: "",
            completedDrawings = runCatching { val arr = JSONArray(p.getString("completedDrawings", "[]")); (0 until arr.length()).map { arr.getString(it) }.toSet() }.getOrDefault(emptySet()),
            unlockedCharacters = runCatching { val arr = JSONArray(p.getString("unlockedCharacters", "[]")); (0 until arr.length()).map { arr.getString(it) }.toSet() }.getOrDefault(emptySet()),
            achievements = runCatching { val arr = JSONArray(p.getString("achievements", "[]")); (0 until arr.length()).map { arr.getString(it) }.toSet() }.getOrDefault(emptySet()),
            totalDrawingsCompleted = p.getInt("totalDrawingsCompleted", 0),
            todayDrawingsCompleted = p.getInt("todayDrawingsCompleted", 0),
            todayDate = p.getString("todayDate", "") ?: "",
            spinsToday = p.getInt("spinsToday", 0),
        )
    }
}
