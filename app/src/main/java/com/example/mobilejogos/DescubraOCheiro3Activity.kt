package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.PorterDuff
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

class DescubraOCheiro3Activity : Activity() {

    private lateinit var tela: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 393f

    private val handler = Handler(Looper.getMainLooper())

    private var jogoFinalizado = false
    private var popupAberto = false
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

        setContentView(R.layout.activity_descubra_o_cheiro3)

        tela = findViewById(R.id.telaDescubraCheiro3)

        tela.post {
            montarTela()
        }
    }

    // =====================================================
    // MONTAR TELA
    // =====================================================

    private fun montarTela() {

        tela.removeAllViews()

        jogoFinalizado = false
        popupAberto = false
        processandoClique = false
        navegando = false

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // NOVO PLANO DE FUNDO

        val fundo = ImageView(this).apply {

            setImageResource(
                R.drawable.planodefundodescubra
            )

            scaleType = ImageView.ScaleType.FIT_XY

            isClickable = false
            isFocusable = false
        }

        tela.addView(
            fundo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // BOTÃO RETORNO AMARELO

        val voltar = adicionarImagem(
            "botaoretornoamarelo",
            19, 25, 40, 40,
            sx, sy
        )

        voltar.contentDescription = "Voltar"
        voltar.isClickable = true
        voltar.isFocusable = true

        voltar.setOnClickListener {

            voltarParaCincoOpcoes()
        }

        // TÍTULO AMARELO

        adicionarImagem(
            "descubracomocheiroamarelo",
            205, 2, 490, 85,
            sx, sy
        )

        // PERSONAGENS

        adicionarImagem(
            "criancas",
            55, 190, 150, 210,
            sx, sy
        )

        // BALÃO AMARELO CORRIGIDO

        criarBalao(sx, sy)

        // TAMBOR - INCORRETO

        criarOpcao(
            nomeImagem = "tambor",
            x = 395,
            y = 105,
            sx = sx,
            sy = sy
        )

        // URSO - INCORRETO

        criarOpcao(
            nomeImagem = "urso2",
            x = 600,
            y = 105,
            sx = sx,
            sy = sy
        )

        // LIXO - RESPOSTA CORRETA

        criarOpcao(
            nomeImagem = "lixo",
            x = 395,
            y = 245,
            sx = sx,
            sy = sy
        )

        // MOCHILA - INCORRETO

        criarOpcao(
            nomeImagem = "mochila",
            x = 600,
            y = 245,
            sx = sx,
            sy = sy
        )
    }

    // =====================================================
    // BALÃO AMARELO CORRIGIDO
    // =====================================================

    private fun criarBalao(
        sx: Float,
        sy: Float
    ) {

        val containerBalao = FrameLayout(this).apply {

            isClickable = false
            isFocusable = false
        }

        adicionarView(
            containerBalao,
            75, 85, 300, 135,
            sx, sy
        )

        // IMAGEM DO BALÃO

        val balao = ImageView(this).apply {

            setImageResource(
                R.drawable.balaopergunta
            )

            scaleType = ImageView.ScaleType.FIT_XY

            setColorFilter(
                Color.parseColor("#FFE49A"),
                PorterDuff.Mode.SRC_IN
            )

            isClickable = false
            isFocusable = false
        }

        containerBalao.addView(
            balao,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // TEXTO CENTRALIZADO

        val pergunta = TextView(this).apply {

            text = "Qual objeto tem\num cheiro ruim?"

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#5B4926")
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                18f * minOf(sx, sy)
            )

            typeface = Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

            includeFontPadding = false

            maxLines = 2

            setPadding(0, 0, 0, 0)

            isClickable = false
        }

        val parametrosTexto = FrameLayout.LayoutParams(
            (245 * sx).toInt().coerceAtLeast(1),
            (65 * sy).toInt().coerceAtLeast(1)
        )

        parametrosTexto.leftMargin =
            (27 * sx).toInt()

        parametrosTexto.topMargin =
            (15 * sy).toInt()

        containerBalao.addView(
            pergunta,
            parametrosTexto
        )
    }

    // =====================================================
    // CARTÕES DOS OBJETOS
    // =====================================================

    private fun criarOpcao(
        nomeImagem: String,
        x: Int,
        y: Int,
        sx: Float,
        sy: Float
    ) {

        // SOMBRA DO CARTÃO

        val sombra = View(this).apply {

            background = GradientDrawable().apply {

                setColor(
                    Color.parseColor("#E8E7E7")
                )

                cornerRadius = 17f * sx
            }

            isClickable = false
            isFocusable = false
        }

        adicionarView(
            sombra,
            x + 13,
            y + 13,
            155,
            100,
            sx,
            sy
        )

        // CARTÃO AMARELO

        val cartao = FrameLayout(this).apply {

            background = GradientDrawable().apply {

                setColor(
                    Color.parseColor("#FFE5A3")
                )

                cornerRadius = 17f * sx
            }

            clipChildren = false
            clipToPadding = false
        }

        adicionarView(
            cartao,
            x, y, 155, 100,
            sx, sy
        )

        // IMAGEM DO OBJETO

        val objeto = ImageView(this)

        val idImagem = resources.getIdentifier(
            nomeImagem,
            "drawable",
            packageName
        )

        if (idImagem != 0) {

            objeto.setImageResource(idImagem)

        } else {

            Log.e(
                "DescubraOCheiro3",
                "Imagem não encontrada: $nomeImagem"
            )
        }

        objeto.scaleType =
            ImageView.ScaleType.FIT_CENTER

        objeto.contentDescription = nomeImagem

        objeto.isClickable = false
        objeto.isFocusable = false

        val parametrosImagem = FrameLayout.LayoutParams(
            (115 * sx).toInt().coerceAtLeast(1),
            (84 * sy).toInt().coerceAtLeast(1),
            Gravity.CENTER
        )

        cartao.addView(
            objeto,
            parametrosImagem
        )

        // ÁREA TRANSPARENTE DE CLIQUE

        val areaClique = View(this).apply {

            setBackgroundColor(Color.TRANSPARENT)

            isClickable = true
            isFocusable = true

            contentDescription =
                "Selecionar $nomeImagem"
        }

        cartao.addView(
            areaClique,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // ANIMAÇÃO E SOM AO CLICAR

        areaClique.setOnClickListener {

            if (
                !jogoFinalizado &&
                !popupAberto &&
                !processandoClique &&
                !navegando
            ) {

                processandoClique = true

                animarPulsacao(cartao)

                tocarAudio(R.raw.brilho_suave)

                handler.postDelayed({

                    processandoClique = false

                    if (!navegando && !isFinishing) {

                        mostrarResposta(
                            nomeImagem == "lixo",
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
    // CONFERIR RESPOSTA
    // =====================================================

    private fun mostrarResposta(
        correta: Boolean,
        sx: Float,
        sy: Float
    ) {

        if (
            jogoFinalizado ||
            popupAberto ||
            navegando
        ) {
            return
        }

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
    // POP-UP PERSONALIZADO
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

        // POSICIONAR ELEMENTOS DO POP-UP

        fun colocar(
            view: View,
            x: Int,
            y: Int,
            largura: Int,
            altura: Int
        ) {

            val params = FrameLayout.LayoutParams(
                (largura * sx).toInt().coerceAtLeast(1),
                (altura * sy).toInt().coerceAtLeast(1)
            )

            params.leftMargin =
                (x * sx).toInt()

            params.topMargin =
                (y * sy).toInt()

            popup.addView(view, params)
        }

        // CRIAR TEXTOS

        fun criarTexto(
            conteudo: String,
            tamanho: Float,
            cor: Int
        ): TextView {

            return TextView(this).apply {

                text = conteudo

                setTextColor(cor)

                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    tamanho * escala
                )

                typeface = Typeface.DEFAULT_BOLD

                gravity = Gravity.CENTER

                includeFontPadding = false
            }
        }

        // GIRAFINHA

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

        // ESTRELAS DECORATIVAS

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

        // TÍTULO DO POP-UP

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

        // MENSAGEM DO POP-UP

        val mensagem = criarTexto(

            if (correta) {

                "Você descobriu todos os cheiros!\nO lixo tem um cheiro ruim."

            } else {

                "Vamos tentar novamente?"
            },

            21f,

            Color.rgb(145, 123, 105)
        )

        colocar(
            mensagem,
            35, 133, 470, 50
        )

        // CRIAR BOTÕES DO POP-UP

        fun criarBotao(
            textoBotao: String,
            corInicio: Int,
            corFim: Int,
            corBorda: Int
        ): TextView {

            return TextView(this).apply {

                text = textoBotao

                setTextColor(
                    Color.rgb(75, 95, 95)
                )

                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    23f * escala
                )

                typeface = Typeface.DEFAULT_BOLD

                gravity = Gravity.CENTER

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
        // POP-UP FINAL - APENAS BOTÃO VOLTAR
        // =================================================

        if (correta) {

            val botaoVoltar = criarBotao(
                "← Voltar",
                Color.rgb(190, 232, 248),
                Color.rgb(155, 215, 240),
                Color.rgb(135, 195, 225)
            )

            // BOTÃO CENTRALIZADO

            colocar(
                botaoVoltar,
                150, 195, 240, 52
            )

            botaoVoltar.setOnClickListener {

                voltarParaCincoOpcoes()
            }

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

                    handler.postDelayed({

                        tela.removeView(camadaEscura)

                        popupAberto = false
                        processandoClique = false

                        animacoes.remove(botaoTentar)?.cancel()

                    }, 380L)
                }
            }
        }
    }

    // =====================================================
    // RETORNAR PARA A TELA DOS CINCO JOGOS
    // =====================================================

    private fun voltarParaCincoOpcoes() {

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
    // BOTÃO VOLTAR DO CELULAR
    // =====================================================

    @Suppress("DEPRECATION")
    override fun onBackPressed() {

        voltarParaCincoOpcoes()
    }

    // =====================================================
    // SOM DE NAVEGAÇÃO
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
                "DescubraOCheiro3",
                "Erro no som de navegação",
                erro
            )
        }
    }

    // =====================================================
    // REPRODUZIR ÁUDIO DOS OBJETOS
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
                "DescubraOCheiro3",
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
        nome: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ): ImageView {

        val imagem = ImageView(this)

        val idImagem = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (idImagem != 0) {

            imagem.setImageResource(idImagem)

        } else {

            Log.e(
                "DescubraOCheiro3",
                "Imagem não encontrada: $nome"
            )
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        imagem.isClickable = false
        imagem.isFocusable = false

        adicionarView(
            imagem,
            x, y, largura, altura,
            sx, sy
        )

        return imagem
    }

    // =====================================================
    // ADICIONAR ELEMENTOS NA TELA
    // =====================================================

    private fun adicionarView(
        view: View,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ) {

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        tela.addView(
            view,
            parametros
        )
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