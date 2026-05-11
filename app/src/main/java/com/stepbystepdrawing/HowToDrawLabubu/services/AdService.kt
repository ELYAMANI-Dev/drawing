package com.stepbystepdrawing.HowToDrawLabubu.services

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.ads.MaxRewardedAd
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Loads and shows ads from [AdManager] `adUnits` only (legacy `providers` is unused when
 * [RemoteAdConfig.usesAdUnits] is true).
 *
 * Full show/load: **AdMob** + **AppLovin** for app open, interstitial, rewarded, rewarded interstitial.
 * Other provider strings are logged and skipped until wired to Unity / Meta SDKs.
 */
object AdService {
    private const val TAG = "AdService"

    private var admobAppOpen: AppOpenAd? = null
    private var appOpenShown = false

    private var admobRewarded: RewardedAd? = null
    private var maxRewardedAd: MaxRewardedAd? = null
    private val pendingRewardResult = AtomicReference<kotlin.coroutines.Continuation<Boolean>?>(null)

    private val admobInterstitialByUnitId = mutableMapOf<String, InterstitialAd?>()
    private val maxInterstitialByUnitId = mutableMapOf<String, MaxInterstitialAd?>()

    private var admobRewardedInterstitial: RewardedInterstitialAd? = null
    /** AppLovin: use a dedicated rewarded ad instance for `rewarded_interstitial` unit IDs. */
    private var maxRewardedInterstitialAd: MaxRewardedAd? = null
    private val pendingRewardedInterstitialResult = AtomicReference<kotlin.coroutines.Continuation<Boolean>?>(null)

    private val interstitialPickIndex = AtomicInteger(0)
    private val pendingInterstitialDone = AtomicReference<kotlin.coroutines.Continuation<Unit>?>(null)

    private var appLovinInitialized = false
    private val appLovinReadyCallbacks = CopyOnWriteArrayList<() -> Unit>()

    private fun flushAppLovinReadyCallbacks() {
        val pending = synchronized(this) {
            val copy = appLovinReadyCallbacks.toList()
            appLovinReadyCallbacks.clear()
            copy
        }
        pending.forEach { runCatching(it).onFailure { e -> Log.w(TAG, "AppLovin ready callback failed", e) } }
    }

    /**
     * Runs [action] after AppLovin init completes (or immediately if already done).
     * If AppLovin is not needed, do not call. If init is skipped (e.g. missing SDK key), pending actions are flushed once anyway.
     */
    fun runWhenAppLovinReady(action: () -> Unit) {
        if (!AdManager.needsAppLovin()) return
        val runImmediately = synchronized(this) {
            if (appLovinInitialized) true
            else {
                appLovinReadyCallbacks.add(action)
                false
            }
        }
        if (runImmediately) action()
    }

    private fun Activity.isSafeForAds(): Boolean {
        if (isFinishing) return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            !isDestroyed
        } else {
            true
        }
    }

    fun init(context: Context) {
        if (!AdManager.isAdsEnabled) return

        if (AdManager.needsAppLovin()) {
            initAppLovin(context)
        }

        val appOpen = AdManager.getFirstAppOpen()
        if (appOpen != null && appOpen.provider == "admob") {
            loadAdmobAppOpen(context.applicationContext, appOpen.unitId)
        }

        val rewarded = AdManager.getFirstRewarded()
        if (rewarded != null && rewarded.provider == "admob") {
            loadAdmobRewarded(context.applicationContext, rewarded.unitId)
        }
    }

    /** Preload all `adUnits.interstitial` (and AppLovin instances) when entering the step player. */
    fun preloadStepAds(activity: Activity) {
        if (!AdManager.isAdsEnabled) return
        for (unit in AdManager.getInterstitialUnits()) {
            when (unit.provider) {
                "admob" -> loadAdmobInterstitial(activity.applicationContext, unit.unitId)
                "applovin" -> ensureMaxInterstitial(activity, unit.unitId)
                else -> Log.w(TAG, "Interstitial preload not implemented for provider=${unit.provider} id=${unit.id}")
            }
        }
    }

    private fun initAppLovin(context: Context) {
        if (appLovinInitialized) return
        val key = AdManager.getConfig()?.appLovinSdkKey
        if (key.isNullOrEmpty()) {
            Log.w(TAG, "AppLovin SDK key missing, skipping init")
            flushAppLovinReadyCallbacks()
            return
        }
        try {
            val initConfig = AppLovinSdkInitializationConfiguration.builder(key)
                .setMediationProvider(AppLovinMediationProvider.MAX)
                .build()
            AppLovinSdk.getInstance(context).initialize(initConfig) { _: AppLovinSdkConfiguration ->
                Log.d(TAG, "AppLovin SDK initialized")
                synchronized(this@AdService) {
                    appLovinInitialized = true
                }
                flushAppLovinReadyCallbacks()
            }
        } catch (e: Exception) {
            Log.e(TAG, "AppLovin init failed", e)
            flushAppLovinReadyCallbacks()
        }
    }

    fun prepareAppLovinAds(activity: Activity) {
        if (!activity.isSafeForAds()) return
        if (!AdManager.isAdsEnabled || !appLovinInitialized) return

        val rewarded = AdManager.getFirstRewarded()
        if (rewarded != null && rewarded.provider == "applovin" && maxRewardedAd == null) {
            maxRewardedAd = MaxRewardedAd.getInstance(rewarded.unitId, activity).apply {
                setListener(object : MaxRewardedAdListener {
                    override fun onAdLoaded(ad: MaxAd) { Log.d(TAG, "AppLovin rewarded loaded") }
                    override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                        Log.w(TAG, "AppLovin rewarded load failed: ${error.message}")
                    }
                    override fun onAdDisplayed(ad: MaxAd) {}
                    override fun onAdHidden(ad: MaxAd) {
                        pendingRewardResult.getAndSet(null)?.resume(false)
                        loadAd()
                    }
                    override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                        pendingRewardResult.getAndSet(null)?.resume(false)
                    }
                    override fun onAdClicked(ad: MaxAd) {}
                    override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
                        pendingRewardResult.getAndSet(null)?.resume(true)
                    }
                })
                loadAd()
            }
        }

        for (unit in AdManager.getInterstitialUnits()) {
            if (unit.provider == "applovin") ensureMaxInterstitial(activity, unit.unitId)
        }
    }

    private fun ensureMaxInterstitial(activity: Activity, unitId: String) {
        if (!activity.isSafeForAds()) return
        if (unitId.isEmpty() || maxInterstitialByUnitId.containsKey(unitId)) return
        if (!appLovinInitialized) return
        maxInterstitialByUnitId[unitId] = MaxInterstitialAd(unitId, activity).apply {
            setListener(object : MaxAdListener {
                override fun onAdLoaded(ad: MaxAd) { Log.d(TAG, "AppLovin interstitial loaded $unitId") }
                override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                    Log.w(TAG, "AppLovin interstitial load failed $unitId: ${error.message}")
                }
                override fun onAdDisplayed(ad: MaxAd) {}
                override fun onAdHidden(ad: MaxAd) {
                    pendingInterstitialDone.getAndSet(null)?.resume(Unit)
                    loadAd()
                }
                override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                    pendingInterstitialDone.getAndSet(null)?.resume(Unit)
                }
                override fun onAdClicked(ad: MaxAd) {}
            })
            loadAd()
        }
    }

    private fun ensureMaxRewardedInterstitial(activity: Activity, unitId: String) {
        if (!activity.isSafeForAds()) return
        if (unitId.isEmpty() || maxRewardedInterstitialAd != null) return
        if (!appLovinInitialized) return
        maxRewardedInterstitialAd = MaxRewardedAd.getInstance(unitId, activity).apply {
            setListener(object : MaxRewardedAdListener {
                override fun onAdLoaded(ad: MaxAd) { Log.d(TAG, "AppLovin rewarded interstitial slot loaded") }
                override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                    Log.w(TAG, "AppLovin rewarded interstitial load failed: ${error.message}")
                }
                override fun onAdDisplayed(ad: MaxAd) {}
                override fun onAdHidden(ad: MaxAd) {
                    pendingRewardedInterstitialResult.getAndSet(null)?.resume(false)
                    loadAd()
                }
                override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                    pendingRewardedInterstitialResult.getAndSet(null)?.resume(false)
                }
                override fun onAdClicked(ad: MaxAd) {}
                override fun onUserRewarded(ad: MaxAd, reward: MaxReward) {
                    pendingRewardedInterstitialResult.getAndSet(null)?.resume(true)
                }
            })
            loadAd()
        }
    }

    // ---------------------------------------------------------------
    // App Open (single unit from adUnits.app_open)
    // ---------------------------------------------------------------

    private fun loadAdmobAppOpen(context: Context, unitId: String) {
        val adRequest = AdRequest.Builder().build()
        AppOpenAd.load(
            context, unitId, adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    admobAppOpen = ad
                    Log.d(TAG, "AppOpenAd loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "AppOpenAd failed to load: $error")
                    admobAppOpen = null
                }
            }
        )
    }

    fun showAppOpenAd(activity: Activity?) {
        if (appOpenShown || activity == null) return
        if (!activity.isSafeForAds()) return
        if (!AdManager.isAdsEnabled) return
        val resolved = AdManager.getFirstAppOpen() ?: return

        when (resolved.provider) {
            "admob" -> showAdmobAppOpen(activity, resolved.unitId)
            "applovin" -> showAppLovinAppOpen(activity, resolved.unitId)
            else -> Log.w(TAG, "App open not implemented for provider=${resolved.provider}")
        }
    }

    private fun showAdmobAppOpen(activity: Activity, unitId: String) {
        if (!activity.isSafeForAds()) return
        val ad = admobAppOpen ?: return
        appOpenShown = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                admobAppOpen = null
                activity.applicationContext?.let { loadAdmobAppOpen(it, unitId) }
            }
            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                admobAppOpen = null
                activity.applicationContext?.let { loadAdmobAppOpen(it, unitId) }
            }
        }
        ad.show(activity)
    }

    private fun showAppLovinAppOpen(activity: Activity, unitId: String) {
        if (unitId.isEmpty()) return
        com.applovin.mediation.ads.MaxAppOpenAd(unitId, activity).apply {
            setListener(object : MaxAdListener {
                override fun onAdLoaded(ad: MaxAd) {
                    if (!appOpenShown) {
                        appOpenShown = true
                        showAd()
                    }
                }
                override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                    Log.w(TAG, "AppLovin app open load failed: ${error.message}")
                }
                override fun onAdDisplayed(ad: MaxAd) {}
                override fun onAdHidden(ad: MaxAd) {}
                override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {}
                override fun onAdClicked(ad: MaxAd) {}
            })
            loadAd()
        }
    }

    // ---------------------------------------------------------------
    // Interstitial — slot 0 / 1 = first & second entries in adUnits.interstitial (cycles if only one)
    // ---------------------------------------------------------------

    private fun loadAdmobInterstitial(context: Context, unitId: String) {
        if (unitId.isEmpty()) return
        InterstitialAd.load(
            context,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    admobInterstitialByUnitId[unitId] = ad
                    Log.d(TAG, "AdMob interstitial loaded $unitId")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "AdMob interstitial failed $unitId: $error")
                    admobInterstitialByUnitId[unitId] = null
                }
            }
        )
    }

    private fun pickInterstitialUnit(): AdManager.ResolvedAdUnit? {
        val units = AdManager.getInterstitialUnits()
        if (units.isEmpty()) return null
        val i = interstitialPickIndex.getAndIncrement().coerceAtLeast(0) % units.size
        return units[i]
    }

    suspend fun showInterstitial(activity: Activity) = withContext(Dispatchers.Main) {
        if (!activity.isSafeForAds()) return@withContext
        if (!AdManager.isAdsEnabled) return@withContext
        val resolved = pickInterstitialUnit() ?: return@withContext
        showInterstitialForResolved(activity, resolved)
    }

    /**
     * Shows interstitial at index [slotIndex] in [AdManager.getInterstitialUnits] (0 = "interstitial 1",
     * 1 = "interstitial 2"). If only one unit is configured, it is reused for both slots.
     */
    suspend fun showInterstitialAtSlot(activity: Activity, slotIndex: Int) = withContext(Dispatchers.Main) {
        if (!activity.isSafeForAds()) return@withContext
        if (!AdManager.isAdsEnabled) return@withContext
        val units = AdManager.getInterstitialUnits()
        if (units.isEmpty()) return@withContext
        val resolved = units[slotIndex.mod(units.size)]
        showInterstitialForResolved(activity, resolved)
    }

    private suspend fun showInterstitialForResolved(activity: Activity, resolved: AdManager.ResolvedAdUnit) {
        when (resolved.provider) {
            "admob" -> showAdmobInterstitial(activity, resolved.unitId)
            "applovin" -> showAppLovinInterstitial(activity, resolved.unitId)
            else -> Log.w(TAG, "Interstitial show not implemented for provider=${resolved.provider}")
        }
    }

    private suspend fun showAdmobInterstitial(activity: Activity, unitId: String) {
        if (!activity.isSafeForAds()) return
        val ad = admobInterstitialByUnitId[unitId] ?: run {
            loadAdmobInterstitial(activity.applicationContext, unitId)
            return
        }
        withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                cont.invokeOnCancellation {
                    ad.fullScreenContentCallback = null
                }
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        admobInterstitialByUnitId[unitId] = null
                        if (activity.isSafeForAds()) {
                            loadAdmobInterstitial(activity.applicationContext, unitId)
                        }
                        if (cont.isActive) cont.resume(Unit)
                    }
                    override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                        admobInterstitialByUnitId[unitId] = null
                        if (activity.isSafeForAds()) {
                            loadAdmobInterstitial(activity.applicationContext, unitId)
                        }
                        if (cont.isActive) cont.resume(Unit)
                    }
                }
                if (activity.isSafeForAds()) {
                    ad.show(activity)
                } else {
                    admobInterstitialByUnitId[unitId] = null
                    if (cont.isActive) cont.resume(Unit)
                }
            }
        }
    }

    private suspend fun showAppLovinInterstitial(activity: Activity, unitId: String) {
        if (!activity.isSafeForAds()) return
        ensureMaxInterstitial(activity, unitId)
        val maxAd = maxInterstitialByUnitId[unitId] ?: return
        if (!maxAd.isReady) {
            maxAd.loadAd()
            return
        }
        withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                cont.invokeOnCancellation { pendingInterstitialDone.compareAndSet(cont, null) }
                pendingInterstitialDone.set(cont)
                maxAd.showAd()
            }
        }
    }

    // ---------------------------------------------------------------
    // Rewarded (optional; legacy / other screens)
    // ---------------------------------------------------------------

    private fun loadAdmobRewarded(context: Context, unitId: String) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context, unitId, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    admobRewarded = ad
                    Log.d(TAG, "RewardedAd loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "RewardedAd failed to load: $error")
                    admobRewarded = null
                }
            }
        )
    }

    suspend fun showRewardedAd(activity: Activity): Boolean = withContext(Dispatchers.Main) {
        if (!activity.isSafeForAds()) return@withContext false
        if (!AdManager.isAdsEnabled) return@withContext false
        val resolved = AdManager.getFirstRewarded() ?: return@withContext false

        when (resolved.provider) {
            "admob" -> showAdmobRewarded(activity, resolved.unitId)
            "applovin" -> showAppLovinRewarded(activity)
            else -> false
        }
    }

    private suspend fun showAdmobRewarded(activity: Activity, unitId: String): Boolean {
        if (!activity.isSafeForAds()) return false
        val ad = admobRewarded ?: return false
        return withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                cont.invokeOnCancellation {
                    ad.fullScreenContentCallback = null
                }
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        admobRewarded = null
                        activity.applicationContext?.let { loadAdmobRewarded(it, unitId) }
                        if (cont.isActive) cont.resume(false)
                    }
                    override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                        admobRewarded = null
                        activity.applicationContext?.let { loadAdmobRewarded(it, unitId) }
                        if (cont.isActive) cont.resume(false)
                    }
                }
                if (activity.isSafeForAds()) {
                    ad.show(activity) { _ ->
                        if (cont.isActive) cont.resume(true)
                    }
                } else {
                    admobRewarded = null
                    if (cont.isActive) cont.resume(false)
                }
            }
        } ?: false
    }

    private suspend fun showAppLovinRewarded(activity: Activity): Boolean {
        if (!activity.isSafeForAds()) return false
        val maxAd = maxRewardedAd ?: return false
        if (!maxAd.isReady) return false
        return withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                cont.invokeOnCancellation { pendingRewardResult.compareAndSet(cont, null) }
                pendingRewardResult.set(cont)
                maxAd.showAd(activity)
            }
        } ?: false
    }

    // ---------------------------------------------------------------
    // Rewarded interstitial (`adUnits.rewarded_interstitial`) — optional / future use
    // ---------------------------------------------------------------

    private fun loadAdmobRewardedInterstitial(context: Context, unitId: String) {
        if (unitId.isEmpty()) return
        RewardedInterstitialAd.load(
            context,
            unitId,
            AdRequest.Builder().build(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    admobRewardedInterstitial = ad
                    Log.d(TAG, "Rewarded interstitial loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded interstitial failed: $error")
                    admobRewardedInterstitial = null
                }
            }
        )
    }

    /**
     * Shows the first enabled `rewarded_interstitial` unit from config.
     * Returns whether the user earned the reward (AdMob); AppLovin uses the same signal.
     */
    suspend fun showRewardedInterstitial(activity: Activity): Boolean = withContext(Dispatchers.Main) {
        if (!activity.isSafeForAds()) return@withContext false
        if (!AdManager.isAdsEnabled) return@withContext false
        val resolved = AdManager.getFirstRewardedInterstitial() ?: return@withContext false
        when (resolved.provider) {
            "admob" -> showAdmobRewardedInterstitial(activity, resolved.unitId)
            "applovin" -> showAppLovinRewardedInterstitial(activity)
            else -> {
                Log.w(TAG, "Rewarded interstitial not implemented for provider=${resolved.provider}")
                false
            }
        }
    }

    private suspend fun showAdmobRewardedInterstitial(activity: Activity, unitId: String): Boolean {
        if (!activity.isSafeForAds()) return false
        val ad = admobRewardedInterstitial ?: run {
            loadAdmobRewardedInterstitial(activity.applicationContext, unitId)
            return false
        }
        return withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                cont.invokeOnCancellation {
                    ad.fullScreenContentCallback = null
                }
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        admobRewardedInterstitial = null
                        activity.applicationContext?.let { loadAdmobRewardedInterstitial(it, unitId) }
                        if (cont.isActive) cont.resume(false)
                    }
                    override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                        admobRewardedInterstitial = null
                        activity.applicationContext?.let { loadAdmobRewardedInterstitial(it, unitId) }
                        if (cont.isActive) cont.resume(false)
                    }
                }
                if (activity.isSafeForAds()) {
                    ad.show(activity) { if (cont.isActive) cont.resume(true) }
                } else {
                    admobRewardedInterstitial = null
                    if (cont.isActive) cont.resume(false)
                }
            }
        } ?: false
    }

    private suspend fun showAppLovinRewardedInterstitial(activity: Activity): Boolean {
        if (!activity.isSafeForAds()) return false
        val resolved = AdManager.getFirstRewardedInterstitial() ?: return false
        ensureMaxRewardedInterstitial(activity, resolved.unitId)
        val maxAd = maxRewardedInterstitialAd ?: return false
        if (!maxAd.isReady) {
            maxAd.loadAd()
            return false
        }
        return withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine { cont ->
                cont.invokeOnCancellation { pendingRewardedInterstitialResult.compareAndSet(cont, null) }
                pendingRewardedInterstitialResult.set(cont)
                maxAd.showAd(activity)
            }
        } ?: false
    }
}
