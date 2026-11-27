package com.example.mobilejogos

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.TypedValue
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

@Suppress("DEPRECATION")
class RespirandoComDragaoActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var videoView: TextureView
    private lateinit var solAnimado: SolAnimadoView
    private lateinit var botaoIniciar: TextView

    private var popupAtual: FrameLayout? = null

    private val larguraBase = 874f
    private val alturaBase = 393f

    private var sx = 1f
    private var sy = 1f
    private var escala = 1f

    // VÍDEO E ÁUDIOS

    private var videoPlayer: MediaPlayer? = null
    private var audioClique: MediaPlayer? = null

    private var superficieVideo: Surface? = null

    // NARRADOR

    private var narrador: TextToSpeech? = null

    private var vozDisponivel = false
    private var narracaoPendente = false

    // VOLUME DA VOZ DA IA

    private val volumeNarracao = 0.65f

    // CONTROLE DO JOGO

    private var videoPreparado = false
    private var jogoIniciado = false
    private var jogoFinalizado = false
    private var telaAtiva = false
    private var iniciarPendente = false
    private var retomandoVideo = false
    private var instrucoesExibidas = false
    private var popupInstrucaoAberto = false
    private var saindo = false

    private var posicaoSalvaVideo = 0

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

        ocultarBarras()

        root = FrameLayout(this).apply {

            setBackgroundColor(
                Color.rgb(191, 225, 246)
            )

            clipChildren = false
            clipToPadding = false
        }

        setContentView(root)

        configurarNarrador()

        root.post {

            if (!isFinishing && !isDestroyed) {

                montarTela()
            }
        }
    }

    // =====================================================
    // TELA CHEIA
    // =====================================================

    private fun ocultarBarras() {

        window.decorView.systemUiVisibility =

            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    override fun onWindowFocusChanged(
        hasFocus: Boolean
    ) {

        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {

            ocultarBarras()
        }
    }

    // =====================================================
    // MONTAR TELA DO DRAGÃO
    // =====================================================

    private fun montarTela() {

        root.removeAllViews()

        sx = root.width / larguraBase
        sy = root.height / alturaBase

        escala = minOf(sx, sy)

        // =================================================
        // VÍDEO DO DRAGÃO
        // =================================================

        videoView = TextureView(this)

        root.addView(
            videoView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        configurarVideo()

        // =================================================
        // SOL ANIMADO
        // =================================================

        solAnimado = SolAnimadoView()

        adicionarView(
            solAnimado,
            744, 10, 120, 120
        )

        // =================================================
        // BOTÃO VOLTAR
        // =================================================

        val voltar = adicionarImagem(
            "botaoretornoazul",
            19, 25, 50, 50
        )

        voltar.contentDescription =
            "Voltar para a tela geral dos jogos"

        voltar.isClickable = true
        voltar.isFocusable = true

        voltar.isSoundEffectsEnabled = false

        voltar.setOnClickListener {

            voltarTela()
        }

        // =================================================
        // SOMBRA DO BOTÃO INICIAR
        // =================================================

        val sombraBotao = View(this).apply {

            background = criarFundo(
                Color.rgb(105, 165, 209),
                Color.rgb(105, 165, 209),
                23f,
                Color.TRANSPARENT,
                0f
            )

            tag = "sombraIniciar"
        }

        adicionarView(
            sombraBotao,
            332, 178, 210, 58
        )

        // =================================================
        // BOTÃO INICIAR PERSONALIZADO
        // =================================================

        botaoIniciar = criarTexto(
            "▶  Iniciar",
            25f,
            Color.rgb(60, 115, 155)
        )

        botaoIniciar.background = criarFundo(
            Color.rgb(230, 247, 255),
            Color.rgb(165, 218, 245),
            23f,
            Color.rgb(117, 181, 224),
            2.5f
        )

        botaoIniciar.isClickable = true
        botaoIniciar.isFocusable = true
        botaoIniciar.isSoundEffectsEnabled = false

        botaoIniciar.contentDescription =
            "Iniciar exercício de respiração"

        adicionarView(
            botaoIniciar,
            332, 172, 210, 58
        )

        botaoIniciar.setOnClickListener {

            tocarSomClique()

            pararNarracao()

            iniciarJogo()
        }

        botaoIniciar.scaleX = 0.8f
        botaoIniciar.scaleY = 0.8f

        botaoIniciar.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500L)
            .setInterpolator(
                OvershootInterpolator(1.3f)
            )
            .start()

        // =================================================
        // ABRIR POP-UP AUTOMATICAMENTE
        // =================================================

        if (!instrucoesExibidas && telaAtiva) {

            mostrarPopupInstrucoes()
        }
    }

    // =====================================================
    // CONFIGURAR NARRADOR
    // =====================================================

    private fun configurarNarrador() {

        narrador = TextToSpeech(this) { resultado ->

            root.post {

                if (
                    isFinishing ||
                    isDestroyed ||
                    saindo
                ) {

                    return@post
                }

                if (resultado == TextToSpeech.SUCCESS) {

                    val configuracao =
                        narrador?.setLanguage(
                            Locale("pt", "BR")
                        )

                    vozDisponivel =
                        configuracao != null &&
                                configuracao >=
                                TextToSpeech.LANG_AVAILABLE

                    // VELOCIDADE DA NARRAÇÃO

                    narrador?.setSpeechRate(0.90f)

                    // TOM NATURAL DA VOZ

                    narrador?.setPitch(1.0f)

                    if (
                        vozDisponivel &&
                        narracaoPendente &&
                        telaAtiva &&
                        popupInstrucaoAberto &&
                        !jogoIniciado
                    ) {

                        tocarNarracao()
                    }

                } else {

                    vozDisponivel = false
                }
            }
        }
    }

    // =====================================================
    // NARRAÇÃO EXPLICATIVA COM VOLUME REDUZIDO
    // =====================================================

    private fun tocarNarracao() {

        if (
            !telaAtiva ||
            saindo ||
            !popupInstrucaoAberto
        ) return

        if (!vozDisponivel) {

            narracaoPendente = true

            return
        }

        narracaoPendente = false

        val mensagem =
            "Olá, amiguinho! " +
                    "Vamos respirar com o dragão? " +
                    "Inspire devagar pelo nariz. " +
                    "E solte o ar lentamente pela boca. " +
                    "Acompanhe o dragão. " +
                    "Vamos começar?"

        // VOLUME REDUZIDO PARA 65%

        val parametrosAudio = Bundle().apply {

            putFloat(
                TextToSpeech.Engine.KEY_PARAM_VOLUME,
                volumeNarracao
            )
        }

        narrador?.speak(
            mensagem,
            TextToSpeech.QUEUE_FLUSH,
            parametrosAudio,
            "instrucoes_dragao"
        )
    }

    // =====================================================
    // PARAR NARRAÇÃO
    // =====================================================

    private fun pararNarracao() {

        narracaoPendente = false

        narrador?.stop()
    }

    // =====================================================
    // CONFIGURAR VÍDEO
    // =====================================================

    private fun configurarVideo() {

        videoView.surfaceTextureListener =

            object : TextureView.SurfaceTextureListener {

                override fun onSurfaceTextureAvailable(
                    texture: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {

                    prepararVideo(texture)
                }

                override fun onSurfaceTextureSizeChanged(
                    texture: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                }

                override fun onSurfaceTextureDestroyed(
                    texture: SurfaceTexture
                ): Boolean {

                    salvarPosicaoVideo()

                    liberarVideo()

                    return true
                }

                override fun onSurfaceTextureUpdated(
                    texture: SurfaceTexture
                ) {
                }
            }

        if (videoView.isAvailable) {

            videoView.surfaceTexture?.let {

                prepararVideo(it)
            }
        }
    }

    // =====================================================
    // PREPARAR VÍDEO
    // =====================================================

    private fun prepararVideo(
        texture: SurfaceTexture
    ) {

        liberarVideo()

        try {

            val surface = Surface(texture)

            superficieVideo = surface

            val player = MediaPlayer()

            videoPlayer = player

            assets.openFd("videodragao.mp4").use {

                player.setDataSource(
                    it.fileDescriptor,
                    it.startOffset,
                    it.length
                )
            }

            player.setSurface(surface)

            player.isLooping = false

            player.setVolume(
                1f,
                1f
            )

            player.setVideoScalingMode(
                MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT
            )

            // VÍDEO CARREGADO

            player.setOnPreparedListener {

                if (
                    videoPlayer !== player ||
                    saindo
                ) {

                    return@setOnPreparedListener
                }

                videoPreparado = true

                when {

                    jogoIniciado -> {

                        player.seekTo(
                            posicaoSalvaVideo
                        )

                        if (telaAtiva) {

                            player.start()

                            retomandoVideo = false

                            if (::solAnimado.isInitialized) {

                                solAnimado.invalidate()
                            }

                        } else {

                            retomandoVideo = true
                        }
                    }

                    iniciarPendente && telaAtiva -> {

                        iniciarJogo()
                    }

                    jogoFinalizado -> {

                        player.seekTo(
                            (player.duration - 1)
                                .coerceAtLeast(0)
                        )
                    }

                    else -> {

                        player.seekTo(1)
                    }
                }
            }

            // FINAL DO VÍDEO

            player.setOnCompletionListener {

                if (
                    jogoIniciado &&
                    !jogoFinalizado &&
                    !saindo
                ) {

                    finalizarJogo()
                }
            }

            // ERRO NO VÍDEO

            player.setOnErrorListener { _, _, _ ->

                videoPreparado = false
                jogoIniciado = false
                iniciarPendente = false

                liberarVideo()

                if (::botaoIniciar.isInitialized) {

                    exibirBotaoIniciar()
                }

                Toast.makeText(
                    this,
                    "Não foi possível reproduzir o vídeo.",
                    Toast.LENGTH_SHORT
                ).show()

                true
            }

            player.prepareAsync()

        } catch (_: Exception) {

            liberarVideo()

            iniciarPendente = false

            Toast.makeText(
                this,
                "Verifique o arquivo videodragao.mp4",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =====================================================
    // POP-UP DE INSTRUÇÕES
    // =====================================================

    private fun mostrarPopupInstrucoes() {

        if (
            popupAtual != null ||
            saindo ||
            isFinishing
        ) return

        instrucoesExibidas = true

        val popup = criarPopup(
            largura = 480,
            altura = 325
        )

        popupInstrucaoAberto = true

        // =================================================
        // CÍRCULO ATRÁS DO DRAGÃO
        // =================================================

        val circulo = View(this).apply {

            background = criarFundo(
                Color.rgb(223, 242, 255),
                Color.rgb(202, 229, 247),
                60f,
                Color.TRANSPARENT,
                0f
            )
        }

        colocarNoPopup(
            popup,
            circulo,
            194, 12, 92, 92
        )

        // =================================================
        // ROSTINHO DO DRAGÃO
        // =================================================

        val dragao = criarImagem(
            "rostodragao"
        )

        colocarNoPopup(
            popup,
            dragao,
            185, 6, 110, 105
        )

        dragao.scaleX = 0.4f
        dragao.scaleY = 0.4f

        dragao.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(550L)
            .setInterpolator(
                OvershootInterpolator(1.5f)
            )
            .start()

        // =================================================
        // ESTRELINHAS DECORATIVAS
        // =================================================

        colocarNoPopup(
            popup,
            criarTexto(
                "✦",
                27f,
                Color.rgb(240, 199, 112)
            ),
            135, 24, 35, 40
        )

        colocarNoPopup(
            popup,
            criarTexto(
                "✧",
                24f,
                Color.rgb(163, 189, 229)
            ),
            310, 62, 35, 35
        )

        // =================================================
        // TÍTULO DO POP-UP
        // =================================================

        val titulo = criarTexto(
            "Vamos respirar juntos?",
            28f,
            Color.rgb(87, 145, 190)
        )

        colocarNoPopup(
            popup,
            titulo,
            15, 111, 450, 40
        )

        // =================================================
        // MENSAGEM EXPLICATIVA
        // =================================================

        val mensagem = criarTexto(
            "Inspire devagar pelo nariz.\n" +
                    "Solte o ar lentamente pela boca.",
            19f,
            Color.rgb(94, 126, 150),
            false
        )

        colocarNoPopup(
            popup,
            mensagem,
            20, 156, 440, 65
        )

        val complemento = criarTexto(
            "Vamos relaxar juntos!",
            18f,
            Color.rgb(107, 157, 192)
        )

        colocarNoPopup(
            popup,
            complemento,
            20, 224, 440, 28
        )

        // =================================================
        // BOTÃO OUVIR NOVAMENTE
        // =================================================

        val botaoAudio = criarTexto(
            "🔊",
            25f,
            Color.rgb(87, 145, 190)
        )

        botaoAudio.background = criarFundo(
            Color.rgb(230, 246, 255),
            Color.rgb(195, 226, 247),
            20f,
            Color.rgb(155, 199, 229),
            2f
        )

        botaoAudio.contentDescription =
            "Ouvir instruções novamente"

        botaoAudio.isClickable = true
        botaoAudio.isFocusable = true
        botaoAudio.isSoundEffectsEnabled = false

        colocarNoPopup(
            popup,
            botaoAudio,
            30, 265, 55, 45
        )

        botaoAudio.setOnClickListener {

            botaoAudio.animate()
                .scaleX(0.90f)
                .scaleY(0.90f)
                .setDuration(90L)
                .withEndAction {

                    botaoAudio.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(130L)
                        .start()
                }
                .start()

            tocarNarracao()
        }

        // =================================================
        // BOTÃO VAMOS COMEÇAR
        // =================================================

        val botaoComecar = criarTexto(
            "Vamos começar!",
            23f,
            Color.rgb(60, 115, 155)
        )

        botaoComecar.background = criarFundo(
            Color.rgb(220, 242, 255),
            Color.rgb(165, 218, 245),
            22f,
            Color.rgb(117, 181, 224),
            2f
        )

        botaoComecar.isClickable = true
        botaoComecar.isFocusable = true
        botaoComecar.isSoundEffectsEnabled = false

        colocarNoPopup(
            popup,
            botaoComecar,
            100, 265, 350, 45
        )

        botaoComecar.setOnClickListener {

            pararNarracao()

            tocarSomClique()

            fecharPopup()

            iniciarJogo()
        }

        // NARRAÇÃO AUTOMÁTICA

        tocarNarracao()
    }

    // =====================================================
    // INICIAR EXERCÍCIO
    // =====================================================

    private fun iniciarJogo() {

        if (
            jogoIniciado ||
            saindo ||
            isFinishing
        ) return

        if (!videoPreparado || !telaAtiva) {

            iniciarPendente = true

            return
        }

        val player = videoPlayer ?: return

        iniciarPendente = false
        jogoIniciado = true
        jogoFinalizado = false

        posicaoSalvaVideo = 0

        pararNarracao()

        fecharPopup()

        ocultarBotaoIniciar()

        try {

            player.seekTo(0)

            player.start()

            solAnimado.invalidate()

        } catch (_: Exception) {

            jogoIniciado = false

            exibirBotaoIniciar()

            Toast.makeText(
                this,
                "Não foi possível iniciar o exercício.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =====================================================
    // SOL ANIMADO SINCRONIZADO COM O VÍDEO
    // =====================================================

    private inner class SolAnimadoView : View(
        this@RespirandoComDragaoActivity
    ) {

        private var movie: Movie? = null

        init {

            setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                null
            )

            isClickable = false
            isFocusable = false

            try {

                assets.open("soldragao.gif").use {

                    movie = Movie.decodeStream(it)
                }

            } catch (_: Exception) {
            }
        }

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            val gif = movie ?: return

            if (
                gif.width() <= 0 ||
                gif.height() <= 0
            ) return

            val duracaoGif = gif.duration()
                .coerceAtLeast(1)

            val player = videoPlayer

            var progresso = 0f

            if (
                videoPreparado &&
                player != null
            ) {

                try {

                    val duracaoVideo =
                        player.duration.coerceAtLeast(1)

                    progresso = (

                            player.currentPosition.toFloat() /
                                    duracaoVideo

                            ).coerceIn(0f, 1f)

                } catch (_: Exception) {
                }
            }

            if (
                !jogoIniciado &&
                !jogoFinalizado
            ) {

                progresso = 0f
            }

            if (jogoFinalizado) {

                progresso = 1f
            }

            val tempoGif = (

                    progresso * (duracaoGif - 1)

                    ).toInt()

            gif.setTime(tempoGif)

            val escalaGif = minOf(

                width.toFloat() / gif.width(),

                height.toFloat() / gif.height()
            )

            val posX = (

                    width - gif.width() * escalaGif

                    ) / 2f

            val posY = (

                    height - gif.height() * escalaGif

                    ) / 2f

            canvas.save()

            canvas.translate(
                posX,
                posY
            )

            canvas.scale(
                escalaGif,
                escalaGif
            )

            gif.draw(
                canvas,
                0f,
                0f
            )

            canvas.restore()

            if (
                jogoIniciado &&
                telaAtiva
            ) {

                postInvalidateOnAnimation()
            }
        }
    }

    // =====================================================
    // FINALIZAR EXERCÍCIO
    // =====================================================

    private fun finalizarJogo() {

        if (
            jogoFinalizado ||
            saindo
        ) return

        jogoFinalizado = true
        jogoIniciado = false

        if (::solAnimado.isInitialized) {

            solAnimado.invalidate()
        }

        if (telaAtiva) {

            mostrarPopupVitoria()
        }
    }

    // =====================================================
    // POP-UP DE PARABÉNS
    // =====================================================

    private fun mostrarPopupVitoria() {

        if (
            popupAtual != null ||
            saindo
        ) return

        val popup = criarPopup(
            largura = 500,
            altura = 280
        )

        val girafinha = criarImagem(
            "logoinferior"
        )

        colocarNoPopup(
            popup,
            girafinha,
            195, 0, 110, 95
        )

        girafinha.scaleX = 0f
        girafinha.scaleY = 0f

        girafinha.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(550L)
            .setInterpolator(
                OvershootInterpolator(1.5f)
            )
            .start()

        // ESTRELINHAS

        colocarNoPopup(
            popup,
            criarTexto(
                "✦",
                28f,
                Color.rgb(240, 199, 112)
            ),
            100, 30, 40, 40
        )

        colocarNoPopup(
            popup,
            criarTexto(
                "✦",
                28f,
                Color.rgb(240, 199, 112)
            ),
            360, 30, 40, 40
        )

        // TÍTULO

        colocarNoPopup(
            popup,
            criarTexto(
                "Parabéns!",
                34f,
                Color.rgb(108, 155, 199)
            ),
            20, 92, 460, 42
        )

        // MENSAGEM

        colocarNoPopup(
            popup,
            criarTexto(
                "Você respirou com o dragão!\n" +
                        "Muito bem, pequeno explorador!",
                20f,
                Color.rgb(94, 126, 150)
            ),
            20, 140, 460, 60
        )

        // =================================================
        // BOTÃO VOLTAR PARA TELA GERAL DOS JOGOS
        // =================================================

        val voltar = criarTexto(
            "← Voltar",
            22f,
            Color.rgb(75, 125, 165)
        )

        voltar.background = criarFundo(
            Color.rgb(222, 242, 255),
            Color.rgb(180, 220, 245),
            22f,
            Color.rgb(145, 193, 224),
            2f
        )

        voltar.isClickable = true
        voltar.isFocusable = true
        voltar.isSoundEffectsEnabled = false

        colocarNoPopup(
            popup,
            voltar,
            30, 215, 205, 48
        )

        voltar.setOnClickListener {

            voltarTela()
        }

        // =================================================
        // BOTÃO REPETIR
        // =================================================

        val repetir = criarTexto(
            "Repetir ↻",
            22f,
            Color.rgb(77, 133, 100)
        )

        repetir.background = criarFundo(
            Color.rgb(225, 249, 228),
            Color.rgb(178, 225, 185),
            22f,
            Color.rgb(145, 205, 160),
            2f
        )

        repetir.isClickable = true
        repetir.isFocusable = true
        repetir.isSoundEffectsEnabled = false

        colocarNoPopup(
            popup,
            repetir,
            265, 215, 205, 48
        )

        repetir.setOnClickListener {

            tocarSomClique()

            fecharPopup()

            repetirJogo()
        }
    }

    // =====================================================
    // REPETIR EXERCÍCIO
    // =====================================================

    private fun repetirJogo() {

        jogoIniciado = false
        jogoFinalizado = false

        posicaoSalvaVideo = 0

        fecharPopup()

        iniciarJogo()
    }

    // =====================================================
    // CRIAR FUNDO DOS POP-UPS
    // =====================================================

    private fun criarPopup(
        largura: Int,
        altura: Int
    ): FrameLayout {

        fecharPopup()

        // FUNDO ESCURECIDO

        val camada = FrameLayout(this).apply {

            setBackgroundColor(
                Color.argb(110, 35, 55, 75)
            )

            isClickable = true
            isFocusable = true

            elevation = 30f * escala
        }

        popupAtual = camada

        root.addView(
            camada,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // CAIXA AZUL PASTEL

        val popup = FrameLayout(this).apply {

            background = criarFundo(
                Color.rgb(255, 255, 255),
                Color.rgb(228, 244, 255),
                28f,
                Color.rgb(158, 204, 235),
                3f
            )

            elevation = 12f * escala

            isClickable = true
        }

        camada.addView(
            popup,
            FrameLayout.LayoutParams(

                (largura * sx)
                    .toInt()
                    .coerceAtLeast(1),

                (altura * sy)
                    .toInt()
                    .coerceAtLeast(1),

                Gravity.CENTER
            )
        )

        // ANIMAÇÃO DO POP-UP

        camada.alpha = 0f

        camada.animate()
            .alpha(1f)
            .setDuration(200L)
            .start()

        popup.scaleX = 0.7f
        popup.scaleY = 0.7f
        popup.alpha = 0f

        popup.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(450L)
            .setInterpolator(
                OvershootInterpolator(1.1f)
            )
            .start()

        return popup
    }

    // =====================================================
    // FECHAR POP-UP
    // =====================================================

    private fun fecharPopup() {

        if (popupInstrucaoAberto) {

            pararNarracao()
        }

        popupInstrucaoAberto = false

        popupAtual?.let {

            it.animate().cancel()

            root.removeView(it)
        }

        popupAtual = null
    }

    // =====================================================
    // OCULTAR BOTÃO INICIAR
    // =====================================================

    private fun ocultarBotaoIniciar() {

        if (::botaoIniciar.isInitialized) {

            botaoIniciar.visibility = View.GONE
        }

        root.findViewWithTag<View>(
            "sombraIniciar"
        )?.visibility = View.GONE
    }

    // =====================================================
    // EXIBIR BOTÃO INICIAR
    // =====================================================

    private fun exibirBotaoIniciar() {

        if (::botaoIniciar.isInitialized) {

            botaoIniciar.visibility = View.VISIBLE
        }

        root.findViewWithTag<View>(
            "sombraIniciar"
        )?.visibility = View.VISIBLE
    }

    // =====================================================
    // SOM DE CLIQUE
    // =====================================================

    private fun tocarSomClique() {

        liberarSomClique()

        try {

            val id = resources.getIdentifier(
                "clique_suave",
                "raw",
                packageName
            )

            if (id == 0) return

            val player = MediaPlayer.create(
                applicationContext,
                id
            ) ?: return

            audioClique = player

            player.setVolume(
                0.45f,
                0.45f
            )

            player.setOnCompletionListener {

                if (audioClique === player) {

                    liberarSomClique()
                }
            }

            player.setOnErrorListener { _, _, _ ->

                if (audioClique === player) {

                    liberarSomClique()
                }

                true
            }

            player.start()

        } catch (_: Exception) {

            liberarSomClique()
        }
    }

    private fun liberarSomClique() {

        val player = audioClique

        audioClique = null

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
    // SOM DO BOTÃO DE RETORNO
    // =====================================================

    private fun tocarSomRetorno() {

        var player: MediaPlayer? = null

        try {

            val id = resources.getIdentifier(
                "clique_suave",
                "raw",
                packageName
            )

            if (id == 0) return

            player = MediaPlayer.create(
                applicationContext,
                id
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

        } catch (_: Exception) {

            player?.release()
        }
    }

    // =====================================================
    // CRIAR TEXTOS
    // =====================================================

    private fun criarTexto(
        conteudo: String,
        tamanho: Float,
        cor: Int,
        negrito: Boolean = true
    ): TextView {

        return TextView(this).apply {

            text = conteudo

            setTextColor(cor)

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                tamanho * escala
            )

            gravity = Gravity.CENTER

            includeFontPadding = false

            typeface = Typeface.create(

                "sans-serif-rounded",

                if (negrito) {

                    Typeface.BOLD

                } else {

                    Typeface.NORMAL
                }
            )
        }
    }

    // =====================================================
    // CRIAR FUNDOS ARREDONDADOS
    // =====================================================

    private fun criarFundo(
        corInicio: Int,
        corFim: Int,
        raio: Float,
        corBorda: Int,
        espessura: Float
    ): GradientDrawable {

        return GradientDrawable(

            GradientDrawable.Orientation.TOP_BOTTOM,

            intArrayOf(
                corInicio,
                corFim
            )

        ).apply {

            cornerRadius =
                raio * escala

            if (espessura > 0f) {

                setStroke(

                    (espessura * escala)
                        .toInt()
                        .coerceAtLeast(1),

                    corBorda
                )
            }
        }
    }

    // =====================================================
    // CRIAR IMAGEM
    // =====================================================

    private fun criarImagem(
        nome: String
    ): ImageView {

        return ImageView(this).apply {

            val id = resources.getIdentifier(
                nome,
                "drawable",
                packageName
            )

            if (id != 0) {

                setImageResource(id)
            }

            scaleType =
                ImageView.ScaleType.FIT_CENTER
        }
    }

    // =====================================================
    // ADICIONAR IMAGEM À TELA
    // =====================================================

    private fun adicionarImagem(
        nome: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int
    ): ImageView {

        val imagem = criarImagem(nome)

        adicionarView(
            imagem,
            x, y, largura, altura
        )

        return imagem
    }

    // =====================================================
    // TAMANHOS E POSIÇÕES
    // =====================================================

    private fun parametros(
        x: Int,
        y: Int,
        largura: Int,
        altura: Int
    ): FrameLayout.LayoutParams {

        return FrameLayout.LayoutParams(

            (largura * sx)
                .toInt()
                .coerceAtLeast(1),

            (altura * sy)
                .toInt()
                .coerceAtLeast(1)

        ).apply {

            leftMargin =
                (x * sx).toInt()

            topMargin =
                (y * sy).toInt()
        }
    }

    private fun adicionarView(
        view: View,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int
    ) {

        root.addView(
            view,
            parametros(
                x, y, largura, altura
            )
        )
    }

    private fun colocarNoPopup(
        popup: FrameLayout,
        view: View,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int
    ) {

        popup.addView(
            view,
            parametros(
                x, y, largura, altura
            )
        )
    }

    // =====================================================
    // SALVAR POSIÇÃO DO VÍDEO
    // =====================================================

    private fun salvarPosicaoVideo() {

        if (videoPreparado) {

            try {

                posicaoSalvaVideo =
                    videoPlayer?.currentPosition ?: 0

            } catch (_: Exception) {
            }
        }
    }

    // =====================================================
    // VOLTAR PARA TELA INICIAL GERAL DOS JOGOS
    // =====================================================

    private fun voltarTela() {

        if (
            saindo ||
            isFinishing
        ) return

        saindo = true

        pararNarracao()

        liberarVideo()

        liberarSomClique()

        tocarSomRetorno()

        val intent = Intent(
            this,
            TelaInicialJogosGeral::class.java
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

    override fun onBackPressed() {

        voltarTela()
    }

    // =====================================================
    // PAUSAR AO SAIR DA TELA
    // =====================================================

    override fun onPause() {

        telaAtiva = false

        pararNarracao()

        retomandoVideo = false

        salvarPosicaoVideo()

        try {

            if (
                jogoIniciado &&
                videoPreparado &&
                videoPlayer?.isPlaying == true
            ) {

                retomandoVideo = true

                videoPlayer?.pause()
            }

        } catch (_: Exception) {
        }

        super.onPause()
    }

    // =====================================================
    // RETOMAR EXERCÍCIO
    // =====================================================

    override fun onResume() {

        super.onResume()

        telaAtiva = true

        ocultarBarras()

        if (saindo) return

        if (
            ::botaoIniciar.isInitialized &&
            !instrucoesExibidas
        ) {

            mostrarPopupInstrucoes()

        } else if (popupInstrucaoAberto) {

            tocarNarracao()
        }

        if (
            iniciarPendente &&
            videoPreparado
        ) {

            iniciarJogo()
        }

        if (
            retomandoVideo &&
            jogoIniciado &&
            videoPreparado
        ) {

            try {

                videoPlayer?.start()

                solAnimado.invalidate()

            } catch (_: Exception) {
            }

            retomandoVideo = false
        }

        if (
            jogoFinalizado &&
            popupAtual == null
        ) {

            mostrarPopupVitoria()
        }
    }

    // =====================================================
    // LIBERAR VÍDEO
    // =====================================================

    private fun liberarVideo() {

        videoPreparado = false

        val player = videoPlayer

        videoPlayer = null

        player?.let {

            try {

                it.setOnPreparedListener(null)

                it.setOnCompletionListener(null)

                it.setOnErrorListener(null)

                it.release()

            } catch (_: Exception) {
            }
        }

        superficieVideo?.release()

        superficieVideo = null
    }

    // =====================================================
    // LIBERAR TODOS OS RECURSOS
    // =====================================================

    override fun onDestroy() {

        saindo = true

        pararNarracao()

        narrador?.shutdown()

        narrador = null

        vozDisponivel = false

        liberarVideo()

        liberarSomClique()

        super.onDestroy()
    }
}