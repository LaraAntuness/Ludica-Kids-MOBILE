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

open class DescubraOCheiroActivity : ComponentActivity() {

    protected open val nivel = 1

    private lateinit var tela: FrameLayout
    private lateinit var conteudo: FrameLayout

    private var escala = 1f
    private var navegando = false
    private var popupAtual: FrameLayout? = null

    private var audioAtual: MediaPlayer? = null

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

    // VERMELHO CORAL SUAVE

    private val vermelho = Paleta(
        "#FFD8D0", "#F5ADA3",
        "#D77D72", "#963E36"
    )

    private data class Opcao(
        val imagem: String,
        val nome: String,
        val paleta: Paleta,
        val correta: Boolean = false
    )

    private val tema: Paleta
        get() = when (nivel) {
            2 -> rosa
            3 -> amarelo
            else -> azul
        }

    private val pergunta: String
        get() = when (nivel) {

            2 -> "Qual alimento tem\num cheiro de fruta?"

            3 -> "O que tem um\ncheiro ruim?"

            else -> "Qual alimento tem\num cheiro doce?"
        }

    private val personagem: String
        get() = when (nivel) {

            2 -> "meninoregando"

            3 -> "meninoruivo"

            else -> "meninadesenhando"
        }

    private val imagemRetorno: String
        get() = when (nivel) {

            2 -> "botaoretornorosa"

            3 -> "botaoretornoamarelo"

            else -> "botaoretornoazul"
        }

    private val explicacao: String
        get() = when (nivel) {

            2 -> "O maracujá tem um cheirinho de fruta!"

            3 -> "O lixo pode ter um cheiro desagradável!"

            else -> "Ele tem um cheirinho doce!"
        }

    private fun opcoes(): List<Opcao> {

        return when (nivel) {

            2 -> listOf(

                Opcao("chocolate", "Chocolate", rosa),

                Opcao("pao", "Pão", amarelo),

                Opcao("maracuja", "Maracujá", verde, true),

                Opcao("batatafrita", "Batata frita", laranja)
            )

            3 -> listOf(

                Opcao("tambor", "Tambor", azul),

                Opcao("urso2", "Ursinho", rosa),

                Opcao("lixo", "Lixo", verde, true),

                Opcao("mochila", "Mochila", roxo)
            )

            else -> listOf(

                Opcao("brigadeiro", "Brigadeiro", rosa, true),

                Opcao("pizza", "Pizza", amarelo),

                Opcao("brocolis", "Brócolis", verde),

                Opcao("macarrao", "Macarrão", laranja)
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

        val fundoTela = imagem(
            "fundodescubra"
        ).apply {

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

        // BOTÃO RETORNO

        val voltar = imagem(
            imagemRetorno,
            "botaoretornoamareloclaro"
        )

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

        // TÍTULO

        val titulo = texto(
            "Nariz Detetive",
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

        // PERSONAGEM ESTÁTICA

        val crianca = imagem(personagem).apply {

            scaleType = ImageView.ScaleType.FIT_END
        }

        colocar(
            conteudo,
            crianca,
            45, 165, 260, 228
        )

        // BALÃO

        criarBalao()

        // CARTÕES

        opcoes().forEachIndexed { indice, opcao ->

            val x = if (indice % 2 == 0) {
                325
            } else {
                578
            }

            val y = if (indice < 2) {
                82
            } else {
                209
            }

            criarCartao(
                opcao,
                x,
                y,
                indice
            )
        }

        criarIndicadorNivel()
    }

    // =========================================
    // BALÃO COM PONTINHA DA FALA
    // =========================================

    private fun criarBalao() {

        val conjunto = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        colocar(
            conteudo,
            conjunto,
            30, 72, 230, 115
        )

        val desenho = object : View(this) {

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
            desenho,
            0, 0, 230, 115
        )

        colocar(
            conjunto,
            texto(
                pergunta,
                19f,
                tema.texto
            ),
            10, 5, 210, 85
        )

        flutuar(
            conjunto,
            3f,
            2700L,
            animacoes
        )
    }

    // =========================================
    // CARTÕES DOS ALIMENTOS
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

                if (opcao.correta) {

                    mostrarPopupAcerto(opcao)

                } else {

                    tocarClique()

                    mostrarPopupErro()
                }
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
    // POP-UP DE ACERTO
    // =========================================

    private fun mostrarPopupAcerto(opcao: Opcao) {

        if (popupAtual != null || navegando) return

        tocarAudio("brilho_suave", 0.20f)

        val camada = criarCamadaPopup()

        val conjunto = criarConjuntoPopup(
            camada,
            640,
            330
        )

        val moldura = FrameLayout(this).apply {

            background = fundo3D(
                verde,
                32f
            )

            elevation = px(5).toFloat()

            clipChildren = false
            clipToPadding = false
        }

        colocar(
            conjunto,
            moldura,
            20, 35, 580, 285
        )

        // TÍTULO SUPERIOR

        colocar(
            moldura,
            texto(
                "Missão cumprida!",
                28f,
                verde.texto
            ),
            50, 5, 435, 43
        )

        // PAINEL INTERNO

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
            12, 48, 556, 222
        )

        // LUPA GRANDE COM BRILHOS

        adicionarLupaDecorativa(conjunto)

        // CÍRCULO DO ALIMENTO

        val circulo = View(this).apply {

            background = fundo(
                verde.topo,
                verde.base,
                100f
            )
        }

        colocar(
            painel,
            circulo,
            35, 13, 150, 150
        )

        val figura = imagem(opcao.imagem)

        colocar(
            painel,
            figura,
            47, 24, 126, 126
        )

        flutuar(
            figura,
            4f,
            2100L,
            animacoesPopup
        )

        // TEXTO DO ACERTO

        colocar(
            painel,
            texto(
                "Você descobriu!",
                25f,
                verde.texto
            ),
            205, 10, 340, 40
        )

        // NOME DO ALIMENTO

        val nome = texto(
            opcao.nome,
            25f,
            verde.texto
        ).apply {

            background = fundo3D(
                verde,
                19f
            )

            setPadding(
                0, 0, 0, px(4)
            )
        }

        colocar(
            painel,
            nome,
            220, 57, 310, 49
        )

        // EXPLICAÇÃO

        colocar(
            painel,
            texto(
                explicacao,
                18f,
                "#537B4D"
            ),
            205, 111, 340, 55
        )

        // VOLTAR PARA O MESMO NÍVEL

        botao(
            painel,
            "← Voltar",
            azul,
            12, 173, 260, 43
        ) {

            tocarClique()

            fecharPopup()
        }

        // PRÓXIMO NÍVEL OU CONCLUSÃO

        botao(
            painel,
            if (nivel == 3) {
                "Concluir desafio"
            } else {
                "Próximo nível →"
            },
            verde,
            284, 173, 260, 43
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
    // POP-UP DE ERRO
    // =========================================

    private fun mostrarPopupErro() {

        if (popupAtual != null || navegando) return

        val camada = criarCamadaPopup()

        val conjunto = criarConjuntoPopup(
            camada,
            640,
            330
        )

        // MOLDURA VERMELHO CORAL

        val moldura = FrameLayout(this).apply {

            background = fundo3D(
                vermelho,
                32f
            )

            elevation = px(5).toFloat()

            clipChildren = false
            clipToPadding = false
        }

        colocar(
            conjunto,
            moldura,
            20, 35, 580, 285
        )

        // TÍTULO

        colocar(
            moldura,
            texto(
                "Ops, detetive!",
                28f,
                vermelho.texto
            ),
            50, 5, 435, 43
        )

        // PAINEL CREME

        val painel = FrameLayout(this).apply {

            background = fundo(
                "#FFFDF8",
                "#FFF5EF",
                25f
            )

            clipChildren = false
            clipToPadding = false
        }

        colocar(
            moldura,
            painel,
            12, 48, 556, 222
        )

        // LUPA GRANDE COM BRILHOS

        adicionarLupaDecorativa(conjunto)

        // CÍRCULO DO BUBBA

        val circulo = View(this).apply {

            background = fundo(
                "#FFE9DF",
                "#F8D7CB",
                100f
            )
        }

        colocar(
            painel,
            circulo,
            35, 13, 150, 150
        )

        // BUBBA ORIGINAL COM DÚVIDA

        val bubba = imagem(
            "girafaduvida",
            "logoinferior"
        )

        colocar(
            painel,
            bubba,
            40, 18, 140, 140
        )

        flutuar(
            bubba,
            3f,
            2400L,
            animacoesPopup
        )

        // INTERROGAÇÃO DECORATIVA

        val interrogacao = texto(
            "?",
            32f,
            "#D68E80"
        ).apply {

            rotation = 12f
        }

        colocar(
            painel,
            interrogacao,
            168, 7, 40, 42
        )

        flutuar(
            interrogacao,
            4f,
            2300L,
            animacoesPopup
        )

        // MENSAGEM DE ERRO

        colocar(
            painel,
            texto(
                "Ainda não foi dessa vez!",
                24f,
                vermelho.texto
            ),
            210, 22, 335, 50
        )

        colocar(
            painel,
            texto(
                "Vamos imaginar os cheirinhos\ne tentar outra vez?",
                19f,
                "#987064"
            ),
            210, 78, 335, 75
        )

        // BOTÃO VERMELHO CORAL

        botao(
            painel,
            "↻ Tentar novamente",
            vermelho,
            103, 173, 350, 43
        ) {

            tocarClique()

            fecharPopup()
        }

        animarEntrada(conjunto)
    }

    // =========================================
    // LUPA NO CANTO SUPERIOR DIREITO
    // =========================================

    private fun adicionarLupaDecorativa(
        conjunto: FrameLayout
    ) {

        val decoracao = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false

            elevation = px(15).toFloat()

            isClickable = false
            isFocusable = false
        }

        colocar(
            conjunto,
            decoracao,
            493, 0, 147, 138
        )

        // LUPA

        val lupa = criarLupa().apply {

            rotation = -12f
        }

        colocar(
            decoracao,
            lupa,
            18, 7, 112, 112
        )

        // BALANÇO SUAVE DA LUPA

        val balanco = ObjectAnimator.ofFloat(
            lupa,
            View.ROTATION,
            -12f,
            -5f,
            -12f
        ).apply {

            duration = 2900L

            repeatCount = ValueAnimator.INFINITE

            start()
        }

        animacoesPopup.add(balanco)

        // BRILHINHOS AO REDOR

        adicionarBrilho(
            decoracao,
            4, 13, 17, 0L
        )

        adicionarBrilho(
            decoracao,
            119, 4, 19, 400L
        )

        adicionarBrilho(
            decoracao,
            0, 64, 12, 800L
        )

        adicionarBrilho(
            decoracao,
            131, 55, 14, 1200L
        )

        adicionarBrilho(
            decoracao,
            91, 113, 11, 600L
        )

        adicionarPontinho(
            decoracao,
            26, 0, 5, 300L
        )

        adicionarPontinho(
            decoracao,
            137, 32, 6, 900L
        )
    }

    // =========================================
    // DESENHO DA LUPA
    // =========================================

    private fun criarLupa(): View {

        return object : View(this) {

            private val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                canvas.save()

                canvas.scale(
                    width / 120f,
                    height / 120f
                )

                paint.shader = null

                // SOMBRA DO CABO

                paint.style = Paint.Style.STROKE

                paint.strokeCap = Paint.Cap.ROUND

                paint.strokeWidth = 23f

                paint.color = cor("#B98349")

                canvas.drawLine(
                    72f, 72f,
                    105f, 105f,
                    paint
                )

                // CABO DOURADO

                paint.strokeWidth = 16f

                paint.color = cor("#E2B579")

                canvas.drawLine(
                    71f, 69f,
                    104f, 102f,
                    paint
                )

                // SOMBRA DA MOLDURA

                paint.style = Paint.Style.FILL

                paint.color = cor("#C39142")

                canvas.drawCircle(
                    46f, 48f, 42f,
                    paint
                )

                // MOLDURA DOURADA

                paint.shader = LinearGradient(
                    0f, 0f,
                    0f, 90f,
                    cor("#FFE6A1"),
                    cor("#E9B64D"),
                    Shader.TileMode.CLAMP
                )

                canvas.drawCircle(
                    46f, 44f, 41f,
                    paint
                )

                // SOMBRA INTERNA DA LENTE

                paint.shader = null

                paint.color = cor("#96CADF")

                canvas.drawCircle(
                    46f, 44f, 31f,
                    paint
                )

                // LENTE AZUL

                paint.shader = LinearGradient(
                    0f, 12f,
                    0f, 75f,
                    cor("#E5F7FF"),
                    cor("#B7E0F1"),
                    Shader.TileMode.CLAMP
                )

                canvas.drawCircle(
                    46f, 42f, 28f,
                    paint
                )

                // REFLEXO DA LENTE

                paint.shader = null

                paint.style = Paint.Style.STROKE

                paint.strokeWidth = 6f

                paint.strokeCap = Paint.Cap.ROUND

                paint.color = cor("#F8FDFF")

                val reflexo = Path().apply {

                    moveTo(27f, 42f)

                    quadTo(
                        27f, 24f,
                        45f, 24f
                    )
                }

                canvas.drawPath(
                    reflexo,
                    paint
                )

                // PONTINHO DE REFLEXO

                paint.style = Paint.Style.FILL

                canvas.drawCircle(
                    53f, 24f, 3f,
                    paint
                )

                canvas.restore()
            }
        }
    }

    // =========================================
    // BRILHOS DOURADOS
    // =========================================

    private fun criarBrilho(): View {

        return object : View(this) {

            private val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                val w = width.toFloat()
                val h = height.toFloat()

                val caminho = Path().apply {

                    moveTo(w * 0.5f, h * 0.02f)

                    quadTo(
                        w * 0.56f, h * 0.02f,
                        w * 0.62f, h * 0.32f
                    )

                    quadTo(
                        w * 0.68f, h * 0.40f,
                        w * 0.98f, h * 0.5f
                    )

                    quadTo(
                        w * 0.98f, h * 0.56f,
                        w * 0.68f, h * 0.62f
                    )

                    quadTo(
                        w * 0.60f, h * 0.68f,
                        w * 0.5f, h * 0.98f
                    )

                    quadTo(
                        w * 0.44f, h * 0.98f,
                        w * 0.38f, h * 0.68f
                    )

                    quadTo(
                        w * 0.32f, h * 0.60f,
                        w * 0.02f, h * 0.5f
                    )

                    quadTo(
                        w * 0.02f, h * 0.44f,
                        w * 0.32f, h * 0.38f
                    )

                    quadTo(
                        w * 0.40f, h * 0.32f,
                        w * 0.5f, h * 0.02f
                    )

                    close()
                }

                paint.style = Paint.Style.FILL

                paint.pathEffect =
                    CornerPathEffect(w * 0.08f)

                paint.shader = LinearGradient(
                    0f, 0f,
                    0f, h,
                    cor("#FFF0AA"),
                    cor("#FFC94D"),
                    Shader.TileMode.CLAMP
                )

                canvas.drawPath(
                    caminho,
                    paint
                )
            }
        }
    }

    private fun adicionarBrilho(
        pai: FrameLayout,
        x: Int,
        y: Int,
        tamanho: Int,
        atraso: Long
    ) {

        val brilho = criarBrilho()

        colocar(
            pai,
            brilho,
            x, y, tamanho, tamanho
        )

        animarBrilho(
            brilho,
            atraso
        )
    }

    private fun adicionarPontinho(
        pai: FrameLayout,
        x: Int,
        y: Int,
        tamanho: Int,
        atraso: Long
    ) {

        val ponto = View(this).apply {

            background = fundo(
                "#FFF4C1",
                "#FFD36A",
                100f
            )
        }

        colocar(
            pai,
            ponto,
            x, y, tamanho, tamanho
        )

        animarBrilho(
            ponto,
            atraso
        )
    }

    private fun animarBrilho(
        view: View,
        atraso: Long
    ) {

        val animacaoAlpha = ObjectAnimator.ofFloat(
            view,
            View.ALPHA,
            0.35f,
            1f,
            0.35f
        ).apply {

            duration = 2200L

            startDelay = atraso

            repeatCount = ValueAnimator.INFINITE

            start()
        }

        animacoesPopup.add(animacaoAlpha)

        arrayOf(
            View.SCALE_X,
            View.SCALE_Y
        ).forEach { propriedade ->

            val animacao = ObjectAnimator.ofFloat(
                view,
                propriedade,
                0.75f,
                1.12f,
                0.75f
            ).apply {

                duration = 2200L

                startDelay = atraso

                repeatCount = ValueAnimator.INFINITE

                start()
            }

            animacoesPopup.add(animacao)
        }
    }

    // =========================================
    // POP-UP FINAL DE PARABÉNS
    // =========================================

    private fun mostrarPopupFinal() {

        tocarAudio(
            "brilho_suave",
            0.20f
        )

        val camada = criarCamadaPopup()

        val conjunto = criarConjuntoPopup(
            camada,
            550,
            345
        )

        val moldura = FrameLayout(this).apply {

            background = fundo3D(
                amarelo,
                34f
            )

            elevation = px(4).toFloat()

            clipChildren = false
            clipToPadding = false
        }

        colocar(
            conjunto,
            moldura,
            5, 36, 540, 300
        )

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

        // FAIXA AMARELA

        colocar(
            painel,
            faixaParabens(),
            50, 12, 416, 65
        )

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

        // TROFÉU

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

        // MENSAGEM FINAL

        colocar(
            painel,
            texto(
                "Você completou todo o desafio!",
                19f,
                "#94702F"
            ),
            15, 77, 486, 30
        )

        // BUBBA

        val bubba = imagem(
            "rostogirafa",
            "logoinferior"
        )

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

                paint.shader = null

                paint.color = cor("#EFB83F")

                canvas.drawRoundRect(
                    43f, 65f,
                    57f, 86f,
                    5f, 5f,
                    paint
                )

                paint.color = cor("#F5C653")

                canvas.drawRoundRect(
                    27f, 82f,
                    73f, 94f,
                    6f, 6f,
                    paint
                )

                val caminho = caminhoEstrela(
                    50f,
                    34f,
                    16f
                )

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
    // ESTRELA ARREDONDADA
    // =========================================

    private fun caminhoEstrela(
        cx: Float,
        cy: Float,
        raio: Float
    ): Path {

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

        return caminho
    }

    private fun estrela(): View {

        return object : View(this) {

            private val paint =
                Paint(Paint.ANTI_ALIAS_FLAG)

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                val raio =
                    minOf(width, height) * 0.46f

                val caminho = caminhoEstrela(
                    width / 2f,
                    height / 2f,
                    raio
                )

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

            clipChildren = false

            clipToPadding = false

            elevation = px(50).toFloat()
        }

        popupAtual = camada

        tela.addView(
            camada,
            FrameLayout.LayoutParams(-1, -1)
        )

        return camada
    }

    private fun criarConjuntoPopup(
        camada: FrameLayout,
        largura: Int,
        altura: Int
    ): FrameLayout {

        val conjunto = FrameLayout(this).apply {

            clipChildren = false

            clipToPadding = false
        }

        camada.addView(
            conjunto,
            FrameLayout.LayoutParams(
                px(largura),
                px(altura),
                Gravity.CENTER
            )
        )

        return conjunto
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
        raio: Float
    ): GradientDrawable {

        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                cor(topo),
                cor(base)
            )
        ).apply {

            cornerRadius = raio * escala
        }
    }

    // =========================================
    // ANIMAÇÕES
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

        arrayOf(
            View.SCALE_X,
            View.SCALE_Y
        ).forEach { propriedade ->

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

    private fun imagem(
        nome: String,
        alternativa: String? = null
    ): ImageView {

        return ImageView(this).apply {

            scaleType = ImageView.ScaleType.FIT_CENTER

            var id = resources.getIdentifier(
                nome,
                "drawable",
                packageName
            )

            if (id == 0 && alternativa != null) {

                id = resources.getIdentifier(
                    alternativa,
                    "drawable",
                    packageName
                )
            }

            if (id != 0) {

                setImageResource(id)

            } else {

                android.util.Log.e(
                    "NarizDetetive",
                    "Imagem não encontrada: $nome"
                )
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

            1 -> DescubraOCheiro2Activity::class.java

            2 -> DescubraOCheiro3Activity::class.java

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
                DescubraOCheiroActivity::class.java
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
    // ÁUDIOS
    // =========================================

    private fun tocarClique() {

        tocarAudio(
            "clique_pop",
            0.18f
        )
    }

    private fun tocarAudio(
        nome: String,
        volume: Float
    ) {

        val id = resources.getIdentifier(
            nome,
            "raw",
            packageName
        )

        if (id == 0) return

        pararAudio()

        try {

            audioAtual = MediaPlayer.create(
                this,
                id
            )?.apply {

                setVolume(
                    volume,
                    volume
                )

                setOnCompletionListener { player ->

                    if (audioAtual === player) {

                        audioAtual = null

                        player.release()
                    }
                }

                start()
            }

        } catch (_: Exception) {

            pararAudio()
        }
    }

    private fun pararAudio() {

        val player = audioAtual

        audioAtual = null

        player?.let {

            try {

                it.setOnCompletionListener(null)

                it.release()

            } catch (_: Exception) {
            }
        }
    }

    // =========================================
    // TELA CHEIA
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

        pararAudio()

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

        pararAudio()

        super.onDestroy()
    }
}
