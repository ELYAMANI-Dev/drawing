package com.stepbystepdrawing.HowToDrawPoppyPlaytime

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.analytics.MixpanelAnalytics
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingDetails
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingSession
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.FavoritesStore
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.UiState
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdManager
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.services.AdService
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.components.OfflineConnectionScreen
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens.DetailScreen
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens.FavoritesScreen
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens.GalleryScreen
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.BorderStrong
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.DrawingStepsTheme
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlue
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.PrimaryBlueDim
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextPrimary
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.TextSecondary
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

private enum class MainListDestination {
    Gallery,
    Favorites,
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DrawingStepsTheme {
                DrawingStepsApp()
            }
        }
        window.decorView.post { hideImmersiveSystemUi() }
    }

    override fun onResume() {
        super.onResume()
        hideImmersiveSystemUi()
    }

    override fun onPause() {
        super.onPause()
        MixpanelAnalytics.flush()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideImmersiveSystemUi()
    }

    private fun hideImmersiveSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

private fun shareDrawingStepsOnPlayStore(context: android.content.Context) {
    val pkg = context.packageName
    val url = "https://play.google.com/store/apps/details?id=$pkg"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            context.getString(
                R.string.share_app_body,
                context.getString(R.string.app_name),
                url,
            ),
        )
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(send, context.getString(R.string.share_app_chooser)))
}

private fun splashRetryIntent(activity: ComponentActivity): Intent =
    Intent(activity, SplashActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        putExtra(SplashActivity.EXTRA_FETCH_ID, System.nanoTime())
    }

@Composable
private fun DrawingStepsApp() {
    val activity = LocalContext.current as? ComponentActivity
    var session by remember { mutableStateOf(DrawingSession.peekState()) }

    // When session becomes null (Try again), always launch a fresh Splash fetch.
    // Key must be `session` so this runs again after each reset — not `Unit` (that only ran once).
    LaunchedEffect(session) {
        if (session == null) {
            val act = activity ?: return@LaunchedEffect
            act.startActivity(splashRetryIntent(act))
            act.finish()
        }
    }

    if (session == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {}
        return
    }

    val state: DrawingSession.State = session!!
    when (state) {
        is DrawingSession.State.Unavailable ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.displayCutout)
            ) {
                OfflineConnectionScreen(
                    kind = state.kind,
                    detailMessage = state.message,
                    onCloseApp = { activity?.finish() },
                    onOpenNetworkSettings = {
                        activity?.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                    },
                    onRetry = {
                        DrawingSession.reset()
                        session = null
                    },
                )
            }

        is DrawingSession.State.Ready ->
            DrawingStepsMainFlow(
                activity = activity,
                session = state,
            )
    }
}

@Composable
private fun DrawingStepsMainFlow(
    activity: ComponentActivity?,
    session: DrawingSession.State.Ready,
) {
    val appContext = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val favoritesStore = remember(appContext) { FavoritesStore(appContext) }
    var favoriteDrawingIds by remember { mutableStateOf(favoritesStore.load()) }
    var mainListDestination by remember { mutableStateOf(MainListDestination.Gallery) }

    var selectedDrawingId by remember { mutableStateOf<String?>(null) }
    var detailState by remember { mutableStateOf<UiState<DrawingDetails>>(UiState.Loading) }
    var currentStep by remember { mutableIntStateOf(0) }
    var showResultFirst by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AdService.showAppOpenAd(activity)
        val act = activity ?: return@LaunchedEffect
        AdService.runWhenAppLovinReady {
            if (!act.isFinishing && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !act.isDestroyed)) {
                AdService.prepareAppLovinAds(act)
            }
        }
    }

    LaunchedEffect(selectedDrawingId, showResultFirst) {
        if (selectedDrawingId != null && !showResultFirst) {
            val act = activity ?: return@LaunchedEffect
            if (!act.isFinishing && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !act.isDestroyed)) {
                AdService.preloadStepAds(act)
            }
        }
    }

    LaunchedEffect(selectedDrawingId, mainListDestination) {
        when {
            selectedDrawingId != null -> MixpanelAnalytics.trackScreenView("Detail", mapOf("lesson_id" to selectedDrawingId!!))
            mainListDestination == MainListDestination.Favorites -> MixpanelAnalytics.trackScreenView("Favorites")
            else -> MixpanelAnalytics.trackScreenView("Gallery")
        }
    }

    val galleryState = remember(session.cards) {
        if (session.cards.isEmpty()) {
            UiState.Error("No lessons available.")
        } else {
            UiState.Success(session.cards)
        }
    }

    val openLesson: (String) -> Unit = remember(session) {
        { id: String ->
            val details = session.details[id]
            detailState = if (details != null) {
                UiState.Success(details)
            } else {
                UiState.Error("Lesson not found.")
            }
            selectedDrawingId = id
            currentStep = 0
            showResultFirst = true
            val title = session.cards.firstOrNull { it.id == id }?.title ?: id
            MixpanelAnalytics.trackLessonOpened(id, title)
        }
    }

    val favoriteCards = remember(session.cards, favoriteDrawingIds) {
        session.cards.filter { it.id in favoriteDrawingIds }
    }

    val composeContext = LocalContext.current
    LaunchedEffect(session.cards, session.details) {
        preloadHomeImages(
            appContext = appContext,
            session = session,
            onProgress = {},
        )
    }

    BackHandler {
        when {
            selectedDrawingId != null -> {
                selectedDrawingId = null
                detailState = UiState.Loading
            }
            mainListDestination == MainListDestination.Favorites -> {
                mainListDestination = MainListDestination.Gallery
            }
            else -> showExitDialog = true
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.displayCutout)
        ) {
            if (selectedDrawingId == null) {
                when (mainListDestination) {
                    MainListDestination.Gallery ->
                        GalleryScreen(
                            heroTitle = session.appTitle,
                            galleryState = galleryState,
                            onOpenFavorites = { mainListDestination = MainListDestination.Favorites },
                            favoritesShortcutHighlighted = favoriteDrawingIds.isNotEmpty(),
                            onPlayRandom = {
                                if (session.cards.isNotEmpty()) {
                                    MixpanelAnalytics.trackPlayRandom()
                                    openLesson(session.cards.random().id)
                                }
                            },
                            onShareApp = {
                                MixpanelAnalytics.trackShareApp()
                                shareDrawingStepsOnPlayStore(composeContext)
                            },
                            onRequestExit = { showExitDialog = true },
                            onSelect = openLesson
                        )
                    MainListDestination.Favorites ->
                        FavoritesScreen(
                            favoriteCards = favoriteCards,
                            onBack = { mainListDestination = MainListDestination.Gallery },
                            onSelectLesson = openLesson
                        )
                }
            } else {
                DetailScreen(
                    detailState = detailState,
                    currentStep = currentStep,
                    showResultFirst = showResultFirst,
                    isFavorite = selectedDrawingId?.let { it in favoriteDrawingIds } == true,
                    onBackToGallery = {
                        selectedDrawingId = null
                        detailState = UiState.Loading
                    },
                    onBackStep = { if (currentStep > 0) currentStep -= 1 },
                    onNextStep = {
                        val total = (detailState as? UiState.Success)?.data?.steps?.size ?: 0
                        if (currentStep < total - 1) {
                            currentStep += 1
                            // Step ads (1-based index after each Next): 5,15,25… → int #1/#2; 10,20,30… → rewarded
                            val stepNumber = currentStep + 1
                            if (AdManager.isAdsEnabled) {
                                scope.launch {
                                    val act = activity ?: return@launch
                                    if (act.isFinishing) return@launch
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && act.isDestroyed) return@launch
                                    when {
                                        stepNumber % 10 == 0 && stepNumber >= 10 ->
                                            AdService.showRewardedAd(act)
                                        stepNumber % 10 == 5 ->
                                            AdService.showInterstitialAtSlot(
                                                act,
                                                slotIndex = (stepNumber / 10) % 2,
                                            )
                                    }
                                }
                            }
                        }
                    },
                    onToggleFavorite = {
                        val id = selectedDrawingId ?: return@DetailScreen
                        favoriteDrawingIds = favoritesStore.toggle(id)
                        MixpanelAnalytics.trackFavoriteToggled(id, id in favoriteDrawingIds)
                    },
                    onStartSteps = {
                        showResultFirst = false
                        currentStep = 0
                    },
                    onGoToPreview = { showResultFirst = true },
                    onCompleteLesson = { showResultFirst = true },
                )
            }
        }
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onDismiss = { showExitDialog = false },
            onConfirmExit = { activity?.finish() }
        )
    }
}

private suspend fun preloadHomeImages(
    appContext: android.content.Context,
    session: DrawingSession.State.Ready,
    onProgress: (Float) -> Unit,
) {
    val urls = LinkedHashSet<String>()
    session.cards.forEach { if (it.thumbnailUrl.isNotBlank()) urls += it.thumbnailUrl }
    // Step images loaded on-demand (prevents OOM on large catalogs)
    if (urls.isEmpty()) {
        onProgress(1f)
        return
    }

    val loader = appContext.imageLoader
    val total = urls.size
    var done = 0
    urls.forEach { url ->
        coroutineContext.ensureActive()
        withContext(Dispatchers.IO) {
            runCatching {
                loader.execute(
                    ImageRequest.Builder(appContext)
                        .data(url)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .networkCachePolicy(CachePolicy.ENABLED)
                        .crossfade(false)
                        .build()
                )
            }
        }
        done += 1
        onProgress(done.toFloat() / total.toFloat())
    }
}

@Composable
private fun ExitConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(2.dp, BorderStrong, RoundedCornerShape(24.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.SentimentVeryDissatisfied,
                    contentDescription = null,
                    modifier = Modifier.size(88.dp),
                    tint = PrimaryBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                androidx.compose.material3.Text(
                    text = stringResource(
                        R.string.exit_dialog_title,
                        stringResource(R.string.app_name),
                    ),
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.material3.Text(
                    text = "Are you sure you want to close the app?",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(PrimaryBlueDim, RoundedCornerShape(16.dp))
                            .border(2.dp, PrimaryBlue, RoundedCornerShape(16.dp))
                            .clickable { onConfirmExit() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = "Yes",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(PrimaryBlueDim, RoundedCornerShape(16.dp))
                            .border(2.dp, PrimaryBlue, RoundedCornerShape(16.dp))
                            .clickable { onDismiss() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Text(
                            text = "No",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrawingStepsPreview() {
    DrawingStepsTheme { DrawingStepsApp() }
}
