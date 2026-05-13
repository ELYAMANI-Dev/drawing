package com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.R
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdManager

private const val TAG = "GalleryNativeAd"

/**
 * Single native placement on the gallery: prefers placement `"native_home"`, else first `adUnits.native`.
 * AdMob only; other providers are skipped until wired.
 */
@Composable
fun GalleryNativeAdRow(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(Unit) {
        if (!AdManager.isAdsEnabled) return@LaunchedEffect
        val resolved =
            AdManager.getNativeByPlacement("native_home")
                ?: AdManager.getFirstNative()
        if (resolved == null || resolved.provider != "admob" || resolved.unitId.isEmpty()) {
            return@LaunchedEffect
        }

        AdLoader.Builder(context, resolved.unitId)
            .forNativeAd { ad ->
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Native failed: $error")
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder().setRequestMultipleImages(false).build()
            )
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    DisposableEffect(nativeAd) {
        val ad = nativeAd
        onDispose {
            ad?.destroy()
        }
    }

    val ad = nativeAd ?: return

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp, max = 320.dp),
        factory = { ctx ->
            LayoutInflater.from(ctx)
                .inflate(R.layout.admob_native_compact, null, false) as NativeAdView
        },
        update = { view ->
            bindNativeAd(view, ad)
        }
    )
}

private fun bindNativeAd(adView: NativeAdView, ad: NativeAd) {
    adView.headlineView = adView.findViewById(R.id.ad_headline)
    adView.bodyView = adView.findViewById(R.id.ad_body)
    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
    adView.iconView = adView.findViewById(R.id.ad_icon)
    adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
    adView.mediaView = adView.findViewById(R.id.ad_media)

    (adView.headlineView as? TextView)?.text = ad.headline
    (adView.bodyView as? TextView)?.text = ad.body
    (adView.callToActionView as? Button)?.apply {
        text = ad.callToAction
        visibility = if (ad.callToAction.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    ad.icon?.let {
        (adView.iconView as? ImageView)?.setImageDrawable(it.drawable)
        adView.iconView?.visibility = View.VISIBLE
    } ?: run { adView.iconView?.visibility = View.GONE }

    ad.advertiser?.let {
        (adView.advertiserView as? TextView)?.text = it
        adView.advertiserView?.visibility = View.VISIBLE
    } ?: run { adView.advertiserView?.visibility = View.GONE }

    adView.mediaView?.let { media ->
        if (ad.mediaContent != null) {
            media.mediaContent = ad.mediaContent
            media.visibility = View.VISIBLE
        } else {
            media.visibility = View.GONE
        }
    }

    adView.setNativeAd(ad)
}
