package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

class OlhinhosCuriosos3Activity : Activity() {

    private lateinit var root: FrameLayout

    private val handler = Handler(Looper.getMainLooper())

    // =====================================================
    // ANIMAIS DO NÍVEL 3
    // =====================================================

    private val animais = listOf(
        Animal("galinha", "galinha", 270, 130, 110, 110),
        Animal("cavalo", "cavalo", 25, 140, 200, 170),
        Animal("pato", "pato", 230, 320, 100, 90),
        Animal("porco", "porco", 630, 260, 120, 100),
        Animal("ovelha", "ovelha", 390, 120, 140, 115)
    )

    private val selecionados = mutableSetOf<String>()

    private var indiceAtual = 0
    private var finalizado = false
    private var navegando = false

    private var pedidoView: TextView? = null
    private var mensagemView: TextView? = null

    private var audioToquePlayer: MediaPlayer? = null

    private val animacoesAnimais =
        mutableMapOf<String, ObjectAnimator>()

    private val imagensAnimais =
        mutableMapOf<String, ImageView>()

    private val halosAnimais =
        mutableMapOf<String, View>()

    data class Animal(
        val imagem: String,
        val nome: String,
        val x: Int,
        val y: Int,
        val largura: Int,
        val altura: Int
    )

    // =====================================================
    // INICIAR ACTIVITY
    // =====================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        root = FrameLayout(this)

        root.setBackgroundColor(
            Color.rgb(203, 224, 237)
        )

        setContentView(root)

        root.post {
            montarJogo()
        }
    }

    // =====================================================
    // MONTAGEM DO JOGO
    // =====================================================

    private fun montarJogo() {

        pararTodasAnimacoes()

        imagensAnimais.clear()

        halosAnimais.clear()

        root.removeAllViews()

        val sx = root.width / 874f
        val sy = root.height / 402f

        // =================================================
        // PLANO DE FUNDO
        // =================================================

        val fundo = ImageView(this).apply {

            setImageResource(
                R.drawable.fundoolhinhoscuriosos3
            )

            scaleType = ImageView.ScaleType.FIT_XY
        }

        root.addView(
            fundo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // BOTÃO RETORNO
        // =================================================

        val voltar = adicionarImagem(
            "botaoretornoamareloclaro",
            19, 25, 40, 40,
            sx, sy
        )

        voltar.isClickable = true
        voltar.isFocusable = true

        voltar.contentDescription = "Voltar aos cinco jogos"

        voltar.setOnClickListener {

            voltarParaJogos()
        }

        // =================================================
        // TÍTULO
        // =================================================

        adicionarImagem(
            "titulo_toquenosanimais",
            250, 10, 370, 70,
            sx, sy
        )

        // =================================================
        // TEXTO INDICANDO O ANIMAL
        // =================================================

        pedidoView = TextView(this).apply {

            text = ""

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                28f * minOf(sx, sy)
            )

            setTextColor(
                Color.rgb(87, 75, 25)
            )

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER

            setShadowLayer(
                2f,
                1f,
                1f,
                Color.WHITE
            )

            includeFontPadding = true

            maxLines = 1

            isSingleLine = true
        }

        adicionarView(
            pedidoView!!,
            240, 80, 394, 42,
            sx, sy
        )

        // =================================================
        // MENSAGEM DE ERRO
        // =================================================

        mensagemView = TextView(this).apply {

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                15f * minOf(sx, sy)
            )

            setTextColor(
                Color.rgb(145, 48, 40)
            )

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER

            includeFontPadding = true

            visibility = View.GONE
        }

        adicionarView(
            mensagemView!!,
            235, 112, 404, 36,
            sx, sy
        )

        // =================================================
        // ADICIONAR OS CINCO ANIMAIS
        // =================================================

        animais.forEachIndexed { index, animal ->

            // CÍRCULO AMARELO
            // INICIALMENTE INVISÍVEL

            val halo = View(this).apply {

                background = GradientDrawable().apply {

                    shape = GradientDrawable.OVAL

                    setColor(
                        Color.argb(100, 255, 221, 40)
                    )

                    setStroke(
                        (4 * minOf(sx, sy))
                            .toInt()
                            .coerceAtLeast(2),

                        Color.rgb(255, 215, 0)
                    )
                }

                visibility =
                    if (animal.imagem in selecionados) {

                        View.VISIBLE

                    } else {

                        View.INVISIBLE
                    }

                isClickable = false
                isFocusable = false
            }

            halosAnimais[animal.imagem] = halo

            adicionarView(
                halo,
                animal.x,
                animal.y,
                animal.largura,
                animal.altura,
                sx,
                sy
            )

            // =================================================
            // IMAGEM DO ANIMAL
            // =================================================

            val imagemId = resources.getIdentifier(
                animal.imagem,
                "drawable",
                packageName
            )

            val imagem = ImageView(this).apply {

                if (imagemId != 0) {

                    setImageResource(imagemId)
                }

                scaleType = ImageView.ScaleType.FIT_CENTER

                contentDescription = animal.nome

                isClickable = false

                isFocusable = false
            }

            adicionarView(
                imagem,
                animal.x,
                animal.y,
                animal.largura,
                animal.altura,
                sx,
                sy
            )

            imagensAnimais[animal.imagem] = imagem

            // ANIMAÇÃO NO PRÓPRIO ANIMAL

            if (
                animal.imagem !in selecionados &&
                !finalizado
            ) {

                animarAnimal(
                    animal.imagem,
                    imagem,
                    index
                )
            }
        }

        // =================================================
        // ÁREAS DE TOQUE DOS ANIMAIS
        // =================================================

        animais.forEach { animal ->

            val areaToque = View(this).apply {

                isClickable = true

                isFocusable = true

                contentDescription = animal.nome

                importantForAccessibility =
                    View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }

            adicionarView(
                areaToque,
                animal.x,
                animal.y,
                animal.largura,
                animal.altura,
                sx,
                sy
            )

            areaToque.setOnClickListener {

                if (!finalizado && !navegando) {

                    tocarSomAudioToque()

                    val halo = halosAnimais[animal.imagem]

                    val imagem = imagensAnimais[animal.imagem]

                    if (halo != null && imagem != null) {

                        verificarAnimal(
                            animal,
                            halo,
                            imagem,
                            sx,
                            sy
                        )
                    }
                }
            }
        }

        // GARANTIR QUE O BOTÃO VOLTAR
        // FIQUE ACIMA DAS ÁREAS DE TOQUE

        voltar.bringToFront()

        atualizarPedido()
    }

    // =====================================================
    // PULSAÇÃO SUAVE NOS ANIMAIS
    // =====================================================

    private fun animarAnimal(
        nomeAnimal: String,
        imagem: ImageView,
        indice: Int
    ) {

        animacoesAnimais
            .remove(nomeAnimal)
            ?.cancel()

        imagem.scaleX = 1f
        imagem.scaleY = 1f

        val escalaX = PropertyValuesHolder.ofFloat(
            View.SCALE_X,
            1f,
            1.09f,
            1f
        )

        val escalaY = PropertyValuesHolder.ofFloat(
            View.SCALE_Y,
            1f,
            1.09f,
            1f
        )

        val animacao = ObjectAnimator.ofPropertyValuesHolder(
            imagem,
            escalaX,
            escalaY
        ).apply {

            duration = 1600L

            startDelay = indice * 180L

            repeatCount = ValueAnimator.INFINITE

            repeatMode = ValueAnimator.RESTART

            interpolator =
                AccelerateDecelerateInterpolator()
        }

        animacoesAnimais[nomeAnimal] = animacao

        animacao.start()
    }

    // =====================================================
    // PARAR A PULSAÇÃO DE UM ANIMAL ENCONTRADO
    // =====================================================

    private fun pararAnimacaoAnimal(
        nomeAnimal: String,
        imagem: ImageView
    ) {

        animacoesAnimais
            .remove(nomeAnimal)
            ?.cancel()

        imagem.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(180L)
            .start()
    }

    // =====================================================
    // PARAR TODAS AS ANIMAÇÕES
    // =====================================================

    private fun pararTodasAnimacoes() {

        animacoesAnimais.values.forEach {

            it.cancel()
        }

        animacoesAnimais.clear()

        imagensAnimais.values.forEach {

            it.animate().cancel()

            it.scaleX = 1f
            it.scaleY = 1f
        }
    }

    // =====================================================
    // SOM AO TOCAR NOS ANIMAIS
    // =====================================================

    private fun tocarSomAudioToque() {

        pararAudio()

        try {

            val player = MediaPlayer.create(
                this,
                R.raw.audio_toque
            ) ?: return

            audioToquePlayer = player

            player.setOnCompletionListener {

                if (audioToquePlayer === it) {

                    audioToquePlayer = null

                    it.release()
                }
            }

            player.setOnErrorListener { mp, _, _ ->

                if (audioToquePlayer === mp) {

                    audioToquePlayer = null

                    mp.release()
                }

                true
            }

            player.start()

        } catch (erro: Exception) {

            pararAudio()

            Log.e(
                "OlhinhosCuriosos3",
                "Erro ao reproduzir áudio",
                erro
            )
        }
    }

    // =====================================================
    // PARAR ÁUDIO
    // =====================================================

    private fun pararAudio() {

        val player = audioToquePlayer

        audioToquePlayer = null

        player?.let {

            it.setOnCompletionListener(null)

            it.setOnErrorListener(null)

            it.release()
        }
    }

    // =====================================================
    // VERIFICAR ANIMAL
    // =====================================================

    private fun verificarAnimal(
        animal: Animal,
        halo: View,
        imagem: ImageView,
        sx: Float,
        sy: Float
    ) {

        if (
            finalizado ||
            navegando ||
            animal.imagem in selecionados
        ) {
            return
        }

        if (indiceAtual >= animais.size) return

        val esperado = animais[indiceAtual]

        // =================================================
        // ANIMAL ERRADO
        // =================================================

        if (animal.imagem != esperado.imagem) {

            mensagemView?.apply {

                text = "Animal errado, tente novamente!"

                setTextColor(
                    Color.rgb(145, 48, 40)
                )

                visibility = View.VISIBLE
            }

            handler.removeCallbacksAndMessages(null)

            handler.postDelayed({

                if (!finalizado) {

                    mensagemView?.visibility = View.GONE
                }

            }, 1800L)

            return
        }

        // =================================================
        // ANIMAL CORRETO
        // =================================================

        selecionados.add(animal.imagem)

        // PARAR PULSAÇÃO DO ANIMAL CORRETO

        pararAnimacaoAnimal(
            animal.imagem,
            imagem
        )

        // MOSTRAR O CÍRCULO AMARELO

        halo.visibility = View.VISIBLE

        halo.alpha = 0f

        halo.scaleX = 0.85f
        halo.scaleY = 0.85f

        // ANIMAÇÃO DE ENTRADA DO CÍRCULO

        halo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300L)
            .setInterpolator(
                OvershootInterpolator(1.2f)
            )
            .start()

        indiceAtual++

        if (indiceAtual == animais.size) {

            finalizarJogo(sx, sy)

        } else {

            atualizarPedido()
        }
    }

    // =====================================================
    // ATUALIZAR ANIMAL SOLICITADO
    // =====================================================

    private fun atualizarPedido() {

        if (indiceAtual >= animais.size) {
            return
        }

        val animal = animais[indiceAtual]

        val artigo = when (animal.nome) {

            "cavalo",
            "pato",
            "porco" -> "no"

            "galinha",
            "ovelha" -> "na"

            else -> "no"
        }

        pedidoView?.apply {

            text = "Toque $artigo ${animal.nome}"

            visibility = View.VISIBLE
        }

        mensagemView?.visibility = View.GONE
    }

    // =====================================================
    // FINALIZAR JOGO
    // =====================================================

    private fun finalizarJogo(
        sx: Float,
        sy: Float
    ) {

        finalizado = true

        pararTodasAnimacoes()

        handler.removeCallbacksAndMessages(null)

        pedidoView?.visibility = View.GONE

        mensagemView?.visibility = View.GONE

        handler.postDelayed({

            if (!isFinishing && !isDestroyed && !navegando) {

                mostrarPopupVitoria(sx, sy)
            }

        }, 450L)
    }

    // =====================================================
    // POP-UP FINAL DE VITÓRIA
    // =====================================================

    private fun mostrarPopupVitoria(
        sx: Float,
        sy: Float
    ) {

        val escala = minOf(sx, sy)

        // FUNDO LEVEMENTE ESCURECIDO

        val fundoEscuro = View(this).apply {

            setBackgroundColor(
                Color.argb(85, 35, 45, 40)
            )

            alpha = 0f

            isClickable = true
        }

        root.addView(
            fundoEscuro,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        fundoEscuro.animate()
            .alpha(1f)
            .setDuration(350)
            .start()

        // =================================================
        // POP-UP COM CORES SUAVES
        // =================================================

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
                    (3 * escala)
                        .toInt()
                        .coerceAtLeast(2),

                    Color.rgb(245, 222, 160)
                )
            }

            elevation = 12f

            isClickable = true
        }

        val popupParams = FrameLayout.LayoutParams(
            (540 * sx).toInt(),
            (260 * sy).toInt(),
            Gravity.CENTER
        )

        root.addView(
            popup,
            popupParams
        )

        // =================================================
        // ANIMAÇÃO DE ENTRADA DO POP-UP
        // =================================================

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
        // POSICIONAR ELEMENTOS
        // =================================================

        fun colocar(
            view: View,
            x: Int,
            y: Int,
            largura: Int,
            altura: Int
        ) {

            val params = FrameLayout.LayoutParams(
                (largura * sx)
                    .toInt()
                    .coerceAtLeast(1),

                (altura * sy)
                    .toInt()
                    .coerceAtLeast(1)
            ).apply {

                leftMargin = (x * sx).toInt()

                topMargin = (y * sy).toInt()
            }

            popup.addView(view, params)
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

        // =================================================
        // GIRAFINHA
        // =================================================

        val girafinha = ImageView(this).apply {

            val imagemId = resources.getIdentifier(
                "logoinferior",
                "drawable",
                packageName
            )

            if (imagemId != 0) {

                setImageResource(imagemId)
            }

            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        colocar(
            girafinha,
            215, 0, 110, 95
        )

        // ANIMAÇÃO DA GIRAFINHA

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
            "Parabéns, explorador!",
            32f,
            Color.rgb(165, 138, 199)
        )

        colocar(
            titulo,
            35, 85, 470, 48
        )

        // =================================================
        // MENSAGEM FINAL
        // =================================================

        val subtitulo = criarTexto(
            "Você encontrou todos os animais!\nVocê completou todos os níveis!",
            20f,
            Color.rgb(145, 123, 105)
        )

        colocar(
            subtitulo,
            35, 133, 470, 50
        )

        // =================================================
        // CRIAR BOTÃO VOLTAR
        // =================================================

        val botaoVoltar = TextView(this).apply {

            text = "← Voltar"

            setTextColor(
                Color.rgb(75, 95, 95)
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                23f * escala
            )

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER

            includeFontPadding = false

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(190, 232, 248),
                    Color.rgb(155, 215, 240)
                )
            ).apply {

                cornerRadius = 20f * escala

                setStroke(
                    (2 * escala)
                        .toInt()
                        .coerceAtLeast(1),

                    Color.rgb(135, 195, 225)
                )
            }

            elevation = 4f

            isClickable = true

            isFocusable = true
        }

        // =================================================
        // BOTÃO VOLTAR CENTRALIZADO
        // =================================================

        colocar(
            botaoVoltar,
            150, 195, 240, 52
        )

        botaoVoltar.setOnClickListener {

            voltarParaJogos()
        }

        // ANIMAÇÃO SUAVE DO BOTÃO VOLTAR

        botaoVoltar.animate()
            .scaleX(1.03f)
            .scaleY(1.03f)
            .setStartDelay(750)
            .setDuration(450)
            .withEndAction {

                botaoVoltar.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(0)
                    .setDuration(400)
                    .start()
            }
            .start()
    }

    // =====================================================
    // VOLTAR PARA A TELA DOS CINCO JOGOS
    // =====================================================

    private fun voltarParaJogos() {

        if (navegando || isFinishing) return

        navegando = true

        handler.removeCallbacksAndMessages(null)

        pararTodasAnimacoes()

        pararAudio()

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

        voltarParaJogos()
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

        val imagemId = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (imagemId != 0) {

            imagem.setImageResource(imagemId)
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        adicionarView(
            imagem,
            x,
            y,
            largura,
            altura,
            sx,
            sy
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
            (largura * sx)
                .toInt()
                .coerceAtLeast(1),

            (altura * sy)
                .toInt()
                .coerceAtLeast(1)
        )

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        root.addView(
            view,
            parametros
        )
    }

    // =====================================================
    // RETOMAR ANIMAÇÕES
    // =====================================================

    override fun onResume() {

        super.onResume()

        if (!finalizado && !navegando) {

            animais.forEachIndexed { index, animal ->

                if (animal.imagem !in selecionados) {

                    imagensAnimais[animal.imagem]?.let {

                        animarAnimal(
                            animal.imagem,
                            it,
                            index
                        )
                    }
                }
            }
        }
    }

    // =====================================================
    // PAUSAR ANIMAÇÕES E ÁUDIO
    // =====================================================

    override fun onPause() {

        pararTodasAnimacoes()

        pararAudio()

        super.onPause()
    }

    // =====================================================
    // ENCERRAR ACTIVITY
    // =====================================================

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        pararTodasAnimacoes()

        pararAudio()

        super.onDestroy()
    }
}