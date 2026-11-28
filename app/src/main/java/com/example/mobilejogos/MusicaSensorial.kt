package com.example.mobilejogos

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import java.lang.ref.WeakReference
object MusicaSensorial {

    private var player: MediaPlayer? = null

    private var telaAtiva: WeakReference<Context>? = null
    private val handler = Handler(Looper.getMainLooper())

    private val pausa = Runnable {

        if (telaAtiva?.get() == null) {
            player?.pause()
        }
    }

    fun iniciar(context: Context) {

        handler.removeCallbacks(pausa)

        telaAtiva = WeakReference(context)

        if (player == null) {

            player = MediaPlayer.create(
                context.applicationContext,
                R.raw.fundoemocoes
            )?.apply {

                isLooping = true

                setVolume(0.08f, 0.08f)
            }
        }

        player?.let {

            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    fun pausar(context: Context) {

        if (telaAtiva?.get() == context) return

        telaAtiva = null

        handler.removeCallbacks(pausa)

        handler.postDelayed(pausa, 1000L)
    }

    fun parar() {

        handler.removeCallbacks(pausa)

        telaAtiva = null

        player?.release()

        player = null
    }
}