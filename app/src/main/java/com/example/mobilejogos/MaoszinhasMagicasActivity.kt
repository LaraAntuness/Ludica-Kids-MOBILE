package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
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
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.vectordrawable.graphics.drawable.Animatable2Compat

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MaoszinhasMagicasActivity : Activity() {

    private lateinit var root: FrameLayout

    private val larguraBase = 922f
    private val alturaBase = 363f

    private val handler = Handler(Looper.getMainLooper())

    private val alvosGif =
        mutableListOf<CustomTarget<GifDrawable>>()

    private val animaisConcluidos = mutableSetOf<String>()

    private val animaisIniciados = mutableSetOf<String>()

    private val imagensAnimais =
        mutableMapOf<String, ImageView>()

    private val gifsAnimais =
        mutableMapOf<String, GifDrawable>()

    private val animacoesAnimais =
        mutableMapOf<String, ObjectAnimator>()

    private val estrelasAtivas = mutableListOf<View>()

    private val totalAnimais = 4

    private var jogoFinalizado = false
    private var navegando = false
    private var telaAtiva = false
    private var popupAberto = false

    private var audioAtual: MediaPlayer? = null
    private var audioFundo: MediaPlayer? = null
    private var audioBrilho: MediaPlayer? = null

    private val proximaFaseActivity =
        "MaoszinhasMagicas2Activity"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(
            R.layout.activity_maoszinhas_magicas
        )

        root = findViewById(
            R.id.telaMaoszinhasMagicas
        )

        root.post {

            if (!isFinishing && !isDestroyed) {

                montarJogo()
            }
        }
    }

    // =====================================================
    // MONTAR JOGO
    // =====================================================

    private fun montarJogo() {

        pararTodasPulsacoes()

        limparGlitter()

        limparGifs()

        root.removeAllViews()

        animaisConcluidos.clear()
        animaisIniciados.clear()
        imagensAnimais.clear()

        jogoFinalizado = false
        navegando = false
        popupAberto = false

        val sx = root.width.toFloat() / larguraBase
        val sy = root.height.toFloat() / alturaBase

        val escala = minOf(sx, sy)

        // =================================================
        // PLANO DE FUNDO ORIGINAL
        // =================================================

        val fundo = ImageView(this).apply {

            setImageResource(
                R.drawable.planomaoszinhasmagicas
            )

            scaleType = ImageView.ScaleType.FIT_XY

            isClickable = false
            isFocusable = false
        }

        root.addView(
            fundo,
            0,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // BOTÃO VOLTAR
        // =================================================

        val voltar = adicionarImagem(
            "botaoretornocinza",
            19, 25, 40, 40,
            sx, sy
        )

        voltar.contentDescription =
            "Voltar aos cinco jogos"

        voltar.isClickable = true
        voltar.isFocusable = true
        voltar.isSoundEffectsEnabled = false

        voltar.setOnClickListener {

            voltarParaJogos()
        }

        // =================================================
        // TÍTULO MENOR E MAIS ACIMA
        // =================================================

        val titulo = adicionarImagem(
            "titulodescubracomotoque",
            251, -4, 420, 70,
            sx, sy
        )

        // =================================================
        // BALÃO DE INSTRUÇÃO - TOQUE NOS ANIMAIS
        // =================================================

        val instrucao = TextView(this).apply {

            text = "Toque nos animais"

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                19f * escala
            )

            setTextColor(
                Color.rgb(220, 140, 45)
            )

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER

            includeFontPadding = false

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(255, 253, 235),
                    Color.rgb(255, 239, 192)
                )
            ).apply {

                cornerRadius = 22f * escala

                setStroke(
                    (2f * escala)
                        .toInt()
                        .coerceAtLeast(1),

                    Color.rgb(245, 205, 125)
                )
            }

            elevation = 5f * escala

            isClickable = false
            isFocusable = false
        }

        adicionarView(
            instrucao,
            331, 65, 260, 35,
            sx, sy
        )

        // ANIMAÇÃO DE ENTRADA DO BALÃO

        instrucao.alpha = 0f

        instrucao.scaleX = 0.7f
        instrucao.scaleY = 0.7f

        instrucao.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(550L)
            .setInterpolator(
                OvershootInterpolator(1.3f)
            )
            .start()

        // =================================================
        // LOBO
        // =================================================

        adicionarAnimal(
            "lobo.gif",
            77, 40, 210, 180,
            sx, sy,
            0
        )

        // =================================================
        // SAPO
        // =================================================

        adicionarAnimal(
            "sapo.gif",
            420, 185, 210, 180,
            sx, sy,
            1
        )

        // =================================================
        // CASTOR
        // =================================================

        adicionarAnimal(
            "castor.gif",
            285, 210, 210, 180,
            sx, sy,
            2
        )

        // =================================================
        // MACACO
        // =================================================

        adicionarAnimal(
            "macaco.gif",
            600, 100, 300, 250,
            sx, sy,
            3
        )

        // MANTER ELEMENTOS SUPERIORES VISÍVEIS

        titulo.bringToFront()

        instrucao.bringToFront()

        voltar.bringToFront()

        // INICIAR MÚSICA DE FUNDO

        if (telaAtiva) {

            iniciarAudioFundo()
        }
    }

    // =====================================================
    // ADICIONAR ANIMAIS COM GIF
    // =====================================================

    private fun adicionarAnimal(
        arquivo: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float,
        indice: Int
    ) {

        val animal = ImageView(this).apply {

            scaleType =
                ImageView.ScaleType.FIT_CENTER

            contentDescription =
                arquivo.substringBefore(".")

            isClickable = false
            isFocusable = false

            isSoundEffectsEnabled = false
        }

        adicionarView(
            animal,
            x, y, largura, altura,
            sx, sy
        )

        imagensAnimais[arquivo] = animal

        val caminho =
            "file:///android_asset/$arquivo"

        val alvo = object : CustomTarget<GifDrawable>() {

            override fun onResourceReady(
                resource: GifDrawable,
                transition: Transition<in GifDrawable>?
            ) {

                if (
                    navegando ||
                    isFinishing ||
                    isDestroyed
                ) return

                // GIF COMEÇA PARADO

                resource.stop()

                resource.setVisible(true, false)

                // EXECUTAR GIF APENAS UMA VEZ

                resource.setLoopCount(1)

                animal.setImageDrawable(resource)

                resource.stop()

                gifsAnimais[arquivo] = resource

                animal.isClickable = true
                animal.isFocusable = true

                // PULSAÇÃO ANTES DO TOQUE

                if (
                    telaAtiva &&
                    arquivo !in animaisIniciados &&
                    !jogoFinalizado
                ) {

                    animarAnimal(
                        arquivo,
                        animal,
                        indice
                    )
                }

                // IDENTIFICAR O FINAL DO GIF

                resource.clearAnimationCallbacks()

                resource.registerAnimationCallback(

                    object : Animatable2Compat.AnimationCallback() {

                        override fun onAnimationEnd(
                            drawable: Drawable?
                        ) {

                            runOnUiThread {

                                if (
                                    !jogoFinalizado &&
                                    !navegando &&
                                    arquivo in animaisIniciados
                                ) {

                                    registrarAnimalConcluido(
                                        arquivo
                                    )
                                }
                            }
                        }
                    }
                )

                // =========================================
                // TOQUE NO ANIMAL
                // =========================================

                animal.setOnClickListener {

                    if (
                        jogoFinalizado ||
                        navegando ||
                        !telaAtiva ||
                        arquivo in animaisIniciados ||
                        arquivo in animaisConcluidos
                    ) {

                        return@setOnClickListener
                    }

                    val gif =
                        animal.drawable as? GifDrawable

                    if (gif != null && !gif.isRunning) {

                        animaisIniciados.add(arquivo)

                        // PARAR PULSAÇÃO

                        pararPulsacaoAnimal(
                            arquivo,
                            animal
                        )

                        // SOM DO TOQUE

                        tocarAudio(
                            R.raw.audio_toque
                        )

                        // SOM MÁGICO DO GLITTER

                        tocarSomBrilho()

                        // EXPLOSÃO DE ESTRELINHAS

                        criarGlitter(animal)

                        // EXECUTAR MOVIMENTO DO ANIMAL

                        gif.start()
                    }
                }
            }

            override fun onLoadCleared(
                placeholder: Drawable?
            ) {

                gifsAnimais.remove(arquivo)?.let {

                    it.stop()

                    it.clearAnimationCallbacks()
                }

                animacoesAnimais
                    .remove(arquivo)
                    ?.cancel()

                animal.setImageDrawable(null)

                animal.isClickable = false
            }

            override fun onLoadFailed(
                errorDrawable: Drawable?
            ) {

                super.onLoadFailed(errorDrawable)

                Toast.makeText(
                    this@MaoszinhasMagicasActivity,
                    "Não foi possível carregar $arquivo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        alvosGif.add(alvo)

        Glide.with(this)
            .asGif()
            .load(caminho)
            .into(alvo)
    }

    // =====================================================
    // GLITTER AO TOCAR NOS ANIMAIS
    // =====================================================

    private fun criarGlitter(animal: ImageView) {

        val sx = root.width.toFloat() / larguraBase
        val sy = root.height.toFloat() / alturaBase

        val escala = minOf(sx, sy)

        val centroX =
            animal.x + animal.width / 2f

        val centroY =
            animal.y + animal.height / 2f

        val cores = intArrayOf(

            Color.rgb(255, 220, 110),

            Color.rgb(255, 245, 175),

            Color.rgb(255, 255, 255),

            Color.rgb(255, 190, 220),

            Color.rgb(198, 232, 255),

            Color.rgb(224, 204, 255)
        )

        // QUANTIDADE DE ESTRELINHAS

        repeat(32) { indice ->

            val tamanho =
                Random.nextInt(13, 29) * escala

            val estrela = TextView(this).apply {

                text = when (indice % 4) {

                    0 -> "✦"
                    1 -> "✧"
                    2 -> "★"
                    else -> "✦"
                }

                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    tamanho
                )

                setTextColor(
                    cores.random()
                )

                gravity = Gravity.CENTER

                includeFontPadding = false

                setShadowLayer(
                    5f * escala,
                    0f,
                    0f,
                    Color.WHITE
                )

                isClickable = false
                isFocusable = false

                elevation = 8f * escala

                alpha = 1f

                scaleX = 0.4f
                scaleY = 0.4f
            }

            val lado =
                (tamanho * 2.4f)
                    .toInt()
                    .coerceAtLeast(1)

            val parametros = FrameLayout.LayoutParams(
                lado,
                lado
            )

            parametros.leftMargin =
                (centroX - lado / 2f).toInt()

            parametros.topMargin =
                (centroY - lado / 2f).toInt()

            root.addView(
                estrela,
                parametros
            )

            estrelasAtivas.add(estrela)

            // DIREÇÃO ALEATÓRIA DAS ESTRELAS

            val angulo =
                Random.nextDouble(
                    0.0,
                    Math.PI * 2
                )

            val distancia =
                Random.nextInt(40, 125) * escala

            val destinoX =
                cos(angulo).toFloat() * distancia

            val destinoY =
                sin(angulo).toFloat() * distancia

            estrela.animate()
                .translationX(destinoX)
                .translationY(destinoY)
                .rotation(
                    Random.nextInt(-150, 150).toFloat()
                )
                .scaleX(1.3f)
                .scaleY(1.3f)
                .alpha(0f)
                .setStartDelay(
                    Random.nextLong(0L, 130L)
                )
                .setDuration(
                    Random.nextLong(650L, 1050L)
                )
                .setInterpolator(
                    DecelerateInterpolator()
                )
                .withEndAction {

                    root.removeView(estrela)

                    estrelasAtivas.remove(estrela)
                }
                .start()
        }
    }

    // =====================================================
    // LIMPAR EFEITOS DE GLITTER
    // =====================================================

    private fun limparGlitter() {

        estrelasAtivas.toList().forEach {

            it.animate().cancel()

            root.removeView(it)
        }

        estrelasAtivas.clear()
    }

    // =====================================================
    // MÚSICA DE FUNDO
    // =====================================================

    private fun iniciarAudioFundo() {

        if (!telaAtiva || navegando) return

        try {

            if (audioFundo == null) {

                val id = resources.getIdentifier(
                    "audiodefundojogo",
                    "raw",
                    packageName
                )

                if (id == 0) return

                val player = MediaPlayer.create(
                    this,
                    id
                ) ?: return

                audioFundo = player

                // REPETIR CONTINUAMENTE

                player.isLooping = true

                // VOLUME SUAVE

                player.setVolume(
                    0.35f,
                    0.35f
                )

                player.setOnErrorListener { _, _, _ ->

                    liberarAudioFundo()

                    true
                }
            }

            audioFundo?.let {

                if (!it.isPlaying) {

                    it.start()
                }
            }

        } catch (erro: Exception) {

            liberarAudioFundo()

            Log.e(
                "MaoszinhasMagicas",
                "Erro na música de fundo",
                erro
            )
        }
    }

    // =====================================================
    // PAUSAR MÚSICA DE FUNDO
    // =====================================================

    private fun pausarAudioFundo() {

        try {

            audioFundo?.let {

                if (it.isPlaying) {

                    it.pause()
                }
            }

        } catch (_: Exception) {
        }
    }

    // =====================================================
    // LIBERAR MÚSICA DE FUNDO
    // =====================================================

    private fun liberarAudioFundo() {

        val player = audioFundo

        audioFundo = null

        player?.let {

            try {

                it.setOnErrorListener(null)

                it.release()

            } catch (_: Exception) {
            }
        }
    }

    // =====================================================
    // SOM MÁGICO DO GLITTER
    // =====================================================

    private fun tocarSomBrilho() {

        liberarAudioBrilho()

        try {

            val id = resources.getIdentifier(
                "brilho_suave",
                "raw",
                packageName
            )

            if (id == 0) return

            val player = MediaPlayer.create(
                this,
                id
            ) ?: return

            audioBrilho = player

            player.setVolume(
                0.55f,
                0.55f
            )

            player.setOnCompletionListener {

                if (audioBrilho === player) {

                    liberarAudioBrilho()
                }
            }

            player.setOnErrorListener { _, _, _ ->

                if (audioBrilho === player) {

                    liberarAudioBrilho()
                }

                true
            }

            player.start()

        } catch (_: Exception) {

            liberarAudioBrilho()
        }
    }

    private fun liberarAudioBrilho() {

        val player = audioBrilho

        audioBrilho = null

        player?.let {

            try {

                it.setOnCompletionListener(null)

                it.setOnErrorListener(null)

                it.release()

            } catch (_: Exception) {
            }
        }
    }

    // =====================================================
    // PULSAÇÃO SUAVE DOS ANIMAIS
    // =====================================================

    private fun animarAnimal(
        arquivo: String,
        animal: ImageView,
        indice: Int
    ) {

        animacoesAnimais
            .remove(arquivo)
            ?.cancel()

        animal.animate().cancel()

        animal.scaleX = 1f
        animal.scaleY = 1f

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

        val animacao =
            ObjectAnimator.ofPropertyValuesHolder(
                animal,
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

        animacoesAnimais[arquivo] = animacao

        animacao.start()
    }

    // =====================================================
    // PARAR PULSAÇÃO DO ANIMAL TOCADO
    // =====================================================

    private fun pararPulsacaoAnimal(
        arquivo: String,
        animal: ImageView
    ) {

        animacoesAnimais
            .remove(arquivo)
            ?.cancel()

        animal.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(180L)
            .start()
    }

    // =====================================================
    // PARAR TODAS AS PULSAÇÕES
    // =====================================================

    private fun pararTodasPulsacoes() {

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
    // CONTROLE DE CONCLUSÃO DOS ANIMAIS
    // =====================================================

    private fun registrarAnimalConcluido(
        arquivo: String
    ) {

        if (jogoFinalizado || navegando) return

        val novoAnimal =
            animaisConcluidos.add(arquivo)

        if (!novoAnimal) return

        imagensAnimais[arquivo]?.let {

            it.isClickable = false
            it.isFocusable = false
        }

        // VERIFICAR SE TODOS OS ANIMAIS TERMINARAM

        if (animaisConcluidos.size == totalAnimais) {

            jogoFinalizado = true

            pararTodasPulsacoes()

            handler.postDelayed({

                if (
                    !isFinishing &&
                    !isDestroyed &&
                    !navegando &&
                    telaAtiva
                ) {

                    mostrarPopupVitoria(
                        root.width.toFloat() / larguraBase,
                        root.height.toFloat() / alturaBase
                    )
                }

            }, 1200L)
        }
    }

    // =====================================================
    // POP-UP DE VITÓRIA
    // =====================================================

    private fun mostrarPopupVitoria(
        sx: Float,
        sy: Float
    ) {

        if (popupAberto || navegando) return

        popupAberto = true

        limparGlitter()

        val escala = minOf(sx, sy)

        // =================================================
        // FUNDO ESCURECIDO
        // =================================================

        val fundoEscuro = View(this).apply {

            setBackgroundColor(
                Color.argb(85, 35, 45, 40)
            )

            alpha = 0f

            isClickable = true

            elevation = 20f * escala
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
            .setDuration(350L)
            .start()

        // =================================================
        // FUNDO DO POP-UP
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

            elevation = 25f * escala

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
        // ANIMAÇÃO DE ENTRADA
        // =================================================

        popup.scaleX = 0.6f
        popup.scaleY = 0.6f
        popup.alpha = 0f

        popup.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(500L)
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

            popup.addView(
                view,
                params
            )
        }

        // =================================================
        // CRIAR TEXTOS DO POP-UP
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

            scaleType =
                ImageView.ScaleType.FIT_CENTER
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
            .setStartDelay(200L)
            .setDuration(600L)
            .setInterpolator(
                OvershootInterpolator(1.8f)
            )
            .start()

        // =================================================
        // ESTRELINHAS
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
        // MENSAGEM
        // =================================================

        val subtitulo = criarTexto(
            "Você descobriu todos os movimentos! 🌟",
            21f,
            Color.rgb(145, 123, 105)
        )

        colocar(
            subtitulo,
            35, 135, 470, 38
        )

        // =================================================
        // CRIAR BOTÕES
        // =================================================

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
                        (2 * escala)
                            .toInt()
                            .coerceAtLeast(1),

                        corBorda
                    )
                }

                elevation = 4f

                isClickable = true
                isFocusable = true

                isSoundEffectsEnabled = false
            }
        }

        // =================================================
        // BOTÃO VOLTAR
        // =================================================

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

        // =================================================
        // BOTÃO PRÓXIMO NÍVEL
        // =================================================

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

        botaoProximo.animate()
            .scaleX(1.03f)
            .scaleY(1.03f)
            .setStartDelay(750L)
            .setDuration(450L)
            .withEndAction {

                botaoProximo.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(0L)
                    .setDuration(400L)
                    .start()
            }
            .start()
    }

    // =====================================================
    // VOLTAR PARA OS CINCO JOGOS
    // =====================================================

    private fun voltarParaJogos() {

        if (navegando || isFinishing) return

        navegando = true

        handler.removeCallbacksAndMessages(null)

        pararTodasPulsacoes()

        limparGlitter()

        pararAudio()

        liberarAudioFundo()

        liberarAudioBrilho()

        gifsAnimais.values.forEach {

            it.stop()
        }

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

        voltarParaJogos()
    }

    // =====================================================
    // ABRIR PRÓXIMO NÍVEL
    // =====================================================

    private fun abrirProximoNivel() {

        if (navegando || isFinishing) return

        navegando = true

        pararTodasPulsacoes()

        limparGlitter()

        pararAudio()

        liberarAudioFundo()

        liberarAudioBrilho()

        tocarSomNavegacao()

        try {

            val intent = Intent().apply {

                setClassName(
                    this@MaoszinhasMagicasActivity,
                    "$packageName.$proximaFaseActivity"
                )
            }

            startActivity(intent)

            finish()

        } catch (erro: ActivityNotFoundException) {

            navegando = false

            iniciarAudioFundo()

            Toast.makeText(
                this,
                "Próximo nível em breve!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =====================================================
    // SOM AO TOCAR NOS ANIMAIS
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

            player.setVolume(
                0.65f,
                0.65f
            )

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
                "MaoszinhasMagicas",
                "Erro ao reproduzir áudio",
                erro
            )
        }
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

            player?.setVolume(
                0.45f,
                0.45f
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
                "MaoszinhasMagicas",
                "Erro no som de navegação",
                erro
            )
        }
    }

    // =====================================================
    // PARAR ÁUDIO DO TOQUE
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

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        adicionarView(
            imagem,
            x, y, largura, altura,
            sx, sy
        )

        return imagem
    }

    // =====================================================
    // POSICIONAR ELEMENTOS
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
    // LIBERAR GIFS
    // =====================================================

    private fun limparGifs() {

        gifsAnimais.values.forEach {

            it.stop()

            it.clearAnimationCallbacks()
        }

        alvosGif.forEach {

            Glide.with(this).clear(it)
        }

        alvosGif.clear()

        gifsAnimais.clear()
    }

    // =====================================================
    // RETOMAR ANIMAÇÕES E MÚSICA
    // =====================================================

    override fun onResume() {

        super.onResume()

        telaAtiva = true

        if (navegando) return

        iniciarAudioFundo()

        if (!jogoFinalizado) {

            imagensAnimais.entries.forEachIndexed {
                    indice, entrada ->

                val arquivo = entrada.key

                val imagem = entrada.value

                val gif = gifsAnimais[arquivo]

                if (
                    gif != null &&
                    arquivo !in animaisConcluidos
                ) {

                    if (arquivo in animaisIniciados) {

                        if (!gif.isRunning) {

                            gif.start()
                        }

                    } else {

                        animarAnimal(
                            arquivo,
                            imagem,
                            indice
                        )
                    }
                }
            }

        } else if (
            !popupAberto &&
            ::root.isInitialized
        ) {

            mostrarPopupVitoria(
                root.width.toFloat() / larguraBase,
                root.height.toFloat() / alturaBase
            )
        }
    }

    // =====================================================
    // PAUSAR ANIMAÇÕES E ÁUDIOS
    // =====================================================

    override fun onPause() {

        telaAtiva = false

        pararTodasPulsacoes()

        limparGlitter()

        pararAudio()

        pausarAudioFundo()

        liberarAudioBrilho()

        gifsAnimais.values.forEach {

            it.stop()
        }

        super.onPause()
    }

    // =====================================================
    // LIBERAR TODOS OS RECURSOS
    // =====================================================

    override fun onDestroy() {

        navegando = true

        handler.removeCallbacksAndMessages(null)

        pararTodasPulsacoes()

        limparGlitter()

        pararAudio()

        liberarAudioFundo()

        liberarAudioBrilho()

        limparGifs()

        super.onDestroy()
    }
}