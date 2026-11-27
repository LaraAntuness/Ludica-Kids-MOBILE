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
import android.widget.Toast
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MainActivity : Activity() {

    private lateinit var root: FrameLayout

    private val handler = Handler(Looper.getMainLooper())

    private val proximaFaseActivity = "OlhinhosCuriosos2Activity"

    // POSIÇÕES DOS ANIMAIS

    private val animais = listOf(
        Animal("coelho", "coelho", 670, 205, 95, 115),
        Animal("capivara", "capivara", 290, 240, 115, 80),
        Animal("onca", "onça", 175, 145, 110, 85),
        Animal("passaro", "pássaro", 590, 70, 60, 55),
        Animal("burrinho", "burrinho", 15, 170, 155, 155)
    )

    // CORES DO GLITTER

    private val coresGlitter = listOf(
        Color.rgb(255, 211, 85),
        Color.rgb(255, 235, 165),
        Color.rgb(130, 187, 237),
        Color.rgb(183, 222, 255),
        Color.WHITE,
        Color.rgb(164, 211, 144)
    )

    private val estrelasAtivas = mutableListOf<View>()

    private val selecionados = mutableSetOf<String>()

    private var indiceAtual = 0
    private var finalizado = false

    private var pedidoView: TextView? = null
    private var mensagemView: TextView? = null

    private var audioToquePlayer: MediaPlayer? = null
    private var audioFundoPlayer: MediaPlayer? = null
    private var audioAcertoPlayer: MediaPlayer? = null

    private val animacoesAnimais =
        mutableMapOf<String, ObjectAnimator>()

    private val imagensAnimais =
        mutableMapOf<String, ImageView>()

    data class Animal(
        val imagem: String,
        val nome: String,
        val x: Int,
        val y: Int,
        val largura: Int,
        val altura: Int
    )

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
        limparGlitter()

        imagensAnimais.clear()

        root.removeAllViews()

        val sx = root.width / 874f
        val sy = root.height / 402f

        val escala = minOf(sx, sy)

        // PLANO DE FUNDO

        val fundo = ImageView(this).apply {

            val imagemId = resources.getIdentifier(
                "fundoatualizadotoquenosanimais",
                "drawable",
                packageName
            )

            if (imagemId != 0) {
                setImageResource(imagemId)
            }

            scaleType = ImageView.ScaleType.FIT_XY
        }

        root.addView(
            fundo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // BOTÃO VOLTAR

        val voltar = adicionarImagem(
            "botao_retorno",
            19, 25, 40, 40,
            sx, sy
        )

        voltar.isClickable = true
        voltar.isFocusable = true

        voltar.setOnClickListener {
            voltarParaJogos()
        }

        // TÍTULO

        adicionarImagem(
            "titulo_toquenosanimais",
            227, 1, 420, 73,
            sx, sy
        )

        // =================================================
        // BALÃO DE INSTRUÇÕES
        // =================================================

        pedidoView = TextView(this).apply {

            text = ""

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                19f * escala
            )

            // COR LARANJA

            setTextColor(
                Color.rgb(220, 140, 45)
            )

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER

            includeFontPadding = false

            isSingleLine = true

            setPadding(
                (8 * sx).toInt(),
                0,
                (8 * sx).toInt(),
                0
            )

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(255, 253, 245),
                    Color.rgb(255, 249, 228)
                )
            ).apply {

                cornerRadius = 18f * escala

                setStroke(
                    (2.5f * escala)
                        .toInt()
                        .coerceAtLeast(1),

                    Color.rgb(245, 222, 160)
                )
            }

            elevation = 4f * escala
        }

        adicionarView(
            pedidoView!!,
            307, 58, 260, 35,
            sx, sy
        )

        // =================================================
        // MENSAGEM DE ERRO
        // =================================================

        mensagemView = TextView(this).apply {

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                13f * escala
            )

            setTextColor(
                Color.rgb(170, 85, 85)
            )

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER

            includeFontPadding = false

            background = GradientDrawable().apply {

                setColor(
                    Color.rgb(255, 235, 232)
                )

                cornerRadius = 15f * escala

                setStroke(
                    (2 * escala)
                        .toInt()
                        .coerceAtLeast(1),

                    Color.rgb(240, 182, 180)
                )
            }

            elevation = 3f * escala

            visibility = View.GONE
        }

        adicionarView(
            mensagemView!!,
            302, 104, 270, 26,
            sx, sy
        )

        // =================================================
        // ANIMAIS
        // =================================================

        animais.forEachIndexed { index, animal ->

            // CÍRCULO AMARELO

            val halo = View(this).apply {

                background = GradientDrawable().apply {

                    shape = GradientDrawable.OVAL

                    setColor(
                        Color.argb(100, 255, 221, 40)
                    )

                    setStroke(
                        (4 * escala)
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

            adicionarView(
                halo,
                animal.x,
                animal.y,
                animal.largura,
                animal.altura,
                sx,
                sy
            )

            // IMAGEM DO ANIMAL

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

                isClickable = true
                isFocusable = true
            }

            val parametros = FrameLayout.LayoutParams(
                (animal.largura * sx)
                    .toInt()
                    .coerceAtLeast(1),

                (animal.altura * sy)
                    .toInt()
                    .coerceAtLeast(1)
            ).apply {

                leftMargin = (animal.x * sx).toInt()

                topMargin = (animal.y * sy).toInt()
            }

            root.addView(
                imagem,
                parametros
            )

            imagensAnimais[animal.imagem] = imagem

            // PULSAÇÃO DOS ANIMAIS

            if (animal.imagem !in selecionados && !finalizado) {

                animarAnimal(
                    animal.imagem,
                    imagem,
                    index
                )
            }

            // CLIQUE NOS ANIMAIS

            imagem.setOnClickListener {

                if (!finalizado) {

                    tocarSomAudioToque()

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

        // =================================================
        // ARBUSTO NA FRENTE DO BURRINHO
        // =================================================

        adicionarImagem(
            "arbustro",
            -60, 130, 450, 460,
            sx, sy
        ).apply {

            isClickable = false

            isFocusable = false

            importantForAccessibility =
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        // =================================================
        // ARBUSTO NA FRENTE DO COELHO
        // =================================================

        adicionarImagem(
            "arbustro",
            550, 140, 450, 460,
            sx, sy
        ).apply {

            isClickable = false

            isFocusable = false

            importantForAccessibility =
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        atualizarPedido()
    }

    // =====================================================
    // ÁUDIO DE FUNDO
    // =====================================================

    private fun iniciarAudioFundo() {

        try {

            if (audioFundoPlayer == null) {

                val audioId = resources.getIdentifier(
                    "audiodefundojogo",
                    "raw",
                    packageName
                )

                if (audioId == 0) {

                    Log.e(
                        "OlhinhosCuriosos",
                        "Áudio de fundo não encontrado"
                    )

                    return
                }

                audioFundoPlayer = MediaPlayer.create(
                    this,
                    audioId
                )

                audioFundoPlayer?.isLooping = true

                audioFundoPlayer?.setVolume(
                    0.35f,
                    0.35f
                )
            }

            if (audioFundoPlayer?.isPlaying == false) {

                audioFundoPlayer?.start()
            }

        } catch (erro: Exception) {

            liberarAudioFundo()

            Log.e(
                "OlhinhosCuriosos",
                "Erro ao reproduzir áudio de fundo",
                erro
            )
        }
    }

    // =====================================================
    // PAUSAR ÁUDIO DE FUNDO
    // =====================================================

    private fun pausarAudioFundo() {

        try {

            if (audioFundoPlayer?.isPlaying == true) {

                audioFundoPlayer?.pause()
            }

        } catch (erro: Exception) {

            liberarAudioFundo()
        }
    }

    // =====================================================
    // LIBERAR ÁUDIO DE FUNDO
    // =====================================================

    private fun liberarAudioFundo() {

        audioFundoPlayer?.release()

        audioFundoPlayer = null
    }

    // =====================================================
    // SOM DE ACERTO COM GLITTER
    // =====================================================

    private fun tocarSomAcerto() {

        liberarAudioAcerto()

        try {

            val audioId = resources.getIdentifier(
                "brilho_suave",
                "raw",
                packageName
            )

            if (audioId == 0) {

                Log.e(
                    "OlhinhosCuriosos",
                    "Áudio brilho_suave não encontrado"
                )

                return
            }

            val player = MediaPlayer.create(
                this,
                audioId
            ) ?: return

            audioAcertoPlayer = player

            player.setVolume(
                0.55f,
                0.55f
            )

            player.setOnCompletionListener {

                if (audioAcertoPlayer === it) {

                    audioAcertoPlayer = null

                    it.release()
                }
            }

            player.setOnErrorListener { mp, _, _ ->

                if (audioAcertoPlayer === mp) {

                    audioAcertoPlayer = null

                    mp.release()
                }

                true
            }

            player.start()

        } catch (erro: Exception) {

            liberarAudioAcerto()

            Log.e(
                "OlhinhosCuriosos",
                "Erro no som de acerto",
                erro
            )
        }
    }

    private fun liberarAudioAcerto() {

        val player = audioAcertoPlayer

        audioAcertoPlayer = null

        player?.let {

            it.setOnCompletionListener(null)

            it.setOnErrorListener(null)

            it.release()
        }
    }

    // =====================================================
    // GLITTER ANIMADO AO REDOR DO ANIMAL
    // =====================================================

    private fun animarGlitter(
        imagem: ImageView,
        sx: Float,
        sy: Float
    ) {

        val escala = minOf(sx, sy)

        val centroX =
            imagem.x + imagem.width / 2f

        val centroY =
            imagem.y + imagem.height / 2f

        // QUANTIDADE DE ESTRELINHAS

        repeat(32) { indice ->

            val estrela = TextView(this).apply {

                text =
                    if (indice % 3 == 0) "✦" else "★"

                setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    Random.nextInt(12, 23).toFloat() * escala
                )

                setTextColor(
                    coresGlitter.random()
                )

                gravity = Gravity.CENTER

                includeFontPadding = false

                elevation = 6f

                isClickable = false

                isFocusable = false

                importantForAccessibility =
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }

            val tamanho =
                (34 * escala).toInt().coerceAtLeast(1)

            val parametros = FrameLayout.LayoutParams(
                tamanho,
                tamanho
            ).apply {

                leftMargin =
                    (centroX - tamanho / 2f).toInt()

                topMargin =
                    (centroY - tamanho / 2f).toInt()
            }

            root.addView(
                estrela,
                parametros
            )

            estrelasAtivas.add(estrela)

            val angulo =
                2.0 * Math.PI * indice / 32.0 +
                        Random.nextDouble(-0.2, 0.2)

            val distancia =
                Random.nextInt(45, 120) * escala

            estrela.alpha = 1f

            estrela.scaleX = 0.2f
            estrela.scaleY = 0.2f

            estrela.animate()
                .translationX(
                    (cos(angulo) * distancia).toFloat()
                )
                .translationY(
                    (sin(angulo) * distancia).toFloat()
                )
                .rotation(
                    Random.nextFloat() * 360f
                )
                .scaleX(1.2f)
                .scaleY(1.2f)
                .alpha(0f)
                .setDuration(
                    Random.nextLong(700L, 1200L)
                )
                .withEndAction {

                    root.removeView(estrela)

                    estrelasAtivas.remove(estrela)
                }
                .start()
        }
    }

    // =====================================================
    // REMOVER GLITTER
    // =====================================================

    private fun limparGlitter() {

        estrelasAtivas.toList().forEach { estrela ->

            estrela.animate().cancel()

            root.removeView(estrela)
        }

        estrelasAtivas.clear()
    }

    // =====================================================
    // PULSAÇÃO SUAVE DOS ANIMAIS
    // =====================================================

    private fun animarAnimal(
        nomeAnimal: String,
        imagem: ImageView,
        indice: Int
    ) {

        animacoesAnimais.remove(nomeAnimal)?.cancel()

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
    // PARAR PULSAÇÃO DO ANIMAL ENCONTRADO
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
                "OlhinhosCuriosos",
                "Erro ao reproduzir áudio de toque",
                erro
            )
        }
    }

    // =====================================================
    // PARAR SOM DE TOQUE
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
            animal.imagem in selecionados
        ) {
            return
        }

        val esperado = animais[indiceAtual]

        // =================================================
        // RESPOSTA ERRADA
        // =================================================

        if (animal.imagem != esperado.imagem) {

            mensagemView?.apply {

                text = "Animal errado, tente novamente!"

                visibility = View.VISIBLE

                alpha = 0f

                animate()
                    .alpha(1f)
                    .setDuration(200L)
                    .start()
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
        // RESPOSTA CORRETA
        // =================================================

        selecionados.add(animal.imagem)

        // SOM DE ACERTO

        tocarSomAcerto()

        // GLITTER NO ANIMAL CORRETO

        animarGlitter(
            imagem,
            sx,
            sy
        )

        // PARAR PULSAÇÃO

        pararAnimacaoAnimal(
            animal.imagem,
            imagem
        )

        // MOSTRAR CÍRCULO AMARELO

        halo.visibility = View.VISIBLE

        halo.alpha = 0f

        halo.scaleX = 0.85f
        halo.scaleY = 0.85f

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
    // ATUALIZAR FRASE DO BALÃO
    // =====================================================

    private fun atualizarPedido() {

        if (indiceAtual >= animais.size) {
            return
        }

        val animalAtual = animais[indiceAtual]

        val frase = when (animalAtual.imagem) {

            "capivara" -> "Toque na capivara!"

            "onca" -> "Toque na onça!"

            else -> "Toque no ${animalAtual.nome}!"
        }

        pedidoView?.apply {

            animate().cancel()

            text = "✦  $frase  ✦"

            visibility = View.VISIBLE

            alpha = 0f

            scaleX = 0.90f
            scaleY = 0.90f

            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(350L)
                .setInterpolator(
                    OvershootInterpolator(1.2f)
                )
                .start()
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
        HistoricoAtividades.registrar(this, "Olhinhos Curiosos", "Nível 1")

        pararTodasAnimacoes()

        handler.removeCallbacksAndMessages(null)

        pedidoView?.visibility = View.GONE

        mensagemView?.visibility = View.GONE

        // AGUARDAR O GLITTER DO ÚLTIMO ANIMAL

        handler.postDelayed({

            if (!isFinishing && !isDestroyed) {

                mostrarPopupVitoria(sx, sy)
            }

        }, 1200L)
    }

    // =====================================================
    // POP-UP DE VITÓRIA
    // =====================================================

    private fun mostrarPopupVitoria(
        sx: Float,
        sy: Float
    ) {

        val escala = minOf(sx, sy)

        // FUNDO ESCURECIDO

        val fundoEscuro = View(this).apply {

            setBackgroundColor(
                Color.argb(85, 35, 45, 40)
            )

            alpha = 0f

            isClickable = true

            elevation = 8f
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

        // ANIMAÇÃO DE ENTRADA

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

        // POSICIONAR ELEMENTOS

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

        // ESTRELA ESQUERDA

        val estrelaEsquerda = criarTexto(
            "✦",
            26f,
            Color.rgb(245, 210, 135)
        )

        colocar(
            estrelaEsquerda,
            75, 40, 35, 35
        )

        // ESTRELA DIREITA

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
            "Parabéns, explorador!",
            32f,
            Color.rgb(165, 138, 199)
        )

        colocar(
            titulo,
            35, 85, 470, 48
        )

        // SUBTÍTULO

        val subtitulo = criarTexto(
            "Você encontrou todos os animais! 🐾",
            21f,
            Color.rgb(145, 123, 105)
        )

        colocar(
            subtitulo,
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
                        (2 * escala)
                            .toInt()
                            .coerceAtLeast(1),

                        corBorda
                    )
                }

                elevation = 4f

                isClickable = true

                isFocusable = true
            }
        }

        // BOTÃO VOLTAR

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

        // BOTÃO PRÓXIMO NÍVEL

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

        // ANIMAÇÃO DO BOTÃO PRÓXIMO

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

        if (isFinishing) return

        handler.removeCallbacksAndMessages(null)

        pararTodasAnimacoes()

        limparGlitter()

        pararAudio()

        liberarAudioAcerto()

        liberarAudioFundo()

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
    // PRÓXIMO NÍVEL
    // =====================================================

    private fun abrirProximoNivel() {

        if (isFinishing) return

        try {

            val intent = Intent().apply {

                setClassName(
                    this@MainActivity,
                    "$packageName.$proximaFaseActivity"
                )
            }

            startActivity(intent)

            pararTodasAnimacoes()

            limparGlitter()

            pararAudio()

            liberarAudioAcerto()

            liberarAudioFundo()

            finish()

        } catch (erro: ActivityNotFoundException) {

            Toast.makeText(
                this,
                "Próximo nível em breve!",
                Toast.LENGTH_SHORT
            ).show()
        }
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
    // RETOMAR ANIMAÇÕES E MÚSICA
    // =====================================================

    override fun onResume() {

        super.onResume()
        HistoricoAtividades.iniciar(this)

        iniciarAudioFundo()

        if (!finalizado) {

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
    // PAUSAR ANIMAÇÕES E ÁUDIOS
    // =====================================================

    override fun onPause() {

        pararTodasAnimacoes()

        limparGlitter()

        pararAudio()

        liberarAudioAcerto()

        pausarAudioFundo()

        super.onPause()
        HistoricoAtividades.pausar(this)
    }

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        pararTodasAnimacoes()

        limparGlitter()

        pararAudio()

        liberarAudioAcerto()

        liberarAudioFundo()

        super.onDestroy()
    }
}