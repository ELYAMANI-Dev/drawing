package com.stepbystepdrawing.HowToDrawPoppyPlaytime.services

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Lightweight sound effects + haptic feedback using ToneGenerator (no asset files needed).
 */
object SoundManager {
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    fun init(context: Context) {
        toneGenerator = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 60) }.getOrNull()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playCorrect() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
        vibrateShort()
    }

    fun playWrong() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 200)
        vibrateMedium()
    }

    fun playCoin() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 100)
        vibrateShort()
    }

    fun playSpin() {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 80)
    }

    fun playCelebration() {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 300)
        vibrateLong()
    }

    fun playUnlock() {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        vibrateShort()
    }

    private fun vibrateShort() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun vibrateMedium() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun vibrateLong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
