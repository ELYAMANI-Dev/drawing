package com.stepbystepdrawing.HowToDrawPoppyPlaytime

import android.app.Activity
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.analytics.MixpanelAnalytics
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.CatalogLoad
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingApi
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingSession
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.UserProfileStore
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.UnavailableKind
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.hasInternetConnection
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.isLikelyNoNetwork
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.loadCatalogFromJson
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdManager
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdService
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.RemoteAdConfigService
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.DrawingStepsTheme
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlueDim
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import com.google.android.gms.ads.MobileAds
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class SplashActivity : ComponentActivity() {

    companion object {
        /** Bumps on each retry so [LaunchedEffect] always runs a fresh network fetch. */
        const val EXTRA_FETCH_ID = "com.stepbystepdrawing.HowToDrawPoppyPlaytime.EXTRA_FETCH_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT,
            ),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        val fetchId = intent.getLongExtra(EXTRA_FETCH_ID, 0L)
        setContent {
            DrawingStepsTheme {
                SplashScreen(
                    fetchId = fetchId,
                    onContinue = {
                        val target = if (UserProfileStore.isOnboarded(this))
                            MainActivity::class.java
                        else
                            QuizActivity::class.java
                        startActivity(Intent(this, target))
                        finish()
                    },
                )
            }
        }
    }
}

@Composable
private fun SplashLauncherIcon(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val painter = remember(context.packageName) {
        val drawable = context.applicationInfo.loadIcon(context.packageManager)
        val px = (88 * context.resources.displayMetrics.density).toInt().coerceIn(96, 512)
        BitmapPainter(drawable.toBitmap(px, px).asImageBitmap())
    }
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
    )
}

@Composable
private fun SplashScreen(
    fetchId: Long,
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val progress = remember { Animatable(0f) }
    val loadingLabel = stringResource(R.string.splash_loading_lessons)

    LaunchedEffect(fetchId) {
        progress.snapTo(0f)
        suspend fun bumpProgress(target: Float) {
            progress.animateTo(
                target,
                animationSpec = tween(durationMillis = 280, easing = LinearEasing),
            )
        }

        val appCtx = context.applicationContext

        suspend fun initServicesAndContinue() {
            val adConfig = withContext(Dispatchers.IO) {
                RemoteAdConfigService.load(appCtx)
            }
            withContext(Dispatchers.Main) {
                AdManager.init(adConfig)

                if (AdManager.needsAdMob()) {
                    suspendCancellableCoroutine { cont ->
                        MobileAds.initialize(appCtx) {
                            if (cont.isActive) cont.resume(Unit)
                        }
                    }
                }
                AdService.init(appCtx)

                MixpanelAnalytics.init(appCtx)
                MixpanelAnalytics.trackAppOpen()

                adConfig?.apiKeys?.oneSignalAppId?.takeIf { it.isNotBlank() }?.let { oneSignalAppId ->
                    if (BuildConfig.DEBUG) OneSignal.Debug.logLevel = LogLevel.VERBOSE
                    OneSignal.initWithContext(appCtx, oneSignalAppId)
                    CoroutineScope(Dispatchers.IO).launch {
                        OneSignal.Notifications.requestPermission(false)
                    }
                }
            }
            onContinue()
        }

        if (!appCtx.hasInternetConnection()) {
            DrawingSession.setUnavailable(
                context.getString(R.string.no_network_body),
                UnavailableKind.NO_DEVICE_INTERNET,
            )
            bumpProgress(1f)
            onContinue()
            return@LaunchedEffect
        }

        suspend fun fetchJson(): String = DrawingApi.fetchPublicAppJson()

        fun kindForFetchFailure(e: Throwable): UnavailableKind =
            if (e.isLikelyNoNetwork()) UnavailableKind.NETWORK_OR_TRANSPORT
            else UnavailableKind.CONTENT_OR_SERVER

        fun messageForFetchFailure(e: Throwable): String =
            if (e.isLikelyNoNetwork()) {
                context.getString(R.string.no_network_body)
            } else {
                val detail = e.message?.takeIf { it.isNotBlank() }
                if (detail != null) {
                    context.getString(R.string.server_error_body) + "\n\n" + detail
                } else {
                    context.getString(R.string.server_error_body)
                }
            }

        val firstFetch = runCatching { fetchJson() }
        if (firstFetch.isFailure) {
            val err = firstFetch.exceptionOrNull()!!
            DrawingSession.setUnavailable(messageForFetchFailure(err), kindForFetchFailure(err))
            bumpProgress(1f)
            onContinue()
            return@LaunchedEffect
        }

        var json = firstFetch.getOrThrow().trim()
        if (json.isEmpty()) {
            delay(500)
            val retryBlank = runCatching { fetchJson() }
            if (retryBlank.isFailure) {
                val err = retryBlank.exceptionOrNull()!!
                DrawingSession.setUnavailable(messageForFetchFailure(err), kindForFetchFailure(err))
                bumpProgress(1f)
                onContinue()
                return@LaunchedEffect
            }
            json = retryBlank.getOrThrow().trim()
        }

        if (json.isEmpty()) {
            DrawingSession.setUnavailable(
                context.getString(R.string.splash_error_empty),
                UnavailableKind.CONTENT_OR_SERVER,
            )
            bumpProgress(1f)
            onContinue()
            return@LaunchedEffect
        }

        bumpProgress(0.25f)
        when (val load = loadCatalogFromJson(context, json)) {
            CatalogLoad.Success -> {
                bumpProgress(1f)
                initServicesAndContinue()
                return@LaunchedEffect
            }
            is CatalogLoad.Error -> {
                DrawingSession.setUnavailable(load.message, UnavailableKind.CONTENT_OR_SERVER)
                bumpProgress(1f)
                onContinue()
                return@LaunchedEffect
            }
            CatalogLoad.Empty -> Unit
        }

        bumpProgress(0.55f)
        delay(700)
        val secondFetch = runCatching { fetchJson() }
        if (secondFetch.isFailure) {
            val err = secondFetch.exceptionOrNull()!!
            DrawingSession.setUnavailable(messageForFetchFailure(err), kindForFetchFailure(err))
            bumpProgress(1f)
            onContinue()
            return@LaunchedEffect
        }

        val json2 = secondFetch.getOrThrow().trim()
        if (json2.isNotEmpty()) {
            when (val load2 = loadCatalogFromJson(context, json2)) {
                CatalogLoad.Success -> {
                    bumpProgress(1f)
                    initServicesAndContinue()
                    return@LaunchedEffect
                }
                is CatalogLoad.Error -> {
                    DrawingSession.setUnavailable(load2.message, UnavailableKind.CONTENT_OR_SERVER)
                    bumpProgress(1f)
                    onContinue()
                    return@LaunchedEffect
                }
                CatalogLoad.Empty -> Unit
            }
        }

        DrawingSession.setUnavailable(
            context.getString(R.string.splash_error_empty),
            UnavailableKind.CONTENT_OR_SERVER,
        )
        bumpProgress(1f)
        onContinue()
    }

    BackHandler { (context as? Activity)?.finish() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Image(
            painter = painterResource(R.drawable.splash),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.1f),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SplashLauncherIcon(modifier = Modifier.size(88.dp))
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
            )
            Spacer(Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryBlue,
                trackColor = PrimaryBlueDim,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = loadingLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
            )
        }
    }
}
