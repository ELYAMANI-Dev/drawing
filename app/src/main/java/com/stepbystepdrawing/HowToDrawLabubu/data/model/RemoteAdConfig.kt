package com.stepbystepdrawing.HowToDrawLabubu.data.model

import org.json.JSONObject

// ---------------------------------------------------------------------------
// API keys (mixpanel, onesignal, tmdb, etc.)
// ---------------------------------------------------------------------------

data class RemoteConfigApiKeys(
    val tmdbApiKey: String = "",
    val mixpanelToken: String = "",
    val oneSignalAppId: String = "",
) {
    companion object {
        fun fromJson(json: JSONObject?): RemoteConfigApiKeys {
            if (json == null) return RemoteConfigApiKeys()
            return RemoteConfigApiKeys(
                tmdbApiKey = json.optString("tmdbApiKey", ""),
                mixpanelToken = json.optString("mixpanelToken", ""),
                oneSignalAppId = json.optString("oneSignalAppId", ""),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Per-ad-unit controller (future: frequency caps, A/B tests, etc.)
// ---------------------------------------------------------------------------

data class AdUnitController(
    val raw: Map<String, Any?> = emptyMap(),
) {
    companion object {
        fun fromJson(json: JSONObject?): AdUnitController {
            if (json == null) return AdUnitController()
            val map = mutableMapOf<String, Any?>()
            json.keys().forEach { key -> map[key] = json.opt(key) }
            return AdUnitController(map)
        }
    }
}

// ---------------------------------------------------------------------------
// Single ad unit (new adUnits array format)
// ---------------------------------------------------------------------------

data class AdUnit(
    val id: String,
    val provider: String,
    val unitId: String,
    val enabled: Boolean,
    val placement: String,
    val controller: AdUnitController = AdUnitController(),
) {
    companion object {
        fun fromJson(json: JSONObject): AdUnit = AdUnit(
            id = json.optString("id", ""),
            provider = json.optString("provider", ""),
            unitId = json.optString("unitId", ""),
            enabled = json.optBoolean("enabled", false),
            placement = json.optString("placement", ""),
            controller = AdUnitController.fromJson(json.optJSONObject("controller")),
        )
    }
}

// ---------------------------------------------------------------------------
// Container for all typed ad-unit arrays
// ---------------------------------------------------------------------------

data class AdUnits(
    val banner: List<AdUnit> = emptyList(),
    val interstitial: List<AdUnit> = emptyList(),
    val mrec: List<AdUnit> = emptyList(),
    val native: List<AdUnit> = emptyList(),
    val rewarded: List<AdUnit> = emptyList(),
    val rewardedInterstitial: List<AdUnit> = emptyList(),
    val appOpen: List<AdUnit> = emptyList(),
) {
    val isEmpty: Boolean
        get() = banner.isEmpty() && interstitial.isEmpty() && mrec.isEmpty()
                && native.isEmpty() && rewarded.isEmpty() && rewardedInterstitial.isEmpty()
                && appOpen.isEmpty()

    companion object {
        fun fromJson(json: JSONObject?): AdUnits {
            if (json == null) return AdUnits()
            fun parseArray(key: String): List<AdUnit> {
                val arr = json.optJSONArray(key) ?: return emptyList()
                return (0 until arr.length()).mapNotNull { i ->
                    arr.optJSONObject(i)?.let { AdUnit.fromJson(it) }
                }
            }
            return AdUnits(
                banner = parseArray("banner"),
                interstitial = parseArray("interstitial"),
                mrec = parseArray("mrec"),
                native = parseArray("native"),
                rewarded = parseArray("rewarded"),
                rewardedInterstitial = parseArray("rewarded_interstitial"),
                appOpen = parseArray("app_open"),
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Legacy per-provider config (providers.admob / providers.applovin / …)
// ---------------------------------------------------------------------------

data class AdProviderConfig(
    val enabled: Boolean,
    val bannerUnitId: String = "",
    val interstitialUnitId: String = "",
    val rewardedUnitId: String = "",
    val rewardedInterstitialUnitId: String = "",
    val appOpenUnitId: String = "",
    val adaptiveBannerUnitId: String = "",
    val nativeUnitId: String = "",
    val nativeVideoUnitId: String = "",
) {
    companion object {
        fun fromJson(json: JSONObject): AdProviderConfig = AdProviderConfig(
            enabled = json.optBoolean("enabled", false),
            bannerUnitId = json.optString("bannerUnitId", ""),
            interstitialUnitId = json.optString("interstitialUnitId", ""),
            rewardedUnitId = json.optString("rewardedUnitId", ""),
            rewardedInterstitialUnitId = json.optString("rewardedInterstitialUnitId", ""),
            appOpenUnitId = json.optString("appOpenUnitId", ""),
            adaptiveBannerUnitId = json.optString("adaptiveBannerUnitId", ""),
            nativeUnitId = json.optString("nativeUnitId", ""),
            nativeVideoUnitId = json.optString("nativeVideoUnitId", ""),
        )
    }
}

// ---------------------------------------------------------------------------
// Enums
// ---------------------------------------------------------------------------

enum class BannerPosition { TOP, BOTTOM }

// ---------------------------------------------------------------------------
// Root config
// ---------------------------------------------------------------------------

data class RemoteAdConfig(
    val appName: String,
    val packageName: String,
    val configVersion: Int,
    val adsEnabled: Boolean,
    val globalKillSwitch: Boolean,
    val frequencyCapSeconds: Int,
    val sessionCap: Int,
    val dailyCap: Int,
    val defaultInterstitialProvider: String,
    val defaultRewardedProvider: String,
    val providers: Map<String, AdProviderConfig>,
    val adUnits: AdUnits = AdUnits(),
    val appLovinSdkKey: String? = null,
    val bannerEnabled: Boolean = true,
    val interstitialEnabled: Boolean = true,
    val appOpenEnabled: Boolean = true,
    val bannerPosition: BannerPosition = BannerPosition.BOTTOM,
    val configRefreshIntervalSeconds: Int? = null,
    val countryOverrides: JSONObject? = null,
    val apiKeys: RemoteConfigApiKeys = RemoteConfigApiKeys(),
) {
    val shouldShowAds: Boolean
        get() = adsEnabled && !globalKillSwitch && (!adUnits.isEmpty || providers.isNotEmpty())

    /** True when the new adUnits arrays are present; legacy `providers` is ignored when this is true. */
    val usesAdUnits: Boolean get() = !adUnits.isEmpty

    companion object {
        fun fromJson(json: JSONObject): RemoteAdConfig {
            val providersJson = json.optJSONObject("providers") ?: JSONObject()
            val providers = mutableMapOf<String, AdProviderConfig>()
            providersJson.keys().asSequence().forEach { key ->
                providersJson.optJSONObject(key)?.let { AdProviderConfig.fromJson(it) }?.let { providers[key] = it }
            }
            val bannerPos = when (json.optString("bannerPosition", "bottom")) {
                "top" -> BannerPosition.TOP
                else -> BannerPosition.BOTTOM
            }
            return RemoteAdConfig(
                appName = json.optString("appName", ""),
                packageName = json.optString("packageName", ""),
                configVersion = json.optInt("configVersion", 0),
                adsEnabled = json.optBoolean("adsEnabled", false),
                globalKillSwitch = json.optBoolean("globalKillSwitch", false),
                frequencyCapSeconds = json.optInt("frequencyCapSeconds", 90),
                sessionCap = json.optInt("sessionCap", 3),
                dailyCap = json.optInt("dailyCap", 15),
                defaultInterstitialProvider = json.optString("defaultInterstitialProvider", "admob"),
                defaultRewardedProvider = json.optString("defaultRewardedProvider", "admob"),
                providers = providers,
                adUnits = AdUnits.fromJson(json.optJSONObject("adUnits")),
                appLovinSdkKey = json.optString("appLovinSdkKey").takeIf { it.isNotEmpty() },
                bannerEnabled = json.optBoolean("bannerEnabled", true),
                interstitialEnabled = json.optBoolean("interstitialEnabled", true),
                appOpenEnabled = json.optBoolean("appOpenEnabled", true),
                bannerPosition = bannerPos,
                configRefreshIntervalSeconds = if (json.has("configRefreshIntervalSeconds")) json.optInt("configRefreshIntervalSeconds") else null,
                countryOverrides = json.optJSONObject("countryOverrides"),
                apiKeys = RemoteConfigApiKeys.fromJson(json.optJSONObject("apiKeys")),
            )
        }
    }
}
