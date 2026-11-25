package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

class IdentificandoEmocoes2Activity : Activity() {

    private lateinit var tela: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 393f

    private val handler = Handler(Looper.getMainLooper())

    private var popupAberto = false
    private var jogoFinalizado = false
    private var processandoClique = false
    private var navegando = false

    private var audioAtual: MediaPlayer? = null

    private val animacoes = mutableMapOf<View, ObjectAnimator>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_emocoes2)

        tela = findViewById(R.id.telaEmocoes)

        tela.post {
            montarTela()
        }
    }

    // =====================================================
    // MONTAR TELA - NÍVEL 2
    // =====================================================

    private fun montarTela() {

        tela.removeAllViews()

        popupAberto = false
        jogoFinalizado = false
        processandoClique = false
        navegando = false

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // PLANO DE FUNDO

        adicionarImagem(
            R.drawable.planodefundodasemocoes,
            0, 0, 874, 393,
            sx, sy,
            ImageView.ScaleType.FIT_XY
        )

        // GIRAFINHA TRISTE

        adicionarImagem(
            R.drawable.girafatriste,
            352, 195, 175, 205,
            sx, sy
        )

        // TÍTULO

        adicionarImagem(
            R.drawable.tituloemocoesatualizado,
            205, 2, 490, 85,
            sx, sy
        )

        // INSTRUÇÃO

        adicionarImagem(
            R.drawable.indicacaodeapertarnanuvem,
            230, 91, 430, 42,
            sx, sy
        )

        // BOTÃO RETORNO VERDE

        val voltar = adicionarImagem(
            R.drawable.botaoretornoverde,
            19, 25, 40, 40,
            sx, sy
        )

        voltar.contentDescription = "Voltar aos cinco jogos"

        voltar.isClickable = true
        voltar.isFocusable = true

        voltar.setOnClickListener {

            voltarParaJogos()
        }

        // NUVEM BRAVA - INCORRETA

        criarNuvem(
            R.drawable.nuvembrava,
            "brava",
            82, 138, 140, 93,
            sx, sy
        )

        // NUVEM FELIZ - INCORRETA

        criarNuvem(
            R.drawable.nuvemfeliz,
            "feliz",
            232, 138, 140, 93,
            sx, sy
        )

        // NUVEM TRISTE - RESPOSTA CORRETA

        criarNuvem(
            R.drawable.nuvemtriste,
            "triste",
            514, 138, 140, 93,
            sx, sy
        )

        // NUVEM DÚVIDA - INCORRETA

        criarNuvem(
            R.drawable.nuvemduvida,
            "duvida",
            666, 138, 140, 93,
            sx, sy
        )
    }

    // =====================================================
    // CRIAR NUVENS INTERATIVAS
    // =====================================================

    private fun criarNuvem(
        recurso: Int,
        emocao: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ) {

        val nuvem = adicionarImagem(
            recurso,
            x, y, largura, altura,
            sx, sy
        )

        nuvem.isClickable = true
        nuvem.isFocusable = true

        nuvem.contentDescription = "Emoção $emocao"

        nuvem.setOnClickListener {

            if (
                !popupAberto &&
                !jogoFinalizado &&
                !processandoClique &&
                !navegando
            ) {

                processandoClique = true

                // ANIMAÇÃO DE PULSAÇÃO

                animarPulsacao(nuvem)

                // SOM AO TOCAR NA NUVEM

                tocarAudio(R.raw.brilho_suave)

                // AGUARDAR A ANIMAÇÃO

                handler.postDelayed({

                    processandoClique = false

                    if (!navegando && !isFinishing) {

                        verificarResposta(
                            emocao,
                            sx,
                            sy
                        )
                    }

                }, 380L)
            }
        }
    }

    // =====================================================
    // ANIMAÇÃO DE PULSAÇÃO
    // =====================================================

    private fun animarPulsacao(view: View) {

        animacoes.remove(view)?.cancel()

        view.scaleX = 1f
        view.scaleY = 1f

        val escalaX = PropertyValuesHolder.ofFloat(
            View.SCALE_X,
            1f,
            0.88f,
            1.10f,
            1f
        )

        val escalaY = PropertyValuesHolder.ofFloat(
            View.SCALE_Y,
            1f,
            0.88f,
            1.10f,
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

    // =====================================================
    // VERIFICAR RESPOSTA
    // =====================================================

    private fun verificarResposta(
        emocao: String,
        sx: Float,
        sy: Float
    ) {

        if (
            popupAberto ||
            jogoFinalizado ||
            navegando
        ) {
            return
        }

        // A RESPOSTA CORRETA É TRISTE

        val correta = emocao == "triste"

        if (correta) {

            jogoFinalizado = true
        }

        mostrarPopup(
            correta,
            sx,
            sy
        )
    }

    // =====================================================
    // POP-UP PERSONALIZADO DE ACERTO E ERRO
    // =====================================================

    private fun mostrarPopup(
        correta: Boolean,
        sx: Float,
        sy: Float
    ) {

        if (popupAberto) return

        popupAberto = true

        val escala = minOf(sx, sy)

        // CAMADA ESCURECIDA

        val camadaEscura = FrameLayout(this).apply {

            setBackgroundColor(
                Color.argb(85, 35, 45, 40)
            )

            isClickable = true
            isFocusable = true

            alpha = 0f
        }

        tela.addView(
            camadaEscura,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        camadaEscura.animate()
            .alpha(1f)
            .setDuration(350)
            .start()

        // CAIXA DO POP-UP

        val popup = FrameLayout(this).apply {

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(255, 253, 245),
                    Color.rgb(255, 249, 228)
                )
            ).apply {

                cornerRadius = 28f * escala

                setStroke(
                    (3 * escala).toInt().coerceAtLeast(2),
                    Color.rgb(245, 222, 160)
                )
            }

            elevation = 12f * escala

            isClickable = true
        }

        camadaEscura.addView(
            popup,
            FrameLayout.LayoutParams(
                (540 * sx).toInt(),
                (260 * sy).toInt(),
                Gravity.CENTER
            )
        )

        // ANIMAÇÃO DE ENTRADA

        popup.scaleX = 0.6f
        popup.scaleY = 0.6f
        popup.alpha = 0f

        popup.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(500)
            .setInterpolator(
                OvershootInterpolator(1.1f)
            )
            .start()

        // =================================================
        // POSICIONAR ELEMENTOS DO POP-UP
        // =================================================

        fun colocar(
            view: View,
            x: Int,
            y: Int,
            largura: Int,
            altura: Int
        ) {

            val parametros = FrameLayout.LayoutParams(
                (largura * sx).toInt().coerceAtLeast(1),
                (altura * sy).toInt().coerceAtLeast(1)
            )

            parametros.leftMargin = (x * sx).toInt()

            parametros.topMargin = (y * sy).toInt()

            popup.addView(
                view,
                parametros
            )
        }

        // =================================================
        // CRIAR TEXTOS
        // =================================================

        fun criarTexto(
            conteudo: String,
            tamanho: Float,
            cor: Int
        ): TextView {

            return TextView(this).apply {

                text = conteudo

                gravity = Gravity.CENTER

                setTextColor(cor)

                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    tamanho * escala
                )

                typeface = Typeface.DEFAULT_BOLD

                includeFontPadding = false
            }
        }

        // =================================================
        // GIRAFINHA DO POP-UP
        // =================================================

        val girafinha = ImageView(this).apply {

            setImageResource(R.drawable.logoinferior)

            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        colocar(
            girafinha,
            215, 0, 110, 95
        )

        girafinha.scaleX = 0f
        girafinha.scaleY = 0f

        girafinha.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(200)
            .setDuration(600)
            .setInterpolator(
                OvershootInterpolator(1.8f)
            )
            .start()

        // =================================================
        // ESTRELAS DECORATIVAS
        // =================================================

        val estrelaEsquerda = criarTexto(
            "✦",
            26f,
            Color.rgb(245, 210, 135)
        )

        colocar(
            estrelaEsquerda,
            75, 40, 35, 35
        )

        val estrelaDireita = criarTexto(
            "✦",
            26f,
            Color.rgb(245, 210, 135)
        )

        colocar(
            estrelaDireita,
            430, 40, 35, 35
        )

        // =================================================
        // TÍTULO DO POP-UP
        // =================================================

        val titulo = criarTexto(

            if (correta) {

                "Parabéns, explorador!"

            } else {

                "Ops! Não foi dessa vez!"
            },

            32f,

            Color.rgb(165, 138, 199)
        )

        colocar(
            titulo,
            35, 85, 470, 48
        )

        // =================================================
        // MENSAGEM DO POP-UP
        // =================================================

        val mensagem = criarTexto(

            if (correta) {

                "Muito bem! A girafinha está triste!"

            } else {

                "A girafinha não está se sentindo assim.\nTente outra emoção!"
            },

            20f,

            Color.rgb(145, 123, 105)
        )

        colocar(
            mensagem,
            30, 133, 480, 50
        )

        // =================================================
        // CRIAR BOTÕES PASTEL
        // =================================================

        fun criarBotao(
            textoBotao: String,
            corInicio: Int,
            corFim: Int,
            corBorda: Int
        ): TextView {

            return TextView(this).apply {

                text = textoBotao

                gravity = Gravity.CENTER

                setTextColor(
                    Color.rgb(75, 95, 95)
                )

                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    23f * escala
                )

                typeface = Typeface.DEFAULT_BOLD

                includeFontPadding = false

                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        corInicio,
                        corFim
                    )
                ).apply {

                    cornerRadius = 20f * escala

                    setStroke(
                        (2 * escala).toInt().coerceAtLeast(1),
                        corBorda
                    )
                }

                elevation = 4f * escala

                isClickable = true
                isFocusable = true
            }
        }

        // =================================================
        // POP-UP DE ACERTO
        // =================================================

        if (correta) {

            // BOTÃO VOLTAR AZUL PASTEL

            val botaoVoltar = criarBotao(
                "← Voltar",
                Color.rgb(190, 232, 248),
                Color.rgb(155, 215, 240),
                Color.rgb(135, 195, 225)
            )

            colocar(
                botaoVoltar,
                55, 190, 195, 52
            )

            botaoVoltar.setOnClickListener {

                voltarParaJogos()
            }

            // BOTÃO PRÓXIMO NÍVEL VERDE PASTEL

            val botaoProximo = criarBotao(
                "Próximo nível →",
                Color.rgb(205, 242, 210),
                Color.rgb(175, 225, 185),
                Color.rgb(145, 205, 160)
            )

            colocar(
                botaoProximo,
                275, 190, 210, 52
            )

            botaoProximo.setOnClickListener {

                abrirProximoNivel()
            }

            // ANIMAÇÃO SUAVE DO BOTÃO PRÓXIMO

            botaoProximo.animate()
                .scaleX(1.03f)
                .scaleY(1.03f)
                .setStartDelay(750)
                .setDuration(450)
                .withEndAction {

                    botaoProximo.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setStartDelay(0)
                        .setDuration(400)
                        .start()
                }
                .start()

        } else {

            // =============================================
            // POP-UP DE RESPOSTA ERRADA
            // =============================================

            val botaoTentar = criarBotao(
                "Tentar novamente",
                Color.rgb(190, 232, 248),
                Color.rgb(155, 215, 240),
                Color.rgb(135, 195, 225)
            )

            colocar(
                botaoTentar,
                135, 190, 270, 52
            )

            botaoTentar.setOnClickListener {

                if (
                    !processandoClique &&
                    !navegando
                ) {

                    processandoClique = true

                    animarPulsacao(botaoTentar)

                    tocarAudio(R.raw.clique_pop)

                    tela.removeView(camadaEscura)

                    popupAberto = false
                    processandoClique = false

                    animacoes.remove(botaoTentar)?.cancel()
                }
            }
        }
    }

    // =====================================================
    // RETORNAR PARA A TELA DOS CINCO JOGOS
    // =====================================================

    private fun voltarParaJogos() {

        if (navegando) return

        navegando = true

        handler.removeCallbacksAndMessages(null)

        pararAudio()

        tocarSomNavegacao()

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

    // =====================================================
    // ABRIR PRÓXIMO NÍVEL - NÍVEL 3
    // =====================================================

    private fun abrirProximoNivel() {

        if (navegando) return

        navegando = true

        handler.removeCallbacksAndMessages(null)

        pararAudio()

        tocarSomNavegacao()

        val intent = Intent(
            this,
            IdentificandoEmocoes3Activity::class.java
        )

        startActivity(intent)

        finish()
    }

    // =====================================================
    // BOTÃO VOLTAR DO CELULAR
    // =====================================================

    @Suppress("DEPRECATION")
    override fun onBackPressed() {

        voltarParaJogos()
    }

    // =====================================================
    // SOM DOS BOTÕES DE NAVEGAÇÃO
    // =====================================================

    private fun tocarSomNavegacao() {

        var player: MediaPlayer? = null

        try {

            player = MediaPlayer.create(
                applicationContext,
                R.raw.clique_pop
            )

            player?.setOnCompletionListener {

                it.release()
            }

            player?.setOnErrorListener { mp, _, _ ->

                mp.release()

                true
            }

            player?.start()

        } catch (erro: Exception) {

            player?.release()

            Log.e(
                "IdentificandoEmocoes2",
                "Erro no som de navegação",
                erro
            )
        }
    }

    // =====================================================
    // REPRODUZIR SOM DAS NUVENS
    // =====================================================

    private fun tocarAudio(
        recursoAudio: Int
    ) {

        pararAudio()

        try {

            val player = MediaPlayer.create(
                this,
                recursoAudio
            ) ?: return

            audioAtual = player

            player.setOnCompletionListener {

                if (audioAtual === player) {

                    audioAtual = null

                    player.release()
                }
            }

            player.setOnErrorListener { _, _, _ ->

                if (audioAtual === player) {

                    pararAudio()
                }

                true
            }

            player.start()

        } catch (erro: Exception) {

            pararAudio()

            Log.e(
                "IdentificandoEmocoes2",
                "Erro ao reproduzir áudio",
                erro
            )
        }
    }

    // =====================================================
    // PARAR ÁUDIO
    // =====================================================

    private fun pararAudio() {

        val player = audioAtual

        audioAtual = null

        player?.let {

            try {

                it.setOnCompletionListener(null)

                it.setOnErrorListener(null)

                if (it.isPlaying) {

                    it.stop()
                }

            } catch (_: Exception) {
            }

            it.release()
        }
    }

    // =====================================================
    // ADICIONAR IMAGENS
    // =====================================================

    private fun adicionarImagem(
        recurso: Int,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float,
        escala: ImageView.ScaleType =
            ImageView.ScaleType.FIT_CENTER
    ): ImageView {

        val imagem = ImageView(this)

        imagem.setImageResource(recurso)

        imagem.scaleType = escala

        imagem.isClickable = false
        imagem.isFocusable = false

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        tela.addView(
            imagem,
            parametros
        )

        return imagem
    }

    // =====================================================
    // CICLO DE VIDA
    // =====================================================

    override fun onStop() {

        handler.removeCallbacksAndMessages(null)

        pararAudio()

        animacoes.forEach { (view, animacao) ->

            animacao.cancel()

            view.scaleX = 1f
            view.scaleY = 1f
        }

        animacoes.clear()

        processandoClique = false
        navegando = false

        super.onStop()
    }

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        pararAudio()

        super.onDestroy()
    }
}