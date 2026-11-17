package com.example.mobilejogos

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
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

class MainActivity : Activity() {

    private lateinit var root: FrameLayout

    private val handler = Handler(Looper.getMainLooper())

    // Nome da Activity do próximo nível
    private val proximaFaseActivity = "ProximaFaseActivity"

    private val animais = listOf(
        Animal("coelho", "coelho", 650, 205, 95, 115),
        Animal("capivara", "capivara", 290, 225, 115, 80),
        Animal("onca", "onça", 175, 120, 110, 85),
        Animal("passaro", "pássaro", 590, 65, 60, 55),
        Animal("burrinho", "burrinho", 15, 170, 155, 155)
    )

    private val selecionados = mutableSetOf<String>()

    private var indiceAtual = 0

    private var finalizado = false

    private var pedidoView: TextView? = null

    private var mensagemView: TextView? = null

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

        root.removeAllViews()

        val sx = root.width / 874f
        val sy = root.height / 402f

        // FUNDO OCUPANDO A TELA TODA

        val fundo = ImageView(this).apply {

            val imagemId = resources.getIdentifier(
                "fundo_toque_nos_animais",
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
            19, 25, 40, 40, sx, sy
        )

        voltar.isClickable = true

        voltar.setOnClickListener {
            finish()
        }

        // TÍTULO DO JOGO

        adicionarImagem(
            "titulo_toquenosanimais",
            205, 2, 490, 85, sx, sy
        )

        // MENSAGEM "TOQUE NO..."

        pedidoView = TextView(this).apply {

            text = ""

            textSize = 28f

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
            240, 85, 394, 42, sx, sy
        )

        // MENSAGEM DE ERRO

        mensagemView = TextView(this).apply {

            textSize = 15f

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
            235, 108, 404, 36, sx, sy
        )

        // ANIMAIS

        animais.forEach { animal ->

            val halo = FrameLayout(this)

            val fundoHalo = GradientDrawable().apply {

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

            halo.background = fundoHalo

            halo.visibility =
                if (animal.imagem in selecionados) {
                    View.VISIBLE
                } else {
                    View.GONE
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
                (animal.largura * sx).toInt(),
                (animal.altura * sy).toInt()
            ).apply {

                leftMargin = (animal.x * sx).toInt()

                topMargin = (animal.y * sy).toInt()
            }

            root.addView(imagem, parametros)

            imagem.setOnClickListener {
                verificarAnimal(animal, halo, sx, sy)
            }
        }

        atualizarPedido()
    }

    // =====================================================
    // POP-UP DE VITÓRIA
    // =====================================================

    private fun mostrarPopupVitoria(sx: Float, sy: Float) {

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

        // RETÂNGULO MENOR E COM CORES SUAVES

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

        // TAMANHO DO POP-UP: 540 x 260

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

        // FUNÇÃO PARA POSICIONAR ELEMENTOS

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

        // FUNÇÃO PARA CRIAR TEXTOS

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

        // =====================================================
        // GIRAFINHA NO LUGAR DO TROFÉU
        // =====================================================

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

        // ESTRELAS SUAVES

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

        // =====================================================
        // TÍTULO DO POP-UP
        // =====================================================

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

        // =====================================================
        // FUNÇÃO PARA CRIAR BOTÕES
        // =====================================================

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

        // =====================================================
        // BOTÃO VOLTAR - AZUL PASTEL
        // =====================================================

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

        // =====================================================
        // BOTÃO PRÓXIMO NÍVEL - VERDE PASTEL
        // =====================================================

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
    // PRÓXIMO NÍVEL
    // =====================================================

    private fun abrirProximoNivel() {

        try {

            val intent = Intent().apply {

                setClassName(
                    this@MainActivity,
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

        val imagemId = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (imagemId != 0) {
            imagem.setImageResource(imagemId)
        }

        imagem.scaleType = ImageView.ScaleType.FIT_CENTER

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

        root.addView(view, parametros)
    }

    // =====================================================
    // VERIFICAR ANIMAL
    // =====================================================

    private fun verificarAnimal(
        animal: Animal,
        halo: View,
        sx: Float,
        sy: Float
    ) {

        if (finalizado || animal.imagem in selecionados) {
            return
        }

        val esperado = animais[indiceAtual]

        if (animal.imagem != esperado.imagem) {

            mensagemView?.apply {

                text = "Animal errado, tente novamente!"

                textSize = 15f

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

            }, 1800)

            return
        }

        selecionados.add(animal.imagem)

        halo.visibility = View.VISIBLE

        indiceAtual++

        if (indiceAtual == animais.size) {

            finalizarJogo(sx, sy)

        } else {

            atualizarPedido()
        }
    }

    // =====================================================
    // ATUALIZAR PEDIDO
    // =====================================================

    private fun atualizarPedido() {

        if (indiceAtual >= animais.size) {
            return
        }

        val nomeAnimal = animais[indiceAtual].nome

        pedidoView?.apply {

            text = "Toque no $nomeAnimal"

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

        handler.removeCallbacksAndMessages(null)

        pedidoView?.visibility = View.GONE

        mensagemView?.visibility = View.GONE

        handler.postDelayed({

            mostrarPopupVitoria(sx, sy)

        }, 450)
    }

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        super.onDestroy()
    }
}