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

class DescubraOCheiroActivity : Activity() {

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

        setContentView(R.layout.activity_descubra_o_cheiro)

        tela = findViewById(R.id.telaDescubraCheiro)

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

        // PLANO DE FUNDO

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

        // BOTÃO RETORNO

        val voltar = adicionarImagem(
            "botaoretornoazul",
            19, 25, 40, 40,
            sx, sy
        )

        voltar.contentDescription = "Voltar"
        voltar.isClickable = true
        voltar.isFocusable = true

        voltar.setOnClickListener {

            voltarParaCincoOpcoes()
        }

        // TÍTULO

        adicionarImagem(
            "titulodescubracomocheiro",
            205, 2, 490, 85,
            sx, sy
        )

        // PERSONAGEM

        adicionarImagem(
            "meninadeverde",
            55, 190, 150, 210,
            sx, sy
        )

        // BALÃO CORRIGIDO

        criarBalao(sx, sy)

        // BRIGADEIRO - RESPOSTA CORRETA

        criarOpcao(
            nomeImagem = "brigadeiro",
            x = 395,
            y = 105,
            sx = sx,
            sy = sy,
            deslocamentoImagemX = 8
        ) {
            mostrarResposta(true, sx, sy)
        }

        // PIZZA

        criarOpcao(
            nomeImagem = "pizza",
            x = 600,
            y = 105,
            sx = sx,
            sy = sy
        ) {
            mostrarResposta(false, sx, sy)
        }

        // BRÓCOLIS

        criarOpcao(
            nomeImagem = "brocolis",
            x = 395,
            y = 245,
            sx = sx,
            sy = sy
        ) {
            mostrarResposta(false, sx, sy)
        }

        // MACARRÃO

        criarOpcao(
            nomeImagem = "macarrao",
            x = 600,
            y = 245,
            sx = sx,
            sy = sy
        ) {
            mostrarResposta(false, sx, sy)
        }
    }

    // =====================================================
    // BALÃO DA PERGUNTA CORRIGIDO
    // =====================================================

    private fun criarBalao(
        sx: Float,
        sy: Float
    ) {

        // CONTAINER DO BALÃO

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

            setImageResource(R.drawable.balaopergunta)

            scaleType = ImageView.ScaleType.FIT_XY
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

            text = "Qual alimento tem\num cheiro doce?"

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#303030")
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
        }

        val parametrosTexto = FrameLayout.LayoutParams(
            (245 * sx).toInt().coerceAtLeast(1),
            (65 * sy).toInt().coerceAtLeast(1)
        )

        parametrosTexto.leftMargin = (27 * sx).toInt()

        parametrosTexto.topMargin = (15 * sy).toInt()

        containerBalao.addView(
            pergunta,
            parametrosTexto
        )
    }

    // =====================================================
    // CARTÕES DOS ALIMENTOS
    // =====================================================

    private fun criarOpcao(
        nomeImagem: String,
        x: Int,
        y: Int,
        sx: Float,
        sy: Float,
        deslocamentoImagemX: Int = 0,
        acao: () -> Unit
    ) {

        // SOMBRA

        val sombra = View(this).apply {

            background = GradientDrawable().apply {

                setColor(
                    Color.parseColor("#E8E7E7")
                )

                cornerRadius = 17f * sx
            }

            isClickable = false
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

        // CARTÃO AZUL

        val cartao = FrameLayout(this).apply {

            background = GradientDrawable().apply {

                setColor(
                    Color.parseColor("#96CBEB")
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

        // IMAGEM DO ALIMENTO

        val alimento = ImageView(this)

        val idImagem = resources.getIdentifier(
            nomeImagem,
            "drawable",
            packageName
        )

        if (idImagem != 0) {

            alimento.setImageResource(idImagem)

        } else {

            Log.e(
                "DescubraOCheiro",
                "Imagem não encontrada: $nomeImagem"
            )
        }

        alimento.scaleType =
            ImageView.ScaleType.FIT_CENTER

        alimento.contentDescription = nomeImagem

        alimento.isClickable = false
        alimento.isFocusable = false

        val parametrosImagem = FrameLayout.LayoutParams(
            (115 * sx).toInt().coerceAtLeast(1),
            (84 * sy).toInt().coerceAtLeast(1),
            Gravity.CENTER
        )

        cartao.addView(
            alimento,
            parametrosImagem
        )

        alimento.translationX =
            deslocamentoImagemX * sx

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

                        acao()
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
            1f, 0.88f, 1.10f, 1f
        )

        val escalaY = PropertyValuesHolder.ofFloat(
            View.SCALE_Y,
            1f, 0.88f, 1.10f, 1f
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

        // FUNDO ESCURECIDO

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

        // ANIMAÇÃO DO POP-UP

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

        // POSICIONAR ELEMENTOS

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

            params.leftMargin = (x * sx).toInt()
            params.topMargin = (y * sy).toInt()

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

        // ESTRELAS

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

        // TÍTULO

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

        // MENSAGEM

        val mensagem = criarTexto(

            if (correta) {

                "O brigadeiro tem um cheirinho doce!"

            } else {

                "Vamos tentar novamente?"
            },

            21f,

            Color.rgb(145, 123, 105)
        )

        colocar(
            mensagem,
            35, 135, 470, 38
        )

        // CRIAR BOTÕES

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
        // RESPOSTA CORRETA
        // =================================================

        if (correta) {

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

                voltarParaCincoOpcoes()
            }

            // PRÓXIMO NÍVEL

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

                if (!navegando) {

                    botaoProximo.animate().cancel()

                    animarPulsacao(botaoProximo)

                    abrirProximaFase()
                }
            }

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
            // RESPOSTA ERRADA
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

                if (!processandoClique && !navegando) {

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
    // RETORNO DIRETO PARA A TELA DOS CINCO JOGOS
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
    // BOTÃO VOLTAR FÍSICO DO CELULAR
    // =====================================================

    @Suppress("DEPRECATION")
    override fun onBackPressed() {

        voltarParaCincoOpcoes()
    }

    // =====================================================
    // ABRIR PRÓXIMO NÍVEL
    // =====================================================

    private fun abrirProximaFase() {

        if (navegando) return

        navegando = true

        pararAudio()

        tocarSomNavegacao()

        try {

            val classeProximaFase = Class.forName(
                "$packageName.DescubraOCheiro2Activity"
            )

            val intent = Intent(
                this,
                classeProximaFase
            )

            startActivity(intent)

            finish()

        } catch (erro: Exception) {

            navegando = false

            Log.e(
                "DescubraOCheiro",
                "Erro ao abrir próxima fase",
                erro
            )

            mostrarAvisoProximoNivel()
        }
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
                "DescubraOCheiro",
                "Erro no som de navegação",
                erro
            )
        }
    }

    // =====================================================
    // AVISO PERSONALIZADO
    // =====================================================

    private fun mostrarAvisoProximoNivel() {

        val dialogo = android.app.Dialog(this)

        val caixa = android.widget.LinearLayout(this).apply {

            orientation =
                android.widget.LinearLayout.VERTICAL

            gravity = Gravity.CENTER

            setPadding(35, 30, 35, 30)

            background = GradientDrawable().apply {

                setColor(Color.parseColor("#FFF9E4"))

                cornerRadius = 28f

                setStroke(
                    3,
                    Color.parseColor("#F5DEA0")
                )
            }
        }

        val mensagem = TextView(this).apply {

            text = "O próximo nível ainda não está disponível!"

            textSize = 20f

            gravity = Gravity.CENTER

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(
                Color.parseColor("#A58AC7")
            )

            setPadding(10, 10, 10, 25)
        }

        caixa.addView(mensagem)

        val botao = TextView(this).apply {

            text = "Voltar"

            textSize = 18f

            gravity = Gravity.CENTER

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(
                Color.parseColor("#4B5F5F")
            )

            setPadding(40, 15, 40, 15)

            background = GradientDrawable().apply {

                setColor(Color.parseColor("#BEE8F8"))

                cornerRadius = 20f
            }

            setOnClickListener {

                dialogo.dismiss()

                voltarParaCincoOpcoes()
            }
        }

        caixa.addView(botao)

        dialogo.setContentView(caixa)

        dialogo.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        dialogo.show()
    }

    // =====================================================
    // REPRODUZIR ÁUDIO DOS ALIMENTOS
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
                "DescubraOCheiro",
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
    // ADICIONAR IMAGEM
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
                "DescubraOCheiro",
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
    // ADICIONAR VIEW
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

        parametros.leftMargin = (x * sx).toInt()

        parametros.topMargin = (y * sy).toInt()

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