package com.stepbystepdrawing.HowToDrawPoppyPlaytime

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.DrawingSession
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.data.UserProfileStore
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.screens.QuizOnboardingScreen
import com.stepbystepdrawing.HowToDrawPoppyPlaytime.ui.theme.DrawingStepsTheme

class QuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            DrawingStepsTheme {
                val cards = remember {
                    (DrawingSession.getState() as? DrawingSession.State.Ready)?.cards ?: emptyList()
                }
                QuizOnboardingScreen(
                    cards = cards,
                    onComplete = { profile ->
                        UserProfileStore.save(this, profile)
                        UserProfileStore.setOnboarded(this)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
