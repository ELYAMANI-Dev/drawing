package com.stepbystepdrawing.HowToDrawLabubu.services

import android.util.Log
import com.stepbystepdrawing.HowToDrawLabubu.data.model.AdProviderConfig
import com.stepbystepdrawing.HowToDrawLabubu.data.model.AdUnit
import com.stepbystepdrawing.HowToDrawLabubu.data.model.RemoteAdConfig

/**
 * Central ad manager driven by the remote JSON config.
 *
 * **`adUnits` (preferred):** multiple units per format (`interstitial`, `rewarded_interstitial`,
 * `native`, `app_open`, …), each with `provider`, `unitId`, `placement`, `enabled`.
 * When any `adUnits` bucket is non-empty, [usesAdUnits] is true and **only** `adUnits` are
 * used — the legacy `providers` map is ignored for resolving units.
 *
 * **Legacy `providers` map:** used only when `adUnits` is completely empty (older JSON).
 */
object AdManager {
    private const val TAG = "AdManager"

    @Volatile
    private var config: RemoteAdConfig? = null

    fun init(adConfig: RemoteAdConfig?) {
        config = adConfig
        if (adConfig == null) {
            Log.w(TAG, "init() called with null config — ads disabled")
            return
        }
        Log.d(TAG, "init() config v${adConfig.configVersion}, " +
                "adsEnabled=${adConfig.adsEnabled}, usesAdUnits=${adConfig.usesAdUnits}")
    }

    fun getConfig(): RemoteAdConfig? = config

    val isAdsEnabled: Boolean get() = config?.shouldShowAds == true

    // ------------------------------------------------------------------
    // Resolved ad unit — unified type returned by all getters
    // ------------------------------------------------------------------

    data class ResolvedAdUnit(
        val id: String,
        val provider: String,
        val unitId: String,
        val placement: String,
    )

    // ------------------------------------------------------------------
    // Banner
    // ------------------------------------------------------------------

    fun getBannerUnits(): List<ResolvedAdUnit> = resolveUnits(
        typeEnabled = config?.bannerEnabled != false,
        adUnitsList = { it.adUnits.banner },
        legacyFallback = { cfg -> legacyBannerUnit(cfg) },
    )

    fun getFirstBanner(): ResolvedAdUnit? = getBannerUnits().firstOrNull()

    fun getBannerByPlacement(placement: String): ResolvedAdUnit? =
        getBannerUnits().firstOrNull { it.placement == placement }

    // ------------------------------------------------------------------
    // Interstitial
    // ------------------------------------------------------------------

    fun getInterstitialUnits(): List<ResolvedAdUnit> = resolveUnits(
        typeEnabled = config?.interstitialEnabled != false,
        adUnitsList = { it.adUnits.interstitial },
        legacyFallback = { cfg -> legacyInterstitialUnit(cfg) },
    )

    fun getFirstInterstitial(): ResolvedAdUnit? = getInterstitialUnits().firstOrNull()

    fun getInterstitialByPlacement(placement: String): ResolvedAdUnit? =
        getInterstitialUnits().firstOrNull { it.placement == placement }

    // ------------------------------------------------------------------
    // Rewarded
    // ------------------------------------------------------------------

    fun getRewardedUnits(): List<ResolvedAdUnit> = resolveUnits(
        typeEnabled = true,
        adUnitsList = { it.adUnits.rewarded },
        legacyFallback = { cfg -> legacyRewardedUnit(cfg) },
    )

    fun getFirstRewarded(): ResolvedAdUnit? = getRewardedUnits().firstOrNull()

    fun getRewardedByPlacement(placement: String): ResolvedAdUnit? =
        getRewardedUnits().firstOrNull { it.placement == placement }

    // ------------------------------------------------------------------
    // Rewarded interstitial (distinct from rewarded video in AdMob)
    // ------------------------------------------------------------------

    fun getRewardedInterstitialUnits(): List<ResolvedAdUnit> = resolveUnits(
        typeEnabled = true,
        adUnitsList = { it.adUnits.rewardedInterstitial },
        legacyFallback = { cfg -> legacyRewardedInterstitialOnly(cfg) },
    )

    fun getFirstRewardedInterstitial(): ResolvedAdUnit? = getRewardedInterstitialUnits().firstOrNull()

    fun getRewardedInterstitialByPlacement(placement: String): ResolvedAdUnit? =
        getRewardedInterstitialUnits().firstOrNull { it.placement == placement }

    // ------------------------------------------------------------------
    // App Open
    // ------------------------------------------------------------------

    fun getAppOpenUnits(): List<ResolvedAdUnit> = resolveUnits(
        typeEnabled = config?.appOpenEnabled != false,
        adUnitsList = { it.adUnits.appOpen },
        legacyFallback = { cfg -> legacyAppOpenUnit(cfg) },
    )

    fun getFirstAppOpen(): ResolvedAdUnit? = getAppOpenUnits().firstOrNull()

    // ------------------------------------------------------------------
    // MREC (medium rectangle)
    // ------------------------------------------------------------------

    fun getMrecUnits(): List<ResolvedAdUnit> = resolveUnits(
        typeEnabled = true,
        adUnitsList = { it.adUnits.mrec },
        legacyFallback = { emptyList() },
    )

    fun getFirstMrec(): ResolvedAdUnit? = getMrecUnits().firstOrNull()

    fun getMrecByPlacement(placement: String): ResolvedAdUnit? =
        getMrecUnits().firstOrNull { it.placement == placement }

    // ------------------------------------------------------------------
    // Native
    // ------------------------------------------------------------------

    fun getNativeUnits(): List<ResolvedAdUnit> = resolveUnits(
        typeEnabled = true,
        adUnitsList = { it.adUnits.native },
        legacyFallback = { cfg -> legacyNativeUnit(cfg) },
    )

    fun getFirstNative(): ResolvedAdUnit? = getNativeUnits().firstOrNull()

    fun getNativeByPlacement(placement: String): ResolvedAdUnit? =
        getNativeUnits().firstOrNull { it.placement == placement }

    // ------------------------------------------------------------------
    // Provider helpers
    // ------------------------------------------------------------------

    /** All provider names that have at least one enabled unit across all types. */
    fun activeProviders(): Set<String> {
        val cfg = config ?: return emptySet()
        if (!cfg.shouldShowAds) return emptySet()
        val names = mutableSetOf<String>()
        if (cfg.usesAdUnits) {
            fun collect(units: List<AdUnit>) { units.filter { it.enabled && it.unitId.isNotEmpty() }.forEach { names += it.provider } }
            collect(cfg.adUnits.banner)
            collect(cfg.adUnits.interstitial)
            collect(cfg.adUnits.mrec)
            collect(cfg.adUnits.native)
            collect(cfg.adUnits.rewarded)
            collect(cfg.adUnits.rewardedInterstitial)
            collect(cfg.adUnits.appOpen)
        } else {
            cfg.providers.forEach { (name, p) -> if (p.enabled) names += name }
        }
        return names
    }

    fun isProviderActive(provider: String): Boolean = provider in activeProviders()

    fun needsAdMob(): Boolean = isProviderActive("admob")
    fun needsAppLovin(): Boolean = isProviderActive("applovin")
    fun needsUnity(): Boolean = isProviderActive("unity")
    fun needsMeta(): Boolean = isProviderActive("meta")

    // ------------------------------------------------------------------
    // Internal: resolve from adUnits arrays or fall back to legacy providers
    // ------------------------------------------------------------------

    private fun resolveUnits(
        typeEnabled: Boolean,
        adUnitsList: (RemoteAdConfig) -> List<AdUnit>,
        legacyFallback: (RemoteAdConfig) -> List<ResolvedAdUnit>,
    ): List<ResolvedAdUnit> {
        val cfg = config ?: return emptyList()
        if (!cfg.shouldShowAds || !typeEnabled) return emptyList()

        if (cfg.usesAdUnits) {
            return adUnitsList(cfg)
                .filter { it.enabled && it.unitId.isNotEmpty() }
                .map { ResolvedAdUnit(id = it.id, provider = it.provider, unitId = it.unitId, placement = it.placement) }
        }
        return legacyFallback(cfg)
    }

    // ------------------------------------------------------------------
    // Legacy provider → ResolvedAdUnit adapters
    // ------------------------------------------------------------------

    private fun legacyBannerUnit(cfg: RemoteAdConfig): List<ResolvedAdUnit> =
        firstLegacyUnit(cfg, providerOrder(cfg)) { name, p ->
            val uid = p.adaptiveBannerUnitId.ifEmpty { p.bannerUnitId }
            if (uid.isNotEmpty()) ResolvedAdUnit(id = "legacy_banner_$name", provider = name, unitId = uid, placement = "")
            else null
        }

    private fun legacyInterstitialUnit(cfg: RemoteAdConfig): List<ResolvedAdUnit> =
        firstLegacyUnit(cfg, providerOrder(cfg, cfg.defaultInterstitialProvider)) { name, p ->
            if (p.interstitialUnitId.isNotEmpty()) ResolvedAdUnit(id = "legacy_interstitial_$name", provider = name, unitId = p.interstitialUnitId, placement = "")
            else null
        }

    private fun legacyRewardedUnit(cfg: RemoteAdConfig): List<ResolvedAdUnit> =
        firstLegacyUnit(cfg, providerOrder(cfg, cfg.defaultRewardedProvider)) { name, p ->
            if (p.rewardedUnitId.isNotEmpty()) {
                ResolvedAdUnit(id = "legacy_rewarded_$name", provider = name, unitId = p.rewardedUnitId, placement = "")
            } else null
        }

    private fun legacyRewardedInterstitialOnly(cfg: RemoteAdConfig): List<ResolvedAdUnit> =
        firstLegacyUnit(cfg, providerOrder(cfg, cfg.defaultRewardedProvider)) { name, p ->
            if (p.rewardedInterstitialUnitId.isNotEmpty()) {
                ResolvedAdUnit(id = "legacy_rewarded_interstitial_$name", provider = name, unitId = p.rewardedInterstitialUnitId, placement = "")
            } else null
        }

    private fun legacyAppOpenUnit(cfg: RemoteAdConfig): List<ResolvedAdUnit> =
        firstLegacyUnit(cfg, providerOrder(cfg)) { name, p ->
            if (p.appOpenUnitId.isNotEmpty()) ResolvedAdUnit(id = "legacy_app_open_$name", provider = name, unitId = p.appOpenUnitId, placement = "")
            else null
        }

    private fun legacyNativeUnit(cfg: RemoteAdConfig): List<ResolvedAdUnit> =
        firstLegacyUnit(cfg, providerOrder(cfg)) { name, p ->
            val uid = p.nativeVideoUnitId.ifEmpty { p.nativeUnitId }
            if (uid.isNotEmpty()) ResolvedAdUnit(id = "legacy_native_$name", provider = name, unitId = uid, placement = "")
            else null
        }

    /**
     * Iterates providers in [order], calls [extract] on each enabled provider, and
     * returns the first non-null result wrapped in a singleton list (or empty).
     */
    private fun firstLegacyUnit(
        cfg: RemoteAdConfig,
        order: List<String>,
        extract: (name: String, provider: AdProviderConfig) -> ResolvedAdUnit?,
    ): List<ResolvedAdUnit> {
        for (name in order) {
            val p = cfg.providers[name] ?: continue
            if (!p.enabled) continue
            extract(name, p)?.let { return listOf(it) }
        }
        return emptyList()
    }

    /**
     * Build a provider lookup order: preferred provider first, then all others in
     * insertion order. Deduplicates.
     */
    private fun providerOrder(cfg: RemoteAdConfig, preferred: String? = null): List<String> {
        val order = mutableListOf<String>()
        preferred?.let { if (it in cfg.providers) order += it }
        cfg.providers.keys.forEach { if (it !in order) order += it }
        return order
    }
}
