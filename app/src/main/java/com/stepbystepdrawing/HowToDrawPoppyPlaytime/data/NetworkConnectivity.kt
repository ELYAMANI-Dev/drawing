package com.stepbystepdrawing.HowToDrawPoppyPlaytime.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Best-effort hint only (VPN, metered, OEM quirks). Never use this to skip HTTP —
 * always try the request first, then fall back to cache.
 */
fun Context.hasInternetConnection(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
