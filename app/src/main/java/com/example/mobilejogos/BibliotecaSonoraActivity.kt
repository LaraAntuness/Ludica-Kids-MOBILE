package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast

class BibliotecaSonoraActivity : Activity() {

    private lateinit var tela: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 393f

    private var audioAtual: MediaPlayer? = null
    private var audioRetorno: MediaPlayer? = null

    private var retornando = false

    private val animacoes = mutableMapOf<View, ObjectAnimator>()

    private val audioUrso = R.raw.som_urso
    private val audioBola = R.raw.som_bola
    private val audioCavalinho = R.raw.som_cavalinho
    private val audioMeninoAviao = R.raw.som_aviao
    private val audioDinossauro = R.raw.som_aviao
    private val audioBrinquedos = R.raw.som_brinquedos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_biblioteca_sonora)

        tela = findViewById(R.id.telaBibliotecaSonora)

        tela.post {
            montarTela()
        }
    }

    private fun montarTela() {

        tela.removeAllViews()

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // PLANO DE FUNDO

        adicionarImagem(
            "planofundobibliotecasonora",
            0, 0, 930, 450, sx, sy
        )

        // BOTÃO DE RETORNO

        val voltar = adicionarImagem(
            "botaoretornorosa",
            19, 25, 40, 40, sx, sy
        )

        voltar.contentDescription = "Voltar"
        voltar.isClickable = true

        voltar.setOnClickListener {

            if (!retornando) {

                retornando = true

                animarPulsacao(voltar)

                pararAudio()

                tocarSomRetorno()
            }
        }

        // TÍTULO

        adicionarImagem(
            "titulobibliotecasonora",
            205, 2, 490, 85, sx, sy
        )

        // URSO

        val urso = adicionarImagem(
            "urso",
            40, 172, 140, 180, sx, sy
        )

        configurarToque(urso, audioUrso)

        // BOLA

        val bola = adicionarImagem(
            "bola",
            188, 276, 108, 108, sx, sy
        )

        configurarToque(bola, audioBola)

        // CAVALINHO

        val cavalinho = adicionarImagem(
            "cavalinho",
            258, 135, 175, 170, sx, sy
        )

        configurarToque(cavalinho, audioCavalinho)

        // MENINO COM AVIÃO

        val meninoAviao = adicionarImagem(
            "meninocomaviao",
            450, 78, 180, 200, sx, sy
        )

        configurarToque(meninoAviao, audioMeninoAviao)

        // DINOSSAURO

        val dinossauro = adicionarImagem(
            "dinossauro",
            680, 95, 200, 220, sx, sy
        )

        configurarToque(dinossauro, audioDinossauro)

        // BRINQUEDOS

        val brinquedos = adicionarImagem(
            "brinquedos",
            490, 280, 185, 110, sx, sy
        )

        configurarToque(brinquedos, audioBrinquedos)
    }

    // ADICIONAR IMAGENS

    private fun adicionarImagem(
        nome: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ): ImageView {

        val imagem = ImageView(this)

        val id = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (id != 0) {

            imagem.setImageResource(id)

        } else {

            Toast.makeText(
                this,
                "Imagem não encontrada: $nome",
                Toast.LENGTH_SHORT
            ).show()
        }

        imagem.scaleType = ImageView.ScaleType.FIT_CENTER

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        tela.addView(imagem, parametros)

        return imagem
    }

    // CONFIGURAR TOQUE NAS IMAGENS

    private fun configurarToque(
        imagem: ImageView,
        recursoAudio: Int
    ) {

        imagem.isClickable = true
        imagem.isFocusable = true

        imagem.setOnClickListener {

            if (!retornando) {

                animarPulsacao(imagem)

                tocarAudio(recursoAudio)
            }
        }
    }

    // ANIMAÇÃO DE PULSAÇÃO

    private fun animarPulsacao(view: View) {

        animacoes.remove(view)?.cancel()

        view.scaleX = 1f
        view.scaleY = 1f

        val escalaX = PropertyValuesHolder.ofFloat(
            View.SCALE_X,
            1f,
            0.88f,
            1.12f,
            1f
        )

        val escalaY = PropertyValuesHolder.ofFloat(
            View.SCALE_Y,
            1f,
            0.88f,
            1.12f,
            1f
        )

        val animacao = ObjectAnimator.ofPropertyValuesHolder(
            view,
            escalaX,
            escalaY
        )

        animacao.duration = 380L

        animacoes[view] = animacao

        animacao.start()
    }

    // REPRODUZIR SOM DOS BRINQUEDOS

    private fun tocarAudio(recursoAudio: Int) {

        pararAudio()

        try {

            audioAtual = MediaPlayer.create(
                this,
                recursoAudio
            )

            if (audioAtual == null) {

                Toast.makeText(
                    this,
                    "Não foi possível abrir o áudio.",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

            audioAtual?.setOnCompletionListener { player ->

                if (audioAtual === player) {
                    audioAtual = null
                }

                player.release()
            }

            audioAtual?.start()

        } catch (e: Exception) {

            pararAudio()

            Toast.makeText(
                this,
                "Erro ao reproduzir o áudio.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // SOM DO BOTÃO DE RETORNO - CLIQUE POP

    private fun tocarSomRetorno() {

        try {

            audioRetorno = MediaPlayer.create(
                this,
                R.raw.clique_pop
            )

            val player = audioRetorno

            if (player == null) {

                voltarParaCincoOpcoes()

                return
            }

            player.setOnCompletionListener {

                liberarAudioRetorno()

                voltarParaCincoOpcoes()
            }

            player.setOnErrorListener { _, _, _ ->

                liberarAudioRetorno()

                voltarParaCincoOpcoes()

                true
            }

            player.start()

        } catch (e: Exception) {

            liberarAudioRetorno()

            voltarParaCincoOpcoes()
        }
    }

    // RETORNAR PARA OS CINCO JOGOS

    private fun voltarParaCincoOpcoes() {

        val intent = Intent(
            this,
            CincoOpcoesDosJogosActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

        startActivity(intent)

        finish()
    }

    // PARAR ÁUDIO DOS BRINQUEDOS

    private fun pararAudio() {

        audioAtual?.let { player ->

            try {

                if (player.isPlaying) {
                    player.stop()
                }

            } catch (_: Exception) {
            }

            player.release()
        }

        audioAtual = null
    }

    // LIBERAR ÁUDIO DO RETORNO

    private fun liberarAudioRetorno() {

        audioRetorno?.release()

        audioRetorno = null
    }

    override fun onStop() {

        pararAudio()

        liberarAudioRetorno()

        animacoes.forEach { (view, animacao) ->

            animacao.cancel()

            view.scaleX = 1f
            view.scaleY = 1f
        }

        animacoes.clear()

        retornando = false

        super.onStop()
    }

    override fun onDestroy() {

        pararAudio()

        liberarAudioRetorno()

        super.onDestroy()
    }
}