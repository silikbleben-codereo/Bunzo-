package com.example.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.SoundEffectConstants
import android.view.View
import com.example.BunzoApplication

object SoundHelper {
    private var toneGenerator: ToneGenerator? = null
    private var audioManager: AudioManager? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (e: Exception) {
            try {
                toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 90)
            } catch (_: Exception) {
                toneGenerator = null
            }
        }
        try {
            audioManager = BunzoApplication.instance.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        } catch (_: Exception) {}
    }

    fun playClickSound(view: View? = null) {
        try {
            view?.playSoundEffect(SoundEffectConstants.CLICK)
        } catch (_: Exception) {}
        try {
            audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
        } catch (_: Exception) {}
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
        } catch (_: Exception) {}
    }

    fun playWelcomeChime() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 250)
        } catch (_: Exception) {}
    }

    fun playAddClickSound(view: View? = null) {
        playClickSound(view)
    }

    fun playKitchenNewOrderAlert() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 400)
        } catch (_: Exception) {}
    }

    fun playStatusUpdateSound() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 150)
        } catch (_: Exception) {}
    }
}
