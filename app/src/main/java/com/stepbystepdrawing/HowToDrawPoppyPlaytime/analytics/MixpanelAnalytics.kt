package com.stepbystepdrawing.HowToDrawPoppyPlaytime.analytics

import android.content.Context
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.BuildConfig
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.RemoteAdConfigService
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject

/**
 * Analytics wrapper backed by Mixpanel. Token is loaded from remote ad-manager
 * config (apiKeys.mixpanelToken) so it can be rotated without a new release.
 */
object MixpanelAnalytics {

    private var mixpanel: MixpanelAPI? = null
    private var optedOut: Boolean = false

    private const val TRACK_AUTOMATIC_EVENTS = false

    fun init(context: Context) {
        val token = RemoteAdConfigService.getConfig()?.apiKeys?.mixpanelToken?.takeIf { it.isNotBlank() }
            ?: return
        try {
            mixpanel = MixpanelAPI.getInstance(context.applicationContext, token, TRACK_AUTOMATIC_EVENTS)
            registerSuperProperties()
        } catch (_: Exception) {
            mixpanel = null
        }
    }

    private fun registerSuperProperties() {
        val props = JSONObject().apply {
            put("app_version", BuildConfig.VERSION_NAME)
            put("app_version_code", BuildConfig.VERSION_CODE)
        }
        mixpanel?.registerSuperProperties(props)
    }

    fun optOutTracking() {
        optedOut = true
        mixpanel?.optOutTracking()
    }

    fun optInTracking() {
        optedOut = false
        mixpanel?.optInTracking()
    }

    fun isOptedOut(): Boolean = optedOut

    fun track(eventName: String, properties: Map<String, Any?> = emptyMap()) {
        if (optedOut || mixpanel == null) return
        try {
            val props = JSONObject()
            properties.forEach { (k, v) ->
                when (v) {
                    null -> props.put(k, JSONObject.NULL)
                    is String -> props.put(k, v)
                    is Number -> props.put(k, v)
                    is Boolean -> props.put(k, v)
                    else -> props.put(k, v.toString())
                }
            }
            mixpanel?.track(eventName, props)
        } catch (_: Exception) { }
    }

    fun trackScreenView(screenName: String, extra: Map<String, Any?> = emptyMap()) {
        val props = mutableMapOf<String, Any?>("screen_name" to screenName)
        props.putAll(extra)
        track("screen_view", props)
    }

    fun trackAppOpen() {
        track("app_open")
    }

    fun trackLessonOpened(lessonId: String, lessonTitle: String) {
        track("lesson_opened", mapOf("lesson_id" to lessonId, "lesson_title" to lessonTitle))
    }

    fun trackLessonCompleted(lessonId: String, totalSteps: Int) {
        track("lesson_completed", mapOf("lesson_id" to lessonId, "total_steps" to totalSteps))
    }

    fun trackFavoriteToggled(lessonId: String, isFavorite: Boolean) {
        track(
            if (isFavorite) "favorite_added" else "favorite_removed",
            mapOf("lesson_id" to lessonId),
        )
    }

    fun trackShareApp() {
        track("share_app")
    }

    fun trackPlayRandom() {
        track("play_random")
    }

    fun flush() {
        mixpanel?.flush()
    }
}
