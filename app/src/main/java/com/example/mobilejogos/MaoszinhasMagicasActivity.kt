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

    private val totalAnimais = 4

    private var jogoFinalizado = false
    private var navegando = false
    private var telaAtiva = false
    private var popupAberto = false

    private var audioAtual: MediaPlayer? = null

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
            montarJogo()
        }
    }

    // =====================================================
    // MONTAR JOGO
    // =====================================================

    private fun montarJogo() {

        pararTodasPulsacoes()

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

        // =================================================
        // PLANO DE FUNDO
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

        voltar.setOnClickListener {

            voltarParaJogos()
        }

        // =================================================
        // TÍTULO
        // =================================================

        adicionarImagem(
            "titulodescubracomotoque",
            205, 2, 490, 85,
            sx, sy
        )

        // =================================================
        // INSTRUÇÃO
        // =================================================

        val instrucao = TextView(this).apply {

            text = "Toque nos animais"

            textSize = 22f

            setTextColor(
                Color.rgb(115, 95, 75)
            )

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER
        }

        adicionarView(
            instrucao,
            311, 75, 300, 40,
            sx, sy
        )

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

        voltar.bringToFront()
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

                if (navegando || isFinishing || isDestroyed) {
                    return
                }

                // GIF COMEÇA PARADO

                resource.stop()

                resource.setVisible(true, false)

                // EXECUTAR APENAS UMA VEZ

                resource.setLoopCount(1)

                animal.setImageDrawable(resource)

                resource.stop()

                gifsAnimais[arquivo] = resource

                animal.isClickable = true
                animal.isFocusable = true

                // =========================================
                // PULSAÇÃO ANTES DO TOQUE
                // =========================================

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

                // =========================================
                // IDENTIFICAR O FINAL DO GIF
                // =========================================

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
                        arquivo in animaisIniciados ||
                        arquivo in animaisConcluidos
                    ) {

                        return@setOnClickListener
                    }

                    val gif =
                        animal.drawable as? GifDrawable

                    if (gif != null && !gif.isRunning) {

                        animaisIniciados.add(arquivo)

                        // PARAR A PULSAÇÃO

                        pararPulsacaoAnimal(
                            arquivo,
                            animal
                        )

                        // REPRODUZIR SOM DO TOQUE

                        tocarAudio(
                            R.raw.audio_toque
                        )

                        // EXECUTAR O MOVIMENTO DO GIF

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

        if (jogoFinalizado || navegando) {

            return
        }

        val novoAnimal =
            animaisConcluidos.add(arquivo)

        if (!novoAnimal) {

            return
        }

        imagensAnimais[arquivo]?.let {

            it.isClickable = false
            it.isFocusable = false
        }

        // VERIFICAR SE OS QUATRO ANIMAIS TERMINARAM

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
        // RETÂNGULO COM CORES PASTÉIS
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
        // ANIMAÇÃO DE ENTRADA
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
        // POSICIONAR ELEMENTOS DO POP-UP
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
        // ESTRELAS
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

        // ANIMAÇÃO SUAVE DO BOTÃO

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
    }

    // =====================================================
    // VOLTAR PARA OS CINCO JOGOS
    // =====================================================

    private fun voltarParaJogos() {

        if (navegando || isFinishing) return

        navegando = true

        handler.removeCallbacksAndMessages(null)

        pararTodasPulsacoes()

        pararAudio()

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

        pararAudio()

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
    // RETOMAR ANIMAÇÕES
    // =====================================================

    override fun onResume() {

        super.onResume()

        telaAtiva = true

        if (navegando) return

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

        } else if (!popupAberto && ::root.isInitialized) {

            mostrarPopupVitoria(
                root.width.toFloat() / larguraBase,
                root.height.toFloat() / alturaBase
            )
        }
    }

    // =====================================================
    // PAUSAR ANIMAÇÕES E ÁUDIO
    // =====================================================

    override fun onPause() {

        telaAtiva = false

        pararTodasPulsacoes()

        pararAudio()

        gifsAnimais.values.forEach {

            it.stop()
        }

        super.onPause()
    }

    // =====================================================
    // LIBERAR RECURSOS
    // =====================================================

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        pararTodasPulsacoes()

        pararAudio()

        limparGifs()

        super.onDestroy()
    }
}