package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import kotlin.math.cos
import kotlin.math.sin

open class ProveNaImaginacaoActivity : ComponentActivity() {

    protected open val nivel = 1

    private lateinit var tela: FrameLayout
    private lateinit var conteudo: FrameLayout

    private var escala = 1f
    private var popupAtual: FrameLayout? = null
    private var navegando = false

    private var efeito: MediaPlayer? = null

    private val animacoes = mutableListOf<ObjectAnimator>()
    private val animacoesPopup = mutableListOf<ObjectAnimator>()

    private data class Paleta(
        val topo: String,
        val base: String,
        val sombra: String,
        val texto: String
    )

    private val azul = Paleta(
        "#DDF3FF", "#B7DFF5",
        "#82BBDD", "#285B85"
    )

    private val rosa = Paleta(
        "#FFE5F1", "#F5C8E0",
        "#D99CBE", "#884A76"
    )

    private val amarelo = Paleta(
        "#FFF7D8", "#FFE9AA",
        "#DDBF70", "#927025"
    )

    private val verde = Paleta(
        "#E4F7D8", "#C2E8AF",
        "#8FC47D", "#386C35"
    )

    private val roxo = Paleta(
        "#EFE5FF", "#D8C9F5",
        "#B09AD9", "#685096"
    )

    private val laranja = Paleta(
        "#FFEBD6", "#FFD7AC",
        "#E4AF79", "#9C622D"
    )

    private data class Opcao(
        val imagem: String,
        val nome: String,
        val paleta: Paleta
    )

    private val tema: Paleta
        get() = when (nivel) {
            2 -> rosa
            3 -> verde
            else -> azul
        }

    private val tituloJogo: String
        get() = when (nivel) {
            2 -> "Melhor Sabor"
            3 -> "Melhor Salgado"
            else -> "Bebida Favorita"
        }

    private val perguntaJogo: String
        get() = when (nivel) {
            2 -> "Qual alimento\nvocê mais\ngosta?"
            3 -> "Qual é o seu\nsalgado\nfavorito?"
            else -> "Qual é a sua\nbebida\nfavorita?"
        }

    private val imagemCriancas: String
        get() = when (nivel) {
            2 -> "criancascomendo"
            3 -> "criancassuporte"
            else -> "criancasabracadas"
        }

    private fun opcoes(): List<Opcao> {

        return when (nivel) {

            2 -> listOf(
                Opcao("maca", "Maçã", rosa),
                Opcao("pizza", "Pizza", amarelo),
                Opcao("sorvete", "Sorvete", roxo),
                Opcao("banana", "Banana", verde)
            )

            3 -> listOf(
                Opcao("batatafrita", "Batata frita", amarelo),
                Opcao("sanduiche", "Sanduíche", verde),
                Opcao("coxinha", "Coxinha", laranja),
                Opcao("pipoca", "Pipoca", rosa)
            )

            else -> listOf(
                Opcao("agua", "Água", azul),
                Opcao("milkshake", "Milkshake", rosa),
                Opcao("suco", "Suco", amarelo),
                Opcao("refrigerante", "Refrigerante", roxo)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        aplicarModoImersivo()

        tela = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        setContentView(tela)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    tocarClique()

                    if (popupAtual != null) {
                        fecharPopup()
                    } else {
                        voltarParaJogos()
                    }
                }
            }
        )

        tela.post {

            if (!isFinishing && !isDestroyed) {

                escala = minOf(
                    tela.width / 874f,
                    tela.height / 393f
                ).coerceAtLeast(0.1f)

                montarTela()
            }
        }
    }

    // =========================================
    // TELA PRINCIPAL
    // =========================================

    private fun montarTela() {

        cancelarAnimacoes(animacoes)
        cancelarAnimacoes(animacoesPopup)

        tela.removeAllViews()

        popupAtual = null
        navegando = false

        val fundoTela = imagem("fundoimaginacaoatualizado").apply {

            scaleType = ImageView.ScaleType.FIT_XY
        }

        tela.addView(
            fundoTela,
            FrameLayout.LayoutParams(-1, -1)
        )

        conteudo = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        tela.addView(
            conteudo,
            FrameLayout.LayoutParams(
                px(874),
                px(393),
                Gravity.CENTER
            )
        )

        // BOTÃO DE RETORNO

        val nomeRetorno = when (nivel) {

            2 -> "botaoretornorosa"

            3 -> "botaoretornoverde"

            else -> "botaoretornoazul"
        }

        val voltar = imagem(nomeRetorno)

        colocar(
            conteudo,
            voltar,
            19, 18, 43, 43
        )

        voltar.contentDescription = "Voltar aos jogos"

        toque(voltar) {

            tocarClique()
            voltarParaJogos()
        }

        // TÍTULO NOVO

        val titulo = texto(
            tituloJogo,
            39f,
            tema.texto
        ).apply {

            typeface = Typeface.create(
                "sans-serif-black",
                Typeface.BOLD
            )

            letterSpacing = 0.015f

            setShadowLayer(
                2.5f * escala,
                0f,
                2f * escala,
                Color.WHITE
            )
        }

        colocar(
            conteudo,
            titulo,
            250, 5, 470, 67
        )

        // CRIANÇAS ESTÁTICAS

        // CRIANÇAS ESTÁTICAS EM TONS PASTEL

        val criancas = imagem(imagemCriancas).apply {

            val saturacao = ColorMatrix().apply {
                setSaturation(0.75f)
            }

            val suavizacao = ColorMatrix(
                floatArrayOf(
                    0.90f, 0f, 0f, 0f, 22f,
                    0f, 0.90f, 0f, 0f, 22f,
                    0f, 0f, 0.90f, 0f, 22f,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            saturacao.postConcat(suavizacao)

            colorFilter = ColorMatrixColorFilter(saturacao)
        }

        val posicaoY = if (nivel == 2) {
            194
        } else {
            171
        }

        val alturaCriancas = if (nivel == 2) {
            199
        } else {
            207
        }

        colocar(
            conteudo,
            criancas,
            65,
            posicaoY,
            230,
            alturaCriancas
        )

        // SEM ANIMAÇÃO NAS CRIANÇAS

        criarBalao()

        // CARTÕES

        opcoes().forEachIndexed { i, opcao ->

            val x = if (i % 2 == 0) 325 else 578

            val y = if (i < 2) 82 else 209

            criarCartao(
                opcao,
                x,
                y,
                i
            )
        }

        criarIndicadorNivel()
    }

    // =========================================
    // BALÃO COM PONTINHA CORRIGIDA
    // =========================================

    private fun criarBalao() {

        val conjunto = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        colocar(
            conteudo,
            conjunto,
            30, 72, 195, 115
        )

        val desenhoBalao = object : View(this) {

            private val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                val w = width.toFloat()
                val h = height.toFloat()

                val caminho = Path().apply {

                    moveTo(w * 0.12f, h * 0.02f)

                    lineTo(w * 0.88f, h * 0.02f)

                    quadTo(
                        w * 0.98f, h * 0.02f,
                        w * 0.98f, h * 0.18f
                    )

                    lineTo(w * 0.98f, h * 0.66f)

                    quadTo(
                        w * 0.98f, h * 0.82f,
                        w * 0.88f, h * 0.82f
                    )

                    lineTo(w * 0.73f, h * 0.82f)

                    lineTo(w * 0.61f, h * 0.97f)

                    lineTo(w * 0.56f, h * 0.82f)

                    lineTo(w * 0.12f, h * 0.82f)

                    quadTo(
                        w * 0.02f, h * 0.82f,
                        w * 0.02f, h * 0.66f
                    )

                    lineTo(w * 0.02f, h * 0.18f)

                    quadTo(
                        w * 0.02f, h * 0.02f,
                        w * 0.12f, h * 0.02f
                    )

                    close()
                }

                paint.style = Paint.Style.FILL

                paint.shader = LinearGradient(
                    0f, 0f,
                    0f, h,
                    cor("#FFFDF6"),
                    cor("#FFF5E0"),
                    Shader.TileMode.CLAMP
                )

                canvas.drawPath(caminho, paint)

                paint.shader = null

                paint.style = Paint.Style.STROKE

                paint.strokeWidth = 2.5f * escala

                paint.strokeJoin = Paint.Join.ROUND

                paint.color = cor(tema.base)

                canvas.drawPath(caminho, paint)
            }
        }

        colocar(
            conjunto,
            desenhoBalao,
            0, 0, 195, 115
        )

        colocar(
            conjunto,
            texto(
                perguntaJogo,
                19f,
                tema.texto
            ),
            10, 5, 175, 85
        )

        flutuar(
            conjunto,
            3f,
            2700L,
            animacoes
        )
    }

    // =========================================
    // CARTÕES DAS OPÇÕES
    // =========================================

    private fun criarCartao(
        opcao: Opcao,
        x: Int,
        y: Int,
        indice: Int
    ) {

        val cartao = FrameLayout(this).apply {

            background = fundo3D(
                opcao.paleta,
                25f
            )

            elevation = px(3).toFloat()

            clipChildren = false
            clipToPadding = false
        }

        colocar(
            conteudo,
            cartao,
            x, y, 230, 115
        )

        val figura = imagem(opcao.imagem)

        colocar(
            cartao,
            figura,
            12, 9, 92, 91
        )

        colocar(
            cartao,
            texto(
                opcao.nome,
                if (opcao.nome.length > 11) 17f else 21f,
                opcao.paleta.texto
            ),
            104, 9, 118, 90
        )

        cartao.contentDescription = opcao.nome

        flutuar(
            figura,
            3f,
            2300L + indice * 180L,
            animacoes,
            indice * 220L
        )

        toque(cartao) {

            if (popupAtual == null && !navegando) {

                tocarClique()

                mostrarPopup(opcao)
            }
        }
    }

    // =========================================
    // INDICADOR DE NÍVEL
    // =========================================

    private fun criarIndicadorNivel() {

        val indicador = FrameLayout(this).apply {

            background = fundo(
                "#FAFDFF",
                "#EDF5FA",
                17f
            )

            elevation = px(2).toFloat()
        }

        colocar(
            conteudo,
            indicador,
            468, 348, 220, 29
        )

        colocar(
            indicador,
            texto(
                "Nível $nivel/3",
                12f,
                tema.texto
            ),
            5, 0, 90, 29
        )

        for (i in 1..3) {

            val ativo = i == nivel

            val numero = texto(
                i.toString(),
                12f,
                if (ativo) tema.texto else "#93A9B7"
            ).apply {

                background = fundo(
                    if (ativo) tema.topo else "#F0F5F8",
                    if (ativo) tema.base else "#E4EDF3",
                    20f
                )
            }

            colocar(
                indicador,
                numero,
                105 + (i - 1) * 35,
                4, 21, 21
            )
        }
    }

    // =========================================
    // POP-UP DE ESCOLHA
    // =========================================

    private fun mostrarPopup(opcao: Opcao) {

        if (popupAtual != null || navegando) return

        tocarEfeito("brilho_suave", 0.18f)

        val camada = criarCamadaPopup()

        val conjunto = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        camada.addView(
            conjunto,
            FrameLayout.LayoutParams(
                px(590),
                px(310),
                Gravity.CENTER
            )
        )

        val moldura = FrameLayout(this).apply {

            background = fundo3D(
                tema,
                32f
            )

            elevation = px(5).toFloat()

            clipChildren = false
            clipToPadding = false
        }

        colocar(
            conjunto,
            moldura,
            5, 18, 580, 285
        )

        val painel = FrameLayout(this).apply {

            background = fundo(
                "#FFFDF5",
                "#FFF8E8",
                25f
            )

            clipChildren = false
            clipToPadding = false
        }

        colocar(
            moldura,
            painel,
            12, 35, 556, 236
        )

        // ESTRELA SUPERIOR

        val estrelaTopo = estrela()

        colocar(
            conjunto,
            estrelaTopo,
            267, 0, 56, 56
        )

        estrelaTopo.elevation = px(8).toFloat()

        pulsar(
            estrelaTopo,
            1.07f,
            1900L,
            animacoesPopup
        )

        // CÍRCULO DA IMAGEM

        val circulo = View(this).apply {

            background = fundo(
                tema.topo,
                tema.base,
                100f
            )
        }

        colocar(
            painel,
            circulo,
            35, 16, 155, 155
        )

        val figura = imagem(opcao.imagem)

        colocar(
            painel,
            figura,
            48, 25, 129, 136
        )

        flutuar(
            figura,
            4f,
            2100L,
            animacoesPopup
        )

        val estrela1 = estrela()

        val estrela2 = estrela()

        colocar(
            painel,
            estrela1,
            20, 26, 28, 28
        )

        colocar(
            painel,
            estrela2,
            172, 129, 25, 25
        )

        val mensagem = when (nivel) {

            2 -> "Seu alimento preferido é:"

            3 -> "Seu salgado preferido é:"

            else -> "Sua bebida preferida é:"
        }

        colocar(
            painel,
            texto(
                mensagem,
                21f,
                tema.texto
            ),
            210, 20, 330, 38
        )

        val nome = texto(
            opcao.nome,
            25f,
            opcao.paleta.texto
        ).apply {

            background = fundo3D(
                opcao.paleta,
                19f
            )

            setPadding(
                0, 0, 0, px(4)
            )
        }

        colocar(
            painel,
            nome,
            220, 65, 310, 54
        )

        colocar(
            painel,
            texto(
                "Que escolha legal!",
                19f,
                tema.texto
            ),
            210, 128, 330, 35
        )

        // VOLTAR PARA O MESMO NÍVEL

        botao(
            painel,
            "← Voltar",
            azul,
            12, 184, 260, 43
        ) {

            tocarClique()

            fecharPopup()
        }

        botao(
            painel,
            if (nivel == 3) {
                "Concluir desafio"
            } else {
                "Próximo nível →"
            },
            verde,
            284, 184, 260, 43
        ) {

            tocarClique()

            if (nivel == 3) {

                fecharPopup()

                mostrarPopupFinal()

            } else {

                abrirProximoNivel()
            }
        }

        animarEntrada(conjunto)
    }

    // =========================================
    // NOVO POP-UP FINAL
    // =========================================

    private fun mostrarPopupFinal() {

        tocarEfeito("brilho_suave", 0.20f)

        val camada = criarCamadaPopup()

        val conjunto = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        camada.addView(
            conjunto,
            FrameLayout.LayoutParams(
                px(550),
                px(345),
                Gravity.CENTER
            )
        )

        // MOLDURA AMARELA

        val moldura = FrameLayout(this).apply {

            background = fundo3D(
                amarelo,
                34f
            )

            clipChildren = false
            clipToPadding = false

            elevation = px(4).toFloat()
        }

        colocar(
            conjunto,
            moldura,
            5, 36, 540, 300
        )

        // PAINEL CREME AMARELADO

        val painel = FrameLayout(this).apply {

            background = fundo(
                "#FFFDF3",
                "#FFF4D3",
                26f
            )

            clipChildren = false
            clipToPadding = false
        }

        colocar(
            moldura,
            painel,
            12, 12, 516, 270
        )

        // FAIXA DOURADA

        val faixa = faixaParabens()

        colocar(
            painel,
            faixa,
            50, 12, 416, 65
        )

        // TÍTULO DA RECOMPENSA

        val titulo = texto(
            "Parabéns!",
            34f,
            "#98631C"
        ).apply {

            typeface = Typeface.create(
                "sans-serif-black",
                Typeface.BOLD
            )
        }

        colocar(
            painel,
            titulo,
            100, 13, 316, 49
        )

        // TROFÉU SUPERIOR

        val premio = trofeu()

        colocar(
            conjunto,
            premio,
            235, 0, 80, 68
        )

        premio.elevation = px(12).toFloat()

        pulsar(
            premio,
            1.06f,
            2100L,
            animacoesPopup
        )

        // MENSAGEM DE CONCLUSÃO

        colocar(
            painel,
            texto(
                "Você completou todo o desafio!",
                19f,
                "#94702F"
            ),
            15, 77, 486, 30
        )

        // BUBBA MAIOR

        val bubba = imagem("rostogirafa")

        colocar(
            painel,
            bubba,
            173, 108, 170, 110
        )

        pulsar(
            bubba,
            1.04f,
            2000L,
            animacoesPopup
        )

        // ESTRELAS DECORATIVAS

        val estrelaEsquerda = estrela()

        colocar(
            painel,
            estrelaEsquerda,
            112, 140, 39, 39
        )

        val estrelaDireita = estrela()

        colocar(
            painel,
            estrelaDireita,
            365, 140, 39, 39
        )

        flutuar(
            estrelaEsquerda,
            4f,
            2200L,
            animacoesPopup
        )

        flutuar(
            estrelaDireita,
            4f,
            2500L,
            animacoesPopup
        )

        // JOGAR NOVAMENTE

        botao(
            painel,
            "↻ Jogar novamente",
            azul,
            12, 224, 240, 43
        ) {

            tocarClique()

            reiniciarJogo()
        }

        // VOLTAR AOS JOGOS

        botao(
            painel,
            "Voltar aos jogos",
            rosa,
            264, 224, 240, 43
        ) {

            tocarClique()

            voltarParaJogos()
        }

        animarEntrada(conjunto)
    }

    // =========================================
    // FAIXA DOURADA
    // =========================================

    private fun faixaParabens(): View {

        return object : View(this) {

            private val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                val w = width.toFloat()

                val h = height.toFloat()

                paint.shader = null

                paint.style = Paint.Style.FILL

                paint.color = cor("#E9B849")

                val esquerda = Path().apply {

                    moveTo(w * 0.18f, h * 0.16f)

                    lineTo(w * 0.01f, h * 0.36f)

                    lineTo(w * 0.07f, h * 0.59f)

                    lineTo(w * 0.01f, h * 0.91f)

                    lineTo(w * 0.23f, h * 0.79f)

                    close()
                }

                val direita = Path().apply {

                    moveTo(w * 0.82f, h * 0.16f)

                    lineTo(w * 0.99f, h * 0.36f)

                    lineTo(w * 0.93f, h * 0.59f)

                    lineTo(w * 0.99f, h * 0.91f)

                    lineTo(w * 0.77f, h * 0.79f)

                    close()
                }

                paint.pathEffect =
                    CornerPathEffect(5f * escala)

                canvas.drawPath(esquerda, paint)

                canvas.drawPath(direita, paint)

                paint.pathEffect = null

                // SOMBRA INFERIOR

                paint.color = cor("#DDB04A")

                canvas.drawRoundRect(
                    w * 0.10f,
                    h * 0.12f,
                    w * 0.90f,
                    h * 0.88f,
                    15f * escala,
                    15f * escala,
                    paint
                )

                // FRENTE AMARELA

                paint.shader = LinearGradient(
                    0f, 0f,
                    0f, h,
                    cor("#FFECA1"),
                    cor("#FFD05C"),
                    Shader.TileMode.CLAMP
                )

                canvas.drawRoundRect(
                    w * 0.10f,
                    h * 0.03f,
                    w * 0.90f,
                    h * 0.78f,
                    15f * escala,
                    15f * escala,
                    paint
                )

                paint.shader = null
            }
        }
    }

    // =========================================
    // TROFÉU ARREDONDADO
    // =========================================

    private fun trofeu(): View {

        return object : View(this) {

            private val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                canvas.save()

                canvas.scale(
                    width / 100f,
                    height / 100f
                )

                paint.shader = null

                // ALÇAS DO TROFÉU

                paint.color = cor("#EFB83F")

                paint.style = Paint.Style.STROKE

                paint.strokeWidth = 8f

                paint.strokeCap = Paint.Cap.ROUND

                val esquerda = Path().apply {

                    moveTo(28f, 20f)

                    cubicTo(
                        0f, 8f,
                        1f, 55f,
                        33f, 53f
                    )
                }

                val direita = Path().apply {

                    moveTo(72f, 20f)

                    cubicTo(
                        100f, 8f,
                        99f, 55f,
                        67f, 53f
                    )
                }

                canvas.drawPath(esquerda, paint)

                canvas.drawPath(direita, paint)

                // CORPO

                paint.style = Paint.Style.FILL

                paint.shader = LinearGradient(
                    0f, 5f,
                    0f, 75f,
                    cor("#FFED9D"),
                    cor("#FFC343"),
                    Shader.TileMode.CLAMP
                )

                val taca = Path().apply {

                    moveTo(23f, 8f)

                    quadTo(
                        18f, 8f,
                        18f, 15f
                    )

                    lineTo(22f, 38f)

                    cubicTo(
                        25f, 78f,
                        75f, 78f,
                        78f, 38f
                    )

                    lineTo(82f, 15f)

                    quadTo(
                        82f, 8f,
                        75f, 8f
                    )

                    close()
                }

                canvas.drawPath(taca, paint)

                // SUPORTE

                paint.shader = null

                paint.color = cor("#EFB83F")

                canvas.drawRoundRect(
                    43f, 65f,
                    57f, 86f,
                    5f, 5f,
                    paint
                )

                // BASE

                paint.color = cor("#F5C653")

                canvas.drawRoundRect(
                    27f, 82f,
                    73f, 94f,
                    6f, 6f,
                    paint
                )

                // ESTRELA CENTRAL

                val caminho = Path()

                for (i in 0 until 10) {

                    val angulo =
                        Math.PI * i / 5 - Math.PI / 2

                    val raio = if (i % 2 == 0) {
                        16f
                    } else {
                        9f
                    }

                    val x = 50f +
                            cos(angulo).toFloat() * raio

                    val y = 34f +
                            sin(angulo).toFloat() * raio

                    if (i == 0) {

                        caminho.moveTo(x, y)

                    } else {

                        caminho.lineTo(x, y)
                    }
                }

                caminho.close()

                paint.color = cor("#FFF5C5")

                paint.pathEffect =
                    CornerPathEffect(3f)

                canvas.drawPath(caminho, paint)

                paint.pathEffect = null

                canvas.restore()
            }
        }
    }

    // =========================================
    // ESTRELAS SEM CONTORNO
    // =========================================

    private fun estrela(): View {

        return object : View(this) {

            private val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                val cx = width / 2f

                val cy = height / 2f

                val raio =
                    minOf(width, height) * 0.46f

                val caminho = Path()

                for (i in 0 until 10) {

                    val angulo =
                        Math.PI * i / 5 - Math.PI / 2

                    val r = if (i % 2 == 0) {

                        raio

                    } else {

                        raio * 0.58f
                    }

                    val x = cx +
                            cos(angulo).toFloat() * r

                    val y = cy +
                            sin(angulo).toFloat() * r

                    if (i == 0) {

                        caminho.moveTo(x, y)

                    } else {

                        caminho.lineTo(x, y)
                    }
                }

                caminho.close()

                paint.style = Paint.Style.FILL

                paint.pathEffect =
                    CornerPathEffect(raio * 0.20f)

                paint.shader = LinearGradient(
                    0f, 0f,
                    0f, height.toFloat(),
                    cor("#FFE994"),
                    cor("#FFC94D"),
                    Shader.TileMode.CLAMP
                )

                canvas.drawPath(caminho, paint)
            }
        }
    }

    // =========================================
    // CORAÇÕES ARREDONDADOS
    // =========================================

    private fun coracao(): View {

        return object : View(this) {

            private val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                val w = width.toFloat()

                val h = height.toFloat()

                val caminho = Path().apply {

                    moveTo(w * 0.5f, h * 0.92f)

                    cubicTo(
                        w * 0.12f, h * 0.67f,
                        w * 0.02f, h * 0.42f,
                        w * 0.08f, h * 0.23f
                    )

                    cubicTo(
                        w * 0.16f, h * 0.02f,
                        w * 0.38f, h * 0.03f,
                        w * 0.5f, h * 0.27f
                    )

                    cubicTo(
                        w * 0.62f, h * 0.03f,
                        w * 0.84f, h * 0.02f,
                        w * 0.92f, h * 0.23f
                    )

                    cubicTo(
                        w * 0.98f, h * 0.42f,
                        w * 0.88f, h * 0.67f,
                        w * 0.5f, h * 0.92f
                    )

                    close()
                }

                paint.style = Paint.Style.FILL

                paint.pathEffect =
                    CornerPathEffect(w * 0.05f)

                paint.shader = LinearGradient(
                    0f, 0f,
                    0f, h,
                    cor("#FFB9D6"),
                    cor("#EF83B0"),
                    Shader.TileMode.CLAMP
                )

                canvas.drawPath(caminho, paint)
            }
        }
    }

    // =========================================
    // CAMADA DO POP-UP
    // =========================================

    private fun criarCamadaPopup(): FrameLayout {

        animacoes.forEach {

            if (it.isStarted) it.pause()
        }

        val camada = FrameLayout(this).apply {

            setBackgroundColor(
                Color.argb(150, 25, 45, 65)
            )

            isClickable = true

            isFocusable = true

            elevation = px(50).toFloat()
        }

        popupAtual = camada

        tela.addView(
            camada,
            FrameLayout.LayoutParams(-1, -1)
        )

        return camada
    }

    private fun fecharPopup() {

        cancelarAnimacoes(animacoesPopup)

        popupAtual?.let {

            tela.removeView(it)
        }

        popupAtual = null

        animacoes.forEach {

            if (it.isPaused) it.resume()
        }
    }

    private fun animarEntrada(view: View) {

        view.scaleX = 0.80f

        view.scaleY = 0.80f

        view.alpha = 0f

        view.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(320L)
            .setInterpolator(
                OvershootInterpolator(1.1f)
            )
            .start()
    }

    // =========================================
    // BOTÕES 3D
    // =========================================

    private fun botao(
        pai: FrameLayout,
        nome: String,
        paleta: Paleta,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        acao: () -> Unit
    ) {

        val botao = texto(
            nome,
            19f,
            paleta.texto
        ).apply {

            background = fundo3D(
                paleta,
                22f
            )

            setPadding(
                px(5), 0,
                px(5), px(5)
            )

            elevation = px(2).toFloat()
        }

        colocar(
            pai,
            botao,
            x, y, largura, altura
        )

        toque(botao, acao)
    }

    private fun fundo3D(
        paleta: Paleta,
        raio: Float
    ): LayerDrawable {

        val sombra = GradientDrawable().apply {

            setColor(cor(paleta.sombra))

            cornerRadius = raio * escala
        }

        val frente = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                cor(paleta.topo),
                cor(paleta.base)
            )
        ).apply {

            cornerRadius = raio * escala
        }

        return LayerDrawable(
            arrayOf(sombra, frente)
        ).apply {

            setLayerInset(
                1, 0, 0, 0, px(6)
            )
        }
    }

    private fun fundo(
        topo: String,
        base: String,
        raio: Float,
        borda: String? = null,
        espessura: Int = 0
    ): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                cor(topo),
                cor(base)
            )
        ).apply {

            cornerRadius = raio * escala

            if (borda != null && espessura > 0) {

                setStroke(
                    px(espessura),
                    cor(borda)
                )
            }
        }
    }

    // =========================================
    // ANIMAÇÕES SUAVES
    // =========================================

    private fun flutuar(
        view: View,
        distancia: Float,
        tempo: Long,
        lista: MutableList<ObjectAnimator>,
        atraso: Long = 0L
    ) {

        val animacao = ObjectAnimator.ofFloat(
            view,
            View.TRANSLATION_Y,
            0f,
            -distancia * escala,
            0f
        ).apply {

            duration = tempo

            startDelay = atraso

            repeatCount =
                ValueAnimator.INFINITE

            start()
        }

        lista.add(animacao)
    }

    private fun pulsar(
        view: View,
        tamanho: Float,
        tempo: Long,
        lista: MutableList<ObjectAnimator>
    ) {

        val propriedades = arrayOf(
            View.SCALE_X,
            View.SCALE_Y
        )

        propriedades.forEach { propriedade ->

            val animacao = ObjectAnimator.ofFloat(
                view,
                propriedade,
                1f,
                tamanho,
                1f
            ).apply {

                duration = tempo

                repeatCount =
                    ValueAnimator.INFINITE

                start()
            }

            lista.add(animacao)
        }
    }

    private fun toque(
        view: View,
        acao: () -> Unit
    ) {

        view.isClickable = true

        view.isFocusable = true

        view.setOnClickListener {

            if (!view.isEnabled || navegando) {

                return@setOnClickListener
            }

            view.isEnabled = false

            view.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(85L)
                .withEndAction {

                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(140L)
                        .setInterpolator(
                            OvershootInterpolator()
                        )
                        .withEndAction {

                            view.isEnabled = true

                            if (
                                !isFinishing &&
                                !isDestroyed &&
                                view.isAttachedToWindow &&
                                !navegando
                            ) {

                                acao()
                            }
                        }
                        .start()
                }
                .start()
        }
    }

    private fun cancelarAnimacoes(
        lista: MutableList<ObjectAnimator>
    ) {

        lista.forEach {

            it.cancel()
        }

        lista.clear()
    }

    // =========================================
    // FUNÇÕES VISUAIS
    // =========================================

    private fun imagem(nome: String): ImageView {

        return ImageView(this).apply {

            scaleType = ImageView.ScaleType.FIT_CENTER

            val id = resources.getIdentifier(
                nome,
                "drawable",
                packageName
            )

            if (id != 0) {

                setImageResource(id)
            }
        }
    }

    private fun texto(
        conteudo: String,
        tamanho: Float,
        corTexto: String
    ): TextView {

        return TextView(this).apply {

            text = conteudo

            gravity = Gravity.CENTER

            setTextColor(cor(corTexto))

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                tamanho * escala
            )

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )

            includeFontPadding = false
        }
    }

    private fun colocar(
        pai: FrameLayout,
        view: View,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int
    ) {

        val parametros = FrameLayout.LayoutParams(
            px(largura),
            px(altura)
        ).apply {

            leftMargin = (x * escala).toInt()

            topMargin = (y * escala).toInt()
        }

        pai.addView(
            view,
            parametros
        )
    }

    private fun px(valor: Int): Int {

        return (valor * escala)
            .toInt()
            .coerceAtLeast(1)
    }

    private fun cor(valor: String): Int {

        return Color.parseColor(valor)
    }

    // =========================================
    // NAVEGAÇÃO
    // =========================================

    private fun abrirProximoNivel() {

        if (navegando) return

        val destino = when (nivel) {

            1 -> ProveNaImaginacao2Activity::class.java

            2 -> ProveNaImaginacao3Activity::class.java

            else -> return
        }

        navegando = true

        startActivity(
            Intent(this, destino)
        )

        finish()
    }

    private fun reiniciarJogo() {

        if (navegando) return

        navegando = true

        startActivity(
            Intent(
                this,
                ProveNaImaginacaoActivity::class.java
            )
        )

        finish()
    }

    private fun voltarParaJogos() {

        if (navegando) return

        navegando = true

        MusicaSensorial.parar()

        val intent = Intent(
            this,
            CincoOpcoesDosJogosActivity::class.java
        ).apply {

            flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        startActivity(intent)

        finish()
    }

    // =========================================
    // MÚSICA DE FUNDO
    // =========================================

    private fun tocarClique() {

        tocarEfeito(
            "clique_suave",
            0.20f
        )
    }

    private fun tocarEfeito(
        nome: String,
        volume: Float
    ) {

        val id = resources.getIdentifier(
            nome,
            "raw",
            packageName
        )

        if (id == 0) return

        try {

            efeito?.release()

            efeito = null

            efeito = MediaPlayer.create(
                this,
                id
            )?.apply {

                setVolume(
                    volume,
                    volume
                )

                setOnCompletionListener { player ->

                    if (efeito === player) {

                        efeito = null
                    }

                    player.release()
                }

                start()
            }

        } catch (_: Exception) {
        }
    }

    // =========================================
    // MODO TELA CHEIA
    // =========================================

    @Suppress("DEPRECATION")
    private fun aplicarModoImersivo() {

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

            aplicarModoImersivo()
        }
    }

    override fun onPause() {

        super.onPause()

        MusicaSensorial.pausar(this)

        efeito?.release()

        efeito = null

        animacoes.forEach {

            if (it.isStarted) it.pause()
        }

        animacoesPopup.forEach {

            if (it.isStarted) it.pause()
        }
    }

    override fun onResume() {

        super.onResume()

        MusicaSensorial.iniciar(this)

        if (popupAtual == null) {

            animacoes.forEach {

                if (it.isPaused) it.resume()
            }

        } else {

            animacoesPopup.forEach {

                if (it.isPaused) it.resume()
            }
        }
    }

    override fun onDestroy() {

        cancelarAnimacoes(animacoes)

        cancelarAnimacoes(animacoesPopup)

        efeito?.release()

        efeito = null

        super.onDestroy()
    }
}
