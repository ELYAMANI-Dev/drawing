package com.stepbystepdrawing.HowToDrawLabubu.data.config

import com.stepbystepdrawing.HowToDrawLabubu.BuildConfig

/**
 * Ad Manager remote config URL.
 * Built from local.properties AD_CONFIG_URL at compile time.
 * JSON contains ad settings, API keys (mixpanelToken, oneSignalAppId), etc.
 */
object AdManagerConfig {
    val CONFIG_URL: String get() = BuildConfig.AD_CONFIG_URL
}
