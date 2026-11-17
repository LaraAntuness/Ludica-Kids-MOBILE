package com.example.mobilejogos

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
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

    // CONTROLE DOS ANIMAIS

    private val animaisConcluidos = mutableSetOf<String>()

    private val totalAnimais = 4

    private var jogoFinalizado = false

    // ALTERE PARA A ACTIVITY DO PRÓXIMO JOGO

    private val proximaFaseActivity = "ProximaFaseActivity"

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

        root.removeAllViews()

        for (alvo in alvosGif) {
            Glide.with(this).clear(alvo)
        }

        alvosGif.clear()

        animaisConcluidos.clear()

        jogoFinalizado = false

        val sx = root.width.toFloat() / larguraBase
        val sy = root.height.toFloat() / alturaBase

        // FUNDO

        val fundo = ImageView(this).apply {

            setImageResource(
                R.drawable.planomaoszinhasmagicas
            )

            scaleType = ImageView.ScaleType.FIT_XY
        }

        root.addView(
            fundo,
            0,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // BOTÃO VOLTAR

        val voltar = adicionarImagem(
            "botaoretornocinza",
            19, 25, 40, 40, sx, sy
        )

        voltar.contentDescription = "Voltar"

        voltar.isClickable = true

        voltar.setOnClickListener {
            finish()
        }

        // TÍTULO

        adicionarImagem(
            "titulodescubracomotoque",
            205, 2, 490, 85, sx, sy
        )

        val instrucao = TextView(this).apply {
            text = "Toque nos animais"
            textSize = 22f
            setTextColor(Color.rgb(115, 95, 75))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        adicionarView(
            instrucao,
            311, 75, 300, 40, sx, sy
        )

        // LOBO

        adicionarAnimal(
            "lobo.gif",
            77, 40, 210, 180, sx, sy
        )

        // SAPO

        adicionarAnimal(
            "sapo.gif",
            420, 185, 210, 180, sx, sy
        )

        // CASTOR

        adicionarAnimal(
            "castor.gif",
            285, 210, 210, 180, sx, sy
        )

        // MACACO

        adicionarAnimal(
            "macaco.gif",
            600, 100, 300, 250, sx, sy
        )
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
            x, y, largura, altura, sx, sy
        )

        return imagem
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
        sy: Float
    ) {

        val animal = ImageView(this)

        animal.scaleType =
            ImageView.ScaleType.FIT_CENTER

        adicionarView(
            animal,
            x, y, largura, altura, sx, sy
        )

        val caminho = "file:///android_asset/$arquivo"

        val alvo = object : CustomTarget<GifDrawable>() {

            override fun onResourceReady(
                resource: GifDrawable,
                transition: Transition<in GifDrawable>?
            ) {

                // GIF COMEÇA PARADO

                resource.stop()

                resource.setVisible(true, false)

                // EXECUTAR UMA VEZ POR TOQUE

                resource.setLoopCount(1)

                animal.setImageDrawable(resource)

                // IDENTIFICAR QUANDO A ANIMAÇÃO TERMINA

                resource.registerAnimationCallback(
                    object : Animatable2Compat.AnimationCallback() {

                        override fun onAnimationEnd(
                            drawable: Drawable?
                        ) {

                            // CONTAR O ANIMAL SOMENTE
                            // DEPOIS QUE A ANIMAÇÃO TERMINAR

                            runOnUiThread {

                                if (!jogoFinalizado) {

                                    registrarAnimalConcluido(
                                        arquivo
                                    )
                                }
                            }
                        }
                    }
                )

                // TOQUE NO ANIMAL

                animal.setOnClickListener {

                    if (
                        jogoFinalizado ||
                        arquivo in animaisConcluidos
                    ) {
                        return@setOnClickListener
                    }

                    val gif =
                        animal.drawable as? GifDrawable

                    if (gif != null && !gif.isRunning) {

                        gif.start()
                    }
                }
            }

            override fun onLoadCleared(
                placeholder: Drawable?
            ) {

                animal.setImageDrawable(null)
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
    // CONTROLE DE CONCLUSÃO DOS ANIMAIS
    // =====================================================

    private fun registrarAnimalConcluido(
        arquivo: String
    ) {

        if (jogoFinalizado) {
            return
        }

        // O SET IMPEDE CONTAR O MESMO ANIMAL DUAS VEZES

        val novoAnimal = animaisConcluidos.add(arquivo)

        if (!novoAnimal) {
            return
        }

        // VERIFICAR SE TODOS TERMINARAM

        if (animaisConcluidos.size == totalAnimais) {

            jogoFinalizado = true

            handler.postDelayed({

                if (!isFinishing && !isDestroyed) {

                    mostrarPopupVitoria(
                        root.width.toFloat() / larguraBase,
                        root.height.toFloat() / alturaBase
                    )
                }

            }, 500)
        }
    }

    // =====================================================
    // POP-UP DE VITÓRIA
    // =====================================================

    private fun mostrarPopupVitoria(
        sx: Float,
        sy: Float
    ) {

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

        // RETÂNGULO COM CORES PASTÉIS

        val popup = FrameLayout(this).apply {

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(255, 253, 245),
                    Color.rgb(255, 249, 228)
                )
            ).apply {

                cornerRadius = 28f * minOf(sx, sy)

                setStroke(
                    (3 * minOf(sx, sy))
                        .toInt()
                        .coerceAtLeast(2),
                    Color.rgb(245, 222, 160)
                )
            }

            elevation = 12f

            isClickable = true
        }

        // TAMANHO MENOR DO POP-UP

        val popupParams = FrameLayout.LayoutParams(
            (540 * sx).toInt(),
            (260 * sy).toInt(),
            Gravity.CENTER
        )

        root.addView(popup, popupParams)

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
                    tamanho * minOf(sx, sy)
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

        // GIRAFINHA AUMENTADA

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

        // =================================================
        // TÍTULO
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

        // MENSAGEM PERSONALIZADA

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
                    23f * minOf(sx, sy)
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

                    cornerRadius = 20f * minOf(sx, sy)

                    setStroke(
                        (2 * minOf(sx, sy))
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
            finish()
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
                    .setDuration(400)
                    .start()
            }
            .start()
    }

    // =====================================================
    // ABRIR PRÓXIMO NÍVEL
    // =====================================================

    private fun abrirProximoNivel() {

        try {

            val intent = Intent().apply {

                setClassName(
                    this@MaoszinhasMagicasActivity,
                    "$packageName.$proximaFaseActivity"
                )
            }

            startActivity(intent)

        } catch (e: ActivityNotFoundException) {

            Toast.makeText(
                this,
                "Próximo nível em breve!",
                Toast.LENGTH_SHORT
            ).show()
        }
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
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        root.addView(view, parametros)
    }

    // =====================================================
    // LIBERAR RECURSOS
    // =====================================================

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        for (alvo in alvosGif) {
            Glide.with(this).clear(alvo)
        }

        alvosGif.clear()

        super.onDestroy()
    }
}