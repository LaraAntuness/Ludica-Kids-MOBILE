package com.example.mobilejogos

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Movie
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
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

@Suppress("DEPRECATION")
class RespirandoComDragaoActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var videoDragao: TextureView
    private lateinit var videoSol: SolAnimado
    private lateinit var botaoIniciar: TextView

    private var playerDragao: MediaPlayer? = null
    private var superficie: Surface? = null
    private var audioClique: MediaPlayer? = null

    private val larguraBase = 874f
    private val alturaBase = 402f

    private var sx = 1f
    private var sy = 1f
    private var escala = 1f

    private var preparado = false
    private var iniciou = false
    private var finalizado = false
    private var telaAtiva = false
    private var navegando = false

    private var posicaoVideo = 0

    private var popupVitoria: FrameLayout? = null

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

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        // TELA CRIADA DIRETAMENTE PELO KOTLIN
        // NÃO DEPENDE DO XML

        root = FrameLayout(this)

        root.setBackgroundColor(Color.BLACK)

        setContentView(root)

        root.post {

            if (!isFinishing && !isDestroyed) {

                montarTela()
            }
        }
    }

    // =====================================================
    // MONTAR TELA
    // =====================================================

    private fun montarTela() {

        sx = root.width / larguraBase
        sy = root.height / alturaBase

        escala = minOf(sx, sy)

        // =================================================
        // VÍDEO PRINCIPAL DO DRAGÃO
        // =================================================

        videoDragao = TextureView(this).apply {

            isClickable = false
            isFocusable = false
        }

        root.addView(
            videoDragao,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // SOL COM FUNDO TRANSPARENTE
        // =================================================

        videoSol = SolAnimado().apply {

            setBackgroundColor(Color.TRANSPARENT)

            isClickable = false
            isFocusable = false

            elevation = 3f
        }

        adicionarView(
            videoSol,
            744, 10, 120, 120
        )

        // =================================================
        // BOTÃO RETORNO AZUL
        // =================================================

        val voltar = ImageView(this).apply {

            val id = resources.getIdentifier(
                "botaoretornoazul",
                "drawable",
                packageName
            )

            if (id != 0) {

                setImageResource(id)
            }

            scaleType = ImageView.ScaleType.FIT_CENTER

            contentDescription = "Voltar"

            isClickable = true
            isFocusable = true

            elevation = 5f
        }

        adicionarView(
            voltar,
            19, 25, 50, 50
        )

        voltar.setOnClickListener {

            voltarParaMenu()
        }

        // =================================================
        // BOTÃO INICIAR
        // =================================================

        botaoIniciar = TextView(this).apply {

            text = "▶ Iniciar"

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                28f * escala
            )

            setTextColor(
                Color.rgb(55, 105, 140)
            )

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER

            includeFontPadding = false

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(221, 244, 255),
                    Color.rgb(159, 216, 244)
                )
            ).apply {

                cornerRadius = 30f * escala

                setStroke(
                    (3 * escala)
                        .toInt()
                        .coerceAtLeast(1),

                    Color.rgb(110, 182, 225)
                )
            }

            elevation = 6f

            isClickable = true
            isFocusable = true

            isEnabled = false

            alpha = 0.6f
        }

        adicionarView(
            botaoIniciar,
            317, 167, 240, 68
        )

        botaoIniciar.setOnClickListener {

            iniciarExercicio()
        }

        configurarVideoDragao()

        atualizarBotaoIniciar()
    }

    // =====================================================
    // GIF DO SOL COM TRANSPARÊNCIA
    // =====================================================

    private inner class SolAnimado : View(
        this@RespirandoComDragaoActivity
    ) {

        private var filme: Movie? = null

        val carregado: Boolean
            get() = filme != null

        init {

            setLayerType(
                View.LAYER_TYPE_SOFTWARE,
                null
            )

            setBackgroundColor(Color.TRANSPARENT)

            try {

                assets.open("soldragao.gif").use {

                    filme = Movie.decodeStream(it)
                }

                if (filme == null) {

                    Toast.makeText(
                        this@RespirandoComDragaoActivity,
                        "Não foi possível abrir soldragao.gif",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (erro: Exception) {

                Log.e(
                    "RespirandoComDragao",
                    "Erro ao carregar soldragao.gif",
                    erro
                )

                Toast.makeText(
                    this@RespirandoComDragaoActivity,
                    "GIF do sol não encontrado",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            val gif = filme ?: return

            val duracao =
                gif.duration().coerceAtLeast(1)

            // TEMPO SINCRONIZADO COM O DRAGÃO

            val tempo = obterPosicaoVideo()
                .coerceIn(0, duracao - 1)

            gif.setTime(tempo)

            val larguraGif =
                gif.width().toFloat()

            val alturaGif =
                gif.height().toFloat()

            if (
                larguraGif <= 0 ||
                alturaGif <= 0
            ) {
                return
            }

            val escalaGif = minOf(
                width / larguraGif,
                height / alturaGif
            )

            val deslocamentoX =
                (width - larguraGif * escalaGif) / 2f

            val deslocamentoY =
                (height - alturaGif * escalaGif) / 2f

            canvas.save()

            canvas.translate(
                deslocamentoX,
                deslocamentoY
            )

            canvas.scale(
                escalaGif,
                escalaGif
            )

            // DESENHAR GIF TRANSPARENTE

            gif.draw(
                canvas,
                0f,
                0f
            )

            canvas.restore()

            // ATUALIZAR DURANTE A REPRODUÇÃO

            if (
                iniciou &&
                !finalizado &&
                telaAtiva &&
                !navegando
            ) {

                postInvalidateDelayed(50L)
            }
        }
    }

    // =====================================================
    // OBTER TEMPO ATUAL DO VÍDEO
    // =====================================================

    private fun obterPosicaoVideo(): Int {

        return try {

            if (preparado) {

                playerDragao?.currentPosition
                    ?: posicaoVideo

            } else {

                posicaoVideo
            }

        } catch (_: Exception) {

            posicaoVideo
        }
    }

    // =====================================================
    // CONFIGURAR VÍDEO DO DRAGÃO
    // =====================================================

    private fun configurarVideoDragao() {

        videoDragao.surfaceTextureListener =
            object : TextureView.SurfaceTextureListener {

                override fun onSurfaceTextureAvailable(
                    surfaceTexture: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {

                    if (
                        navegando ||
                        isDestroyed
                    ) return

                    superficie =
                        Surface(surfaceTexture)

                    prepararVideoDragao()
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {

                    ajustarVideoDragao()
                }

                override fun onSurfaceTextureDestroyed(
                    surface: SurfaceTexture
                ): Boolean {

                    pausarVideo()

                    liberarVideo()

                    return true
                }

                override fun onSurfaceTextureUpdated(
                    surface: SurfaceTexture
                ) {
                }
            }

        // CASO A SUPERFÍCIE JÁ ESTEJA DISPONÍVEL

        if (
            videoDragao.isAvailable &&
            playerDragao == null
        ) {

            videoDragao.surfaceTexture?.let {

                superficie = Surface(it)

                prepararVideoDragao()
            }
        }
    }

    // =====================================================
    // CARREGAR VÍDEO MP4 DA PASTA ASSETS
    // =====================================================

    private fun prepararVideoDragao() {

        try {

            val player = MediaPlayer()

            playerDragao = player

            preparado = false

            assets.openFd("videodragao.mp4").use {

                player.setDataSource(
                    it.fileDescriptor,
                    it.startOffset,
                    it.length
                )
            }

            player.setSurface(superficie)

            player.isLooping = false

            player.setVolume(1f, 1f)

            player.setOnVideoSizeChangedListener {
                    _, _, _ ->

                ajustarVideoDragao()
            }

            player.setOnPreparedListener {

                if (playerDragao !== player) {

                    return@setOnPreparedListener
                }

                ajustarVideoDragao()

                // MOSTRAR PRIMEIRO QUADRO

                player.seekTo(posicaoVideo)
            }

            player.setOnSeekCompleteListener {

                if (playerDragao !== player) {

                    return@setOnSeekCompleteListener
                }

                preparado = true

                atualizarBotaoIniciar()

                videoSol.invalidate()

                if (
                    iniciou &&
                    telaAtiva &&
                    !finalizado &&
                    !navegando
                ) {

                    reproduzirVideo()
                }
            }

            player.setOnCompletionListener {

                if (playerDragao === it) {

                    finalizarExercicio()
                }
            }

            player.setOnErrorListener { _, what, extra ->

                Log.e(
                    "RespirandoComDragao",
                    "Erro do vídeo: $what / $extra"
                )

                liberarVideo()

                atualizarBotaoIniciar()

                Toast.makeText(
                    this,
                    "Erro ao reproduzir videodragao.mp4",
                    Toast.LENGTH_LONG
                ).show()

                true
            }

            player.prepareAsync()

        } catch (erro: Exception) {

            liberarVideo()

            Log.e(
                "RespirandoComDragao",
                "Erro ao carregar vídeo",
                erro
            )

            Toast.makeText(
                this,
                "Não foi possível carregar videodragao.mp4",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // =====================================================
    // VÍDEO OCUPANDO TODA A TELA
    // =====================================================

    private fun ajustarVideoDragao() {

        val player = playerDragao ?: return

        val larguraVideo =
            player.videoWidth.toFloat()

        val alturaVideo =
            player.videoHeight.toFloat()

        val larguraTela =
            videoDragao.width.toFloat()

        val alturaTela =
            videoDragao.height.toFloat()

        if (
            larguraVideo <= 0 ||
            alturaVideo <= 0 ||
            larguraTela <= 0 ||
            alturaTela <= 0
        ) {
            return
        }

        val escalaVideo = maxOf(
            larguraTela / larguraVideo,
            alturaTela / alturaVideo
        )

        val larguraFinal =
            larguraVideo * escalaVideo

        val alturaFinal =
            alturaVideo * escalaVideo

        val matriz = Matrix()

        matriz.setScale(
            larguraFinal / larguraTela,
            alturaFinal / alturaTela,
            larguraTela / 2f,
            alturaTela / 2f
        )

        videoDragao.setTransform(matriz)
    }

    // =====================================================
    // ATIVAR BOTÃO INICIAR
    // =====================================================

    private fun atualizarBotaoIniciar() {

        if (
            !::botaoIniciar.isInitialized ||
            !::videoSol.isInitialized
        ) return

        val pronto =
            preparado &&
                    videoSol.carregado &&
                    !iniciou &&
                    !finalizado &&
                    !navegando

        botaoIniciar.isEnabled = pronto

        botaoIniciar.alpha =
            if (pronto) 1f else 0.6f
    }

    // =====================================================
    // INICIAR EXERCÍCIO
    // =====================================================

    private fun iniciarExercicio() {

        if (
            iniciou ||
            finalizado ||
            navegando ||
            !preparado ||
            !videoSol.carregado
        ) {
            return
        }

        iniciou = true

        tocarSomClique()

        botaoIniciar.isEnabled = false

        botaoIniciar.visibility = View.GONE

        reproduzirVideo()
    }

    // =====================================================
    // REPRODUZIR DRAGÃO E SOL JUNTOS
    // =====================================================

    private fun reproduzirVideo() {

        if (
            !preparado ||
            !iniciou ||
            !telaAtiva ||
            finalizado ||
            navegando
        ) return

        try {

            playerDragao?.let {

                if (!it.isPlaying) {

                    it.start()
                }
            }

            videoSol.invalidate()

        } catch (erro: Exception) {

            Log.e(
                "RespirandoComDragao",
                "Erro ao iniciar exercício",
                erro
            )
        }
    }

    // =====================================================
    // PAUSAR VÍDEO E SOL
    // =====================================================

    private fun pausarVideo() {

        try {

            if (preparado) {

                playerDragao?.let {

                    if (it.isPlaying) {

                        it.pause()
                    }

                    posicaoVideo =
                        it.currentPosition
                }
            }

        } catch (_: Exception) {
        }
    }

    // =====================================================
    // FINALIZAR EXERCÍCIO
    // =====================================================

    private fun finalizarExercicio() {

        if (finalizado || navegando) return

        finalizado = true

        pausarVideo()

        videoSol.invalidate()

        if (telaAtiva) {

            mostrarPopupVitoria()
        }
    }

    // =====================================================
    // REPETIR EXERCÍCIO
    // =====================================================

    private fun repetirExercicio() {

        popupVitoria?.let {

            root.removeView(it)
        }

        popupVitoria = null

        iniciou = false

        finalizado = false

        preparado = false

        posicaoVideo = 0

        botaoIniciar.visibility = View.VISIBLE

        botaoIniciar.isEnabled = false

        botaoIniciar.alpha = 0.6f

        try {

            playerDragao?.seekTo(0)

            videoSol.invalidate()

        } catch (erro: Exception) {

            Log.e(
                "RespirandoComDragao",
                "Erro ao reiniciar exercício",
                erro
            )
        }
    }

    // =====================================================
    // POP-UP FINAL DE VITÓRIA
    // =====================================================

    private fun mostrarPopupVitoria() {

        if (
            popupVitoria != null ||
            navegando
        ) return

        val camada = FrameLayout(this).apply {

            setBackgroundColor(
                Color.argb(90, 30, 40, 50)
            )

            elevation = 15f

            isClickable = true
        }

        popupVitoria = camada

        root.addView(
            camada,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // CAIXA DO POP-UP

        val popup = FrameLayout(this).apply {

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(255, 253, 245),
                    Color.rgb(235, 246, 255)
                )
            ).apply {

                cornerRadius = 28f * escala

                setStroke(
                    (3 * escala)
                        .toInt()
                        .coerceAtLeast(1),

                    Color.rgb(160, 215, 240)
                )
            }

            elevation = 12f

            isClickable = true
        }

        camada.addView(
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

            popup.addView(
                view,
                criarParametros(
                    x, y, largura, altura
                )
            )
        }

        // =================================================
        // GIRAFINHA
        // =================================================

        val girafinha = ImageView(this).apply {

            val id = resources.getIdentifier(
                "logoinferior",
                "drawable",
                packageName
            )

            if (id != 0) {

                setImageResource(id)
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
        // ESTRELAS DECORATIVAS
        // =================================================

        colocar(
            criarTexto(
                "✦",
                26f,
                Color.rgb(245, 210, 135)
            ),
            75, 40, 35, 35
        )

        colocar(
            criarTexto(
                "✦",
                26f,
                Color.rgb(245, 210, 135)
            ),
            430, 40, 35, 35
        )

        // =================================================
        // TÍTULO
        // =================================================

        colocar(
            criarTexto(
                "Parabéns!",
                32f,
                Color.rgb(85, 155, 205)
            ),
            35, 85, 470, 48
        )

        // =================================================
        // MENSAGEM FINAL
        // =================================================

        colocar(
            criarTexto(
                "Você respirou com o dragão!\nMuito bem!",
                21f,
                Color.rgb(95, 115, 130)
            ),
            35, 130, 470, 55
        )

        // =================================================
        // BOTÃO VOLTAR
        // =================================================

        val botaoVoltar = criarBotaoPopup(
            "← Voltar",
            Color.rgb(190, 232, 248),
            Color.rgb(155, 215, 240),
            Color.rgb(135, 195, 225)
        )

        colocar(
            botaoVoltar,
            55, 195, 195, 52
        )

        botaoVoltar.setOnClickListener {

            voltarParaMenu()
        }

        // =================================================
        // BOTÃO REPETIR
        // =================================================

        val botaoRepetir = criarBotaoPopup(
            "Repetir ↻",
            Color.rgb(205, 242, 210),
            Color.rgb(175, 225, 185),
            Color.rgb(145, 205, 160)
        )

        colocar(
            botaoRepetir,
            275, 195, 210, 52
        )

        botaoRepetir.setOnClickListener {

            tocarSomClique()

            repetirExercicio()
        }
    }

    // =====================================================
    // CRIAR TEXTOS
    // =====================================================

    private fun criarTexto(
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

    // =====================================================
    // CRIAR BOTÕES DO POP-UP
    // =====================================================

    private fun criarBotaoPopup(
        texto: String,
        corInicio: Int,
        corFim: Int,
        corBorda: Int
    ): TextView {

        return criarTexto(
            texto,
            23f,
            Color.rgb(75, 95, 95)
        ).apply {

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

            isClickable = true

            isFocusable = true

            elevation = 4f
        }
    }

    // =====================================================
    // SOM DE CLIQUE
    // =====================================================

    private fun tocarSomClique() {

        liberarAudioClique()

        try {

            val id = resources.getIdentifier(
                "clique_suave",
                "raw",
                packageName
            )

            if (id == 0) return

            val player = MediaPlayer.create(
                this,
                id
            ) ?: return

            audioClique = player

            player.setVolume(
                0.45f,
                0.45f
            )

            player.setOnCompletionListener {

                if (audioClique === it) {

                    liberarAudioClique()
                }
            }

            player.setOnErrorListener { mp, _, _ ->

                if (audioClique === mp) {

                    liberarAudioClique()
                }

                true
            }

            player.start()

        } catch (_: Exception) {

            liberarAudioClique()
        }
    }

    // =====================================================
    // LIBERAR SOM DE CLIQUE
    // =====================================================

    private fun liberarAudioClique() {

        val player = audioClique

        audioClique = null

        player?.let {

            it.setOnCompletionListener(null)

            it.setOnErrorListener(null)

            it.release()
        }
    }

    // =====================================================
    // POSIÇÕES E TAMANHOS
    // =====================================================

    private fun criarParametros(
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
            criarParametros(
                x,
                y,
                largura,
                altura
            )
        )
    }

    // =====================================================
    // LIBERAR VÍDEO
    // =====================================================

    private fun liberarVideo() {

        preparado = false

        val player = playerDragao

        playerDragao = null

        player?.let {

            it.setOnPreparedListener(null)

            it.setOnCompletionListener(null)

            it.setOnSeekCompleteListener(null)

            it.setOnVideoSizeChangedListener(null)

            it.setOnErrorListener(null)

            it.release()
        }

        superficie?.release()

        superficie = null
    }

    // =====================================================
    // VOLTAR PARA O MENU
    // =====================================================

    private fun voltarParaMenu() {

        if (navegando || isFinishing) return

        navegando = true

        pausarVideo()

        liberarVideo()

        liberarAudioClique()

        finish()
    }

    override fun onBackPressed() {

        voltarParaMenu()
    }

    // =====================================================
    // RETOMAR EXERCÍCIO
    // =====================================================

    override fun onResume() {

        super.onResume()

        telaAtiva = true

        if (finalizado && !navegando) {

            mostrarPopupVitoria()

        } else {

            reproduzirVideo()
        }
    }

    // =====================================================
    // PAUSAR EXERCÍCIO
    // =====================================================

    override fun onPause() {

        telaAtiva = false

        pausarVideo()

        liberarAudioClique()

        super.onPause()
    }

    // =====================================================
    // ENCERRAR ACTIVITY
    // =====================================================

    override fun onDestroy() {

        navegando = true

        liberarVideo()

        liberarAudioClique()

        super.onDestroy()
    }
}