package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
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

class MaoszinhasMagicas2Activity : Activity() {

    private lateinit var root: FrameLayout

    private val larguraBase = 842f
    private val alturaBase = 364f

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

    private val totalAnimais = 4

    private var jogoFinalizado = false
    private var navegando = false
    private var telaAtiva = false
    private var popupAberto = false

    // ÁUDIOS

    private var audioFundo: MediaPlayer? = null

    private var soundPool: SoundPool? = null

    private var somToque = 0
    private var somBrilho = 0
    private var somClique = 0

    private val sonsCarregados = mutableSetOf<Int>()

    // CAMADA DE GLITTER

    private lateinit var camadaGlitter: GlitterView

    private var consumindoToque = false

    // PRÓXIMO NÍVEL

    private val proximaFaseActivity =
        "MaoszinhasMagicas3Activity"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(
            R.layout.activity_maoszinhas_magicas2
        )

        root = findViewById(
            R.id.telaMaoszinhasMagicas2
        )

        // CARREGAR SONS ANTES DOS CLIQUES

        prepararSons()

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
        // PLANO DE FUNDO
        // =================================================

        val fundo = ImageView(this).apply {

            setImageResource(
                R.drawable.planomaoszinhasmagicas2
            )

            scaleType = ImageView.ScaleType.FIT_XY

            isClickable = false
            isFocusable = false
        }

        root.addView(
            fundo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // BOTÃO VOLTAR
        // =================================================

        val voltar = adicionarImagem(
            "botaoretornoamarelo",
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
        // TÍTULO
        // =================================================

        val titulo = adicionarImagem(
            "titulodescubracomotoque",
            211, 0, 420, 65,
            sx, sy
        )

        // =================================================
        // BALÃO DE INSTRUÇÃO
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
            291, 65, 260, 35,
            sx, sy
        )

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
        // POLVO
        // =================================================

        adicionarAnimal(
            arquivo = "polvo.gif",
            x = 8,
            y = 70,
            largura = 250,
            altura = 190,
            sx = sx,
            sy = sy,
            indice = 0
        )

        // =================================================
        // PEIXE
        // =================================================

        adicionarAnimal(
            arquivo = "peixe.gif",
            x = 300,
            y = 80,
            largura = 250,
            altura = 190,
            sx = sx,
            sy = sy,
            indice = 1
        )

        // =================================================
        // TUBARÃO
        // =================================================

        adicionarAnimal(
            arquivo = "tubarao.gif",
            x = 350,
            y = 170,
            largura = 280,
            altura = 230,
            sx = sx,
            sy = sy,
            indice = 2
        )

        // =================================================
        // CAVALO-MARINHO
        // =================================================

        adicionarAnimal(
            arquivo = "cavalomarinho.gif",
            x = 630,
            y = 50,
            largura = 180,
            altura = 200,
            sx = sx,
            sy = sy,
            indice = 3
        )

        // =================================================
        // CAMADA ÚNICA DE GLITTER
        // =================================================

        camadaGlitter = GlitterView().apply {

            isClickable = false

            isFocusable = false

            elevation = 8f * escala

            importantForAccessibility =
                View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        root.addView(
            camadaGlitter,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        titulo.bringToFront()

        instrucao.bringToFront()

        voltar.bringToFront()

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

            scaleType = ImageView.ScaleType.FIT_CENTER

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

                resource.stop()

                resource.setVisible(true, false)

                resource.setLoopCount(1)

                animal.setImageDrawable(resource)

                resource.stop()

                gifsAnimais[arquivo] = resource

                animal.isClickable = true

                animal.isFocusable = true

                // PULSAÇÃO

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

                // FINAL DO GIF

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

                // CLIQUE POR ACESSIBILIDADE

                animal.setOnClickListener {

                    iniciarAnimal(
                        arquivo,
                        animal.x + animal.width / 2f,
                        animal.y + animal.height / 2f
                    )
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
                    this@MaoszinhasMagicas2Activity,
                    "Não foi possível carregar $arquivo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        alvosGif.add(alvo)

        Glide.with(this)
            .asGif()
            .load(caminho)
            .override(
                (largura * sx).toInt().coerceAtLeast(1),
                (altura * sy).toInt().coerceAtLeast(1)
            )
            .into(alvo)
    }

    // =====================================================
    // TOQUE IMEDIATO NOS ANIMAIS
    // =====================================================

    override fun dispatchTouchEvent(
        evento: MotionEvent
    ): Boolean {

        if (evento.actionMasked == MotionEvent.ACTION_DOWN) {

            consumindoToque = false

            if (
                ::root.isInitialized &&
                telaAtiva &&
                !jogoFinalizado &&
                !navegando &&
                !popupAberto
            ) {

                val localizacao = IntArray(2)

                root.getLocationOnScreen(localizacao)

                val toqueX =
                    evento.rawX - localizacao[0]

                val toqueY =
                    evento.rawY - localizacao[1]

                val sx =
                    root.width.toFloat() / larguraBase

                val sy =
                    root.height.toFloat() / alturaBase

                // PRESERVAR O BOTÃO VOLTAR

                val tocouVoltar =
                    toqueX in (19f * sx)..(59f * sx) &&
                            toqueY in (25f * sy)..(65f * sy)

                if (!tocouVoltar) {

                    val candidatos =
                        imagensAnimais.entries
                            .toList()
                            .asReversed()
                            .filter { entrada ->

                                pontoDentroDaImagem(
                                    entrada.value,
                                    toqueX,
                                    toqueY
                                )
                            }

                    val escolhido =
                        candidatos.firstOrNull { entrada ->

                            entrada.key !in animaisIniciados &&
                                    entrada.key !in animaisConcluidos &&
                                    tocouParteVisivel(
                                        entrada.key,
                                        entrada.value,
                                        toqueX,
                                        toqueY
                                    )
                        }

                    if (escolhido != null) {

                        consumindoToque = true

                        iniciarAnimal(
                            escolhido.key,
                            toqueX,
                            toqueY
                        )

                        return true
                    }

                    // IMPEDIR QUE A ÁREA TRANSPARENTE
                    // DE UM GIF ATIVE OUTRO ANIMAL

                    if (candidatos.isNotEmpty()) {

                        consumindoToque = true

                        return true
                    }
                }
            }
        }

        if (consumindoToque) {

            if (
                evento.actionMasked == MotionEvent.ACTION_UP ||
                evento.actionMasked == MotionEvent.ACTION_CANCEL
            ) {

                consumindoToque = false
            }

            return true
        }

        return super.dispatchTouchEvent(evento)
    }

    // =====================================================
    // CONVERTER POSIÇÃO DO TOQUE
    // =====================================================

    private fun coordenadasLocais(
        imagem: ImageView,
        x: Float,
        y: Float
    ): FloatArray {

        val ponto = floatArrayOf(
            x - imagem.left,
            y - imagem.top
        )

        val inversa = Matrix()

        if (imagem.matrix.invert(inversa)) {

            inversa.mapPoints(ponto)
        }

        return ponto
    }

    private fun pontoDentroDaImagem(
        imagem: ImageView,
        x: Float,
        y: Float
    ): Boolean {

        val ponto =
            coordenadasLocais(imagem, x, y)

        return ponto[0] >= 0f &&
                ponto[0] < imagem.width &&
                ponto[1] >= 0f &&
                ponto[1] < imagem.height
    }

    // =====================================================
    // DETECTAR SOMENTE A PARTE VISÍVEL DO ANIMAL
    // =====================================================

    private fun tocouParteVisivel(
        arquivo: String,
        imagem: ImageView,
        x: Float,
        y: Float
    ): Boolean {

        val gif = gifsAnimais[arquivo] ?: return false

        val ponto =
            coordenadasLocais(imagem, x, y)

        ponto[0] -= imagem.paddingLeft
        ponto[1] -= imagem.paddingTop

        val inversa = Matrix()

        if (!imagem.imageMatrix.invert(inversa)) {

            return false
        }

        inversa.mapPoints(ponto)

        val bitmap = gif.firstFrame

        val limites = gif.bounds

        if (
            limites.width() <= 0 ||
            limites.height() <= 0 ||
            bitmap.isRecycled
        ) return false

        val pixelX = (
                (ponto[0] - limites.left) /
                        limites.width() * bitmap.width
                ).toInt()

        val pixelY = (
                (ponto[1] - limites.top) /
                        limites.height() * bitmap.height
                ).toInt()

        if (
            ponto[0] < limites.left ||
            ponto[1] < limites.top ||
            pixelX !in 0 until bitmap.width ||
            pixelY !in 0 until bitmap.height
        ) return false

        return try {

            // PEQUENA TOLERÂNCIA PARA FACILITAR O TOQUE

            val margem = 5

            var encontrou = false

            for (dx in -margem..margem step 5) {

                for (dy in -margem..margem step 5) {

                    val px =
                        (pixelX + dx)
                            .coerceIn(0, bitmap.width - 1)

                    val py =
                        (pixelY + dy)
                            .coerceIn(0, bitmap.height - 1)

                    if (
                        Color.alpha(
                            bitmap.getPixel(px, py)
                        ) > 70
                    ) {

                        encontrou = true
                    }
                }
            }

            encontrou

        } catch (_: Exception) {

            true
        }
    }

    // =====================================================
    // EXECUTAR ANIMAÇÃO IMEDIATAMENTE
    // =====================================================

    private fun iniciarAnimal(
        arquivo: String,
        toqueX: Float,
        toqueY: Float
    ) {

        if (
            jogoFinalizado ||
            navegando ||
            !telaAtiva ||
            arquivo in animaisIniciados ||
            arquivo in animaisConcluidos
        ) return

        val animal =
            imagensAnimais[arquivo] ?: return

        val gif =
            gifsAnimais[arquivo] ?: return

        if (gif.isRunning) return

        animaisIniciados.add(arquivo)

        // PARAR A PULSAÇÃO

        pararPulsacaoAnimal(
            arquivo,
            animal
        )

        // INICIAR GIF PRIMEIRO

        gif.start()

        // GLITTER NO LOCAL EXATO DO TOQUE

        if (::camadaGlitter.isInitialized) {

            camadaGlitter.criarBrilho(
                toqueX,
                toqueY
            )
        }

        // SONS JÁ CARREGADOS

        reproduzirSom(somToque, 0.65f)

        reproduzirSom(somBrilho, 0.50f)

        // EVITAR PROBLEMAS COM GIF DE APENAS UM FRAME

        if (gif.frameCount <= 1) {

            handler.postDelayed({

                registrarAnimalConcluido(arquivo)

            }, 400L)
        }
    }

    // =====================================================
    // PREPARAR SONS COM SOUNDPOOL
    // =====================================================

    private fun prepararSons() {

        val atributos = AudioAttributes.Builder()

            .setUsage(
                AudioAttributes.USAGE_GAME
            )

            .setContentType(
                AudioAttributes.CONTENT_TYPE_SONIFICATION
            )

            .build()

        soundPool = SoundPool.Builder()

            .setMaxStreams(6)

            .setAudioAttributes(atributos)

            .build()

        soundPool?.setOnLoadCompleteListener {
                _, sampleId, status ->

            if (status == 0) {

                sonsCarregados.add(sampleId)
            }
        }

        somToque = carregarSom("audio_toque")

        somBrilho = carregarSom("brilho_suave")

        somClique = carregarSom("clique_pop")
    }

    private fun carregarSom(nome: String): Int {

        val id = resources.getIdentifier(
            nome,
            "raw",
            packageName
        )

        if (id == 0) return 0

        return try {

            soundPool?.load(
                this,
                id,
                1
            ) ?: 0

        } catch (_: Exception) {

            0
        }
    }

    private fun reproduzirSom(
        som: Int,
        volume: Float
    ) {

        if (
            som == 0 ||
            som !in sonsCarregados
        ) return

        soundPool?.play(
            som,
            volume,
            volume,
            1,
            0,
            1f
        )
    }

    // =====================================================
    // NOVO GLITTER OTIMIZADO
    // =====================================================

    private data class Particula(

        val x: Float,
        val y: Float,

        val velocidadeX: Float,
        val velocidadeY: Float,

        val tamanho: Float,
        val cor: Int,
        val rotacao: Float,

        val inicio: Long,
        val duracao: Long
    )

    private inner class GlitterView : View(
        this@MaoszinhasMagicas2Activity
    ) {

        private val particulas =
            mutableListOf<Particula>()

        private val pincel = Paint(
            Paint.ANTI_ALIAS_FLAG
        )

        private val caminho = Path()

        // CORES DO GLITTER

        private val cores = intArrayOf(

            Color.WHITE,

            Color.rgb(255, 228, 145),

            Color.rgb(255, 246, 205),

            Color.rgb(195, 235, 255)
        )

        fun criarBrilho(
            x: Float,
            y: Float
        ) {

            val escala = minOf(
                root.width / larguraBase,
                root.height / alturaBase
            )

            val agora =
                SystemClock.uptimeMillis()

            // 24 PARTÍCULAS SUAVES E LEVES

            repeat(24) { indice ->

                val angulo =
                    Math.PI * 2 * indice / 24.0 +
                            Random.nextDouble(-0.15, 0.15)

                val distancia =
                    Random.nextInt(35, 95) * escala

                particulas.add(

                    Particula(

                        x = x,

                        y = y,

                        velocidadeX =
                            cos(angulo).toFloat() * distancia,

                        velocidadeY =
                            sin(angulo).toFloat() * distancia,

                        tamanho =
                            Random.nextInt(3, 9) * escala,

                        cor = cores.random(),

                        rotacao =
                            Random.nextFloat() * 360f,

                        inicio = agora,

                        duracao =
                            Random.nextLong(450L, 750L)
                    )
                )
            }

            // DESENHAR SEM ESPERAR OUTRA ANIMAÇÃO

            invalidate()
        }

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            val agora =
                SystemClock.uptimeMillis()

            val iterator =
                particulas.iterator()

            while (iterator.hasNext()) {

                val p = iterator.next()

                val progresso = (
                        (agora - p.inicio).toFloat() /
                                p.duracao
                        ).coerceIn(0f, 1f)

                if (progresso >= 1f) {

                    iterator.remove()

                    continue
                }

                val movimento =
                    1f - (1f - progresso) *
                            (1f - progresso)

                val posX =
                    p.x + p.velocidadeX * movimento

                val posY =
                    p.y + p.velocidadeY * movimento

                val tamanho =
                    p.tamanho *
                            (0.65f + movimento * 0.35f)

                val transparencia =
                    if (progresso < 0.25f) {

                        255

                    } else {

                        (
                                255 *
                                        (1f - progresso) / 0.75f
                                ).toInt().coerceIn(0, 255)
                    }

                canvas.save()

                canvas.translate(
                    posX,
                    posY
                )

                canvas.rotate(
                    p.rotacao + progresso * 60f
                )

                // HALO SUAVE AO REDOR DA ESTRELA

                pincel.color = Color.WHITE

                pincel.alpha =
                    (transparencia * 0.20f).toInt()

                canvas.drawCircle(
                    0f,
                    0f,
                    tamanho * 1.6f,
                    pincel
                )

                // ESTRELA DE QUATRO PONTAS

                pincel.color = p.cor

                pincel.alpha = transparencia

                caminho.reset()

                caminho.moveTo(
                    0f,
                    -tamanho
                )

                caminho.quadTo(
                    tamanho * 0.23f,
                    -tamanho * 0.23f,
                    tamanho,
                    0f
                )

                caminho.quadTo(
                    tamanho * 0.23f,
                    tamanho * 0.23f,
                    0f,
                    tamanho
                )

                caminho.quadTo(
                    -tamanho * 0.23f,
                    tamanho * 0.23f,
                    -tamanho,
                    0f
                )

                caminho.quadTo(
                    -tamanho * 0.23f,
                    -tamanho * 0.23f,
                    0f,
                    -tamanho
                )

                caminho.close()

                canvas.drawPath(
                    caminho,
                    pincel
                )

                canvas.restore()
            }

            if (particulas.isNotEmpty()) {

                postInvalidateOnAnimation()
            }
        }

        fun limpar() {

            particulas.clear()

            invalidate()
        }
    }

    private fun limparGlitter() {

        if (::camadaGlitter.isInitialized) {

            camadaGlitter.limpar()
        }
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

                player.isLooping = true

                player.setVolume(
                    0.35f,
                    0.35f
                )

                player.setOnErrorListener {
                        _, _, _ ->

                    liberarAudioFundo()

                    true
                }
            }

            audioFundo?.let {

                if (!it.isPlaying) {

                    it.start()
                }
            }

        } catch (_: Exception) {

            liberarAudioFundo()
        }
    }

    private fun pausarAudioFundo() {

        try {

            audioFundo?.let {

                if (it.isPlaying) {

                    it.pause()
                }
            }

        } catch (_: Exception) {

            liberarAudioFundo()
        }
    }

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
    // PULSAÇÃO DOS ANIMAIS
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
    // PARAR PULSAÇÃO SEM ATRASO
    // =====================================================

    private fun pararPulsacaoAnimal(
        arquivo: String,
        animal: ImageView
    ) {

        animacoesAnimais
            .remove(arquivo)
            ?.cancel()

        animal.animate().cancel()

        animal.scaleX = 1f

        animal.scaleY = 1f
    }

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
    // CONTROLE DE CONCLUSÃO
    // =====================================================

    private fun registrarAnimalConcluido(
        arquivo: String
    ) {

        if (
            jogoFinalizado ||
            navegando ||
            isFinishing ||
            isDestroyed
        ) return

        val novoAnimal =
            animaisConcluidos.add(arquivo)

        if (!novoAnimal) return

        imagensAnimais[arquivo]?.let {

            it.isClickable = false

            it.isFocusable = false
        }

        if (animaisConcluidos.size == totalAnimais) {

            jogoFinalizado = true

            pararTodasPulsacoes()

            // POP-UP MAIS RÁPIDO

            handler.postDelayed({

                if (
                    !isFinishing &&
                    !isDestroyed &&
                    !navegando &&
                    telaAtiva
                ) {

                    mostrarPopupVitoria(
                        root.width / larguraBase,
                        root.height / alturaBase
                    )
                }

            }, 500L)
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

        // FUNDO ESCURECIDO

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
            .setDuration(250L)
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

            elevation = 25f * escala

            isClickable = true
        }

        root.addView(
            popup,
            FrameLayout.LayoutParams(
                (540 * sx).toInt(),
                (260 * sy).toInt(),
                Gravity.CENTER
            )
        )

        popup.scaleX = 0.6f
        popup.scaleY = 0.6f
        popup.alpha = 0f

        popup.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(400L)
            .setInterpolator(
                OvershootInterpolator(1.1f)
            )
            .start()

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

                leftMargin =
                    (x * sx).toInt()

                topMargin =
                    (y * sy).toInt()
            }

            popup.addView(
                view,
                params
            )
        }

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
            .setStartDelay(100L)
            .setDuration(450L)
            .setInterpolator(
                OvershootInterpolator(1.8f)
            )
            .start()

        // ESTRELINHAS DECORATIVAS

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

        // TÍTULO

        colocar(
            criarTexto(
                "Parabéns, explorador!",
                32f,
                Color.rgb(165, 138, 199)
            ),
            35, 85, 470, 48
        )

        // MENSAGEM

        colocar(
            criarTexto(
                "Você descobriu todos os animais do mar! 🌊",
                20f,
                Color.rgb(145, 123, 105)
            ),
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

                isSoundEffectsEnabled = false
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

            player?.setVolume(
                0.45f,
                0.45f
            )

            player?.setOnCompletionListener {

                it.release()
            }

            player?.setOnErrorListener {
                    mp, _, _ ->

                mp.release()

                true
            }

            player?.start()

        } catch (_: Exception) {

            player?.release()
        }
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

        liberarAudioFundo()

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
    // ABRIR PRÓXIMO NÍVEL
    // =====================================================

    private fun abrirProximoNivel() {

        if (navegando || isFinishing) return

        navegando = true

        pararTodasPulsacoes()

        limparGlitter()

        liberarAudioFundo()

        tocarSomNavegacao()

        try {

            val intent = Intent().apply {

                setClassName(
                    this@MaoszinhasMagicas2Activity,
                    "$packageName.$proximaFaseActivity"
                )
            }

            startActivity(intent)

            finish()

        } catch (_: ActivityNotFoundException) {

            navegando = false

            iniciarAudioFundo()

            Toast.makeText(
                this,
                "Próximo nível em breve!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

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

        ).apply {

            leftMargin = (x * sx).toInt()

            topMargin = (y * sy).toInt()
        }

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

        alvosGif.toList().forEach {

            Glide.with(applicationContext).clear(it)
        }

        alvosGif.clear()

        gifsAnimais.clear()
    }

    // =====================================================
    // RETOMAR JOGO
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
                root.width / larguraBase,
                root.height / alturaBase
            )
        }
    }

    // =====================================================
    // PAUSAR JOGO
    // =====================================================

    override fun onPause() {

        telaAtiva = false

        consumindoToque = false

        pararTodasPulsacoes()

        limparGlitter()

        pausarAudioFundo()

        soundPool?.autoPause()

        gifsAnimais.values.forEach {

            it.stop()
        }

        super.onPause()
    }

    // =====================================================
    // LIBERAR RECURSOS
    // =====================================================

    override fun onDestroy() {

        navegando = true

        handler.removeCallbacksAndMessages(null)

        pararTodasPulsacoes()

        limparGlitter()

        liberarAudioFundo()

        soundPool?.release()

        soundPool = null

        sonsCarregados.clear()

        limparGifs()

        super.onDestroy()
    }
}