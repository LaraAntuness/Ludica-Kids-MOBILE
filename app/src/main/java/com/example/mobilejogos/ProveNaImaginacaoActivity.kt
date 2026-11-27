package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.*
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

class ProveNaImaginacaoActivity : ComponentActivity() {

    private lateinit var tela: FrameLayout
    private lateinit var conteudo: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 393f

    private var escala = 1f

    private var popupAberto = false
    private var navegando = false

    private var popupAtual: FrameLayout? = null

    private val animacoesTela = mutableListOf<ObjectAnimator>()
    private val animacoesPopup = mutableListOf<ObjectAnimator>()

    // ÁUDIOS

    private var musicaAmbiente: AudioTrack? = null
    private var somToque: AudioTrack? = null
    private var somPopup: AudioTrack? = null

    private val taxaAudio = 22050

    // VOLUME DOS SONS

    private val volumeMusica = 0.08f
    private val volumeToque = 0.20f
    private val volumePopup = 0.15f

    // PALETA

    private val creme = "#FFF9EF"
    private val azulTexto = "#395681"
    private val azulBorda = "#BCDDF1"
    private val amarelo = "#F5D58A"
    private val rosa = "#EDB0C2"

    private val fonte = Typeface.create(
        "sans-serif-rounded",
        Typeface.BOLD
    )

    private data class Bebida(
        val imagem: String,
        val nome: String,
        val cor: String,
        val borda: String,
        val etiqueta: String
    )

    private val agua = Bebida(
        "agua",
        "Água",
        "#D9F1FC",
        "#91C9EC",
        "#EDF9FF"
    )

    private val milkshake = Bebida(
        "milkshake",
        "Milkshake",
        "#FBE0E9",
        "#ECA8BE",
        "#FFF0F5"
    )

    private val suco = Bebida(
        "suco",
        "Suco",
        "#FFF0C9",
        "#EBCB83",
        "#FFF8E8"
    )

    private val refrigerante = Bebida(
        "refrigerante",
        "Refrigerante",
        "#EAE0FA",
        "#BCA6E7",
        "#F5EEFF"
    )

    // ==========================================
    // INICIAR TELA
    // ==========================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    tocarSomBolhinha()

                    if (popupAberto) {
                        fecharPopup()
                    } else {
                        voltarParaCincoOpcoes()
                    }
                }
            }
        )
        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_prove_na_imaginacao)

        tela = findViewById(R.id.telaProveNaImaginacao)

        tela.clipChildren = false
        tela.clipToPadding = false

        tela.post {

            if (isFinishing || isDestroyed) return@post

            escala = minOf(
                tela.width / larguraBase,
                tela.height / alturaBase
            ).coerceAtLeast(0.1f)

            montarTela()
        }
    }

    // ==========================================
    // TELA PRINCIPAL
    // ==========================================

    private fun montarTela() {

        cancelarAnimacoes(animacoesTela)
        cancelarAnimacoes(animacoesPopup)

        tela.removeAllViews()

        popupAberto = false
        navegando = false
        popupAtual = null

        // FUNDO ORIGINAL

        val fundo = ImageView(this).apply {

            setImageResource(R.drawable.fundosabor)

            scaleType = ImageView.ScaleType.FIT_XY
        }

        tela.addView(
            fundo,
            FrameLayout.LayoutParams(-1, -1)
        )

        val camadaPastel = View(this).apply {

            setBackgroundColor(
                Color.parseColor("#25FFFFFF")
            )
        }

        tela.addView(
            camadaPastel,
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

        criarTitulo()

        // MENINO ORIGINAL

        adicionarImagem(
            conteudo,
            "menino",
            30, 188, 225, 198
        )

        // NOVO BALÃO

        criarBalao()

        // TRACINHOS AMARELOS DO MENINO

        val detalhesMenino = criarTracinhos(
            "#F5D58A"
        )

        posicionar(
            conteudo,
            detalhesMenino,
            220, 211, 38, 43
        )

        detalhesMenino.rotation = 12f

        pulsar(
            detalhesMenino,
            animacoesTela,
            300L,
            1.12f
        )

        balancar(
            detalhesMenino,
            6f,
            2100L,
            animacoesTela
        )

        // CARTÕES

        criarCartao(
            agua,
            300, 98,
            0L
        )

        criarCartao(
            milkshake,
            550, 98,
            700L
        )

        criarCartao(
            suco,
            300, 231,
            1400L
        )

        criarCartao(
            refrigerante,
            550, 231,
            2100L
        )

        // BOTÃO VOLTAR

        val voltar = adicionarImagem(
            conteudo,
            "botaoretornoazul",
            20, 18, 43, 43
        )

        voltar.contentDescription = "Voltar"

        flutuar(
            voltar,
            1.5f,
            2600L,
            animacoesTela
        )

        animacaoToque(voltar) {

            voltarParaCincoOpcoes()
        }
    }

    // ==========================================
    // TÍTULO ORIGINAL
    // ==========================================

    private fun criarTitulo() {

        val placa = FrameLayout(this).apply {

            background = fundoArredondado(
                creme,
                26f,
                azulBorda,
                2f
            )

            elevation = px(2).toFloat()

            clipChildren = false
        }

        posicionar(
            conteudo,
            placa,
            237, 13, 400, 59
        )

        val contorno = View(this).apply {

            background = fundoTracejado(
                "#E7C9AE",
                22f
            )
        }

        posicionar(
            placa,
            contorno,
            6, 5, 388, 49
        )

        val titulo = criarTexto(
            "Bebida Favorita",
            29f,
            azulTexto
        )

        posicionar(
            placa,
            titulo,
            43, 4, 314, 51
        )

        val estrela1 = criarForma(
            "estrela",
            amarelo
        )

        val estrela2 = criarForma(
            "estrela",
            amarelo
        )

        posicionar(
            placa,
            estrela1,
            14, 14, 30, 30
        )

        posicionar(
            placa,
            estrela2,
            356, 14, 30, 30
        )

        balancar(
            estrela1,
            5f,
            2400L,
            animacoesTela
        )

        balancar(
            estrela2,
            5f,
            2600L,
            animacoesTela,
            600L
        )
    }

    // ==========================================
    // NOVO BALÃO EM FORMATO DE NUVEM
    // ==========================================

    private fun criarBalao() {

        val grupo = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        posicionar(
            conteudo,
            grupo,
            30, 76, 240, 125
        )

        val balao = object : View(this) {

            private val paint = Paint(
                Paint.ANTI_ALIAS_FLAG
            )

            init {

                setLayerType(
                    View.LAYER_TYPE_SOFTWARE,
                    null
                )
            }

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                canvas.save()

                canvas.scale(
                    width / 240f,
                    height / 125f
                )

                val caminho = Path().apply {

                    moveTo(24f, 35f)

                    cubicTo(
                        10f, 20f,
                        37f, 8f,
                        56f, 19f
                    )

                    cubicTo(
                        66f, 2f,
                        90f, 0f,
                        104f, 12f
                    )

                    cubicTo(
                        116f, -2f,
                        139f, 0f,
                        150f, 14f
                    )

                    cubicTo(
                        174f, 4f,
                        196f, 16f,
                        198f, 29f
                    )

                    cubicTo(
                        226f, 27f,
                        236f, 48f,
                        226f, 63f
                    )

                    cubicTo(
                        244f, 88f,
                        214f, 107f,
                        184f, 103f
                    )

                    cubicTo(
                        177f, 113f,
                        163f, 120f,
                        150f, 120f
                    )

                    quadTo(
                        159f, 109f,
                        150f, 104f
                    )

                    cubicTo(
                        127f, 114f,
                        101f, 111f,
                        87f, 101f
                    )

                    cubicTo(
                        60f, 110f,
                        33f, 100f,
                        28f, 90f
                    )

                    cubicTo(
                        0f, 90f,
                        0f, 52f,
                        24f, 35f
                    )

                    close()
                }

                // SOMBRA EXTERNA SUAVE

                paint.style = Paint.Style.FILL

                paint.color =
                    Color.parseColor("#9BBFD9")

                paint.setShadowLayer(
                    5f,
                    0f,
                    4f,
                    Color.parseColor("#607DA6C8")
                )

                canvas.drawPath(
                    caminho,
                    paint
                )

                paint.clearShadowLayer()

                // MOLDURA AZUL

                paint.style = Paint.Style.STROKE

                paint.strokeWidth = 7f

                paint.strokeJoin = Paint.Join.ROUND

                paint.color =
                    Color.parseColor("#A9D1EC")

                canvas.drawPath(
                    caminho,
                    paint
                )

                // INTERIOR CREME

                paint.style = Paint.Style.FILL

                paint.color =
                    Color.parseColor("#FFF9EF")

                canvas.drawPath(
                    caminho,
                    paint
                )

                // CONTORNO INTERNO

                paint.style = Paint.Style.STROKE

                paint.strokeWidth = 1.8f

                paint.color =
                    Color.parseColor("#FFFFFF")

                canvas.drawPath(
                    caminho,
                    paint
                )

                canvas.restore()
            }
        }

        posicionar(
            grupo,
            balao,
            0, 0, 240, 125
        )

        // ESTRELA SUPERIOR

        val estrela = criarForma(
            "estrela",
            amarelo
        )

        posicionar(
            grupo,
            estrela,
            103, -4, 34, 34
        )

        // TEXTO

        val pergunta = criarTexto(
            "Qual é a sua\nbebida favorita?",
            18f,
            azulTexto
        )

        posicionar(
            grupo,
            pergunta,
            31, 32, 178, 59
        )

        // CORAÇÃO ESQUERDO

        val coracao1 = criarForma(
            "coracao",
            rosa
        )

        coracao1.rotation = -18f

        posicionar(
            grupo,
            coracao1,
            17, 68, 22, 22
        )

        // CORAÇÃO DIREITO

        val coracao2 = criarForma(
            "coracao",
            rosa
        )

        coracao2.rotation = 18f

        posicionar(
            grupo,
            coracao2,
            200, 68, 22, 22
        )

        // TRACINHOS ROSAS

        val detalhe1 = criarTracinhos(rosa)

        detalhe1.rotation = -70f

        posicionar(
            grupo,
            detalhe1,
            35, 25, 20, 23
        )

        val detalhe2 = criarTracinhos(rosa)

        detalhe2.rotation = 10f

        posicionar(
            grupo,
            detalhe2,
            185, 25, 20, 23
        )

        // ANIMAÇÕES DO BALÃO

        flutuar(
            grupo,
            3f,
            2500L,
            animacoesTela
        )

        balancar(
            estrela,
            6f,
            2300L,
            animacoesTela
        )

        pulsar(
            coracao1,
            animacoesTela,
            0L,
            1.10f
        )

        pulsar(
            coracao2,
            animacoesTela,
            900L,
            1.10f
        )

        pulsar(
            detalhe1,
            animacoesTela,
            400L,
            1.08f
        )

        pulsar(
            detalhe2,
            animacoesTela,
            1000L,
            1.08f
        )
    }

    // ==========================================
    // TRACINHOS DECORATIVOS
    // ==========================================

    private fun criarTracinhos(
        cor: String
    ): View {

        return object : View(this) {

            private val paint = Paint(
                Paint.ANTI_ALIAS_FLAG
            )

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                canvas.save()

                canvas.scale(
                    width / 100f,
                    height / 100f
                )

                val traco1 = Path().apply {

                    moveTo(18f, 42f)

                    quadTo(
                        8f, 44f,
                        13f, 30f
                    )

                    lineTo(26f, 8f)

                    quadTo(
                        39f, -1f,
                        43f, 13f
                    )

                    quadTo(
                        42f, 23f,
                        18f, 42f
                    )

                    close()
                }

                val traco2 = Path().apply {

                    moveTo(35f, 56f)

                    quadTo(
                        25f, 56f,
                        34f, 44f
                    )

                    lineTo(67f, 25f)

                    quadTo(
                        83f, 22f,
                        83f, 35f
                    )

                    quadTo(
                        82f, 46f,
                        35f, 56f
                    )

                    close()
                }

                val traco3 = Path().apply {

                    moveTo(40f, 74f)

                    quadTo(
                        30f, 65f,
                        47f, 62f
                    )

                    lineTo(79f, 61f)

                    quadTo(
                        96f, 65f,
                        89f, 78f
                    )

                    quadTo(
                        85f, 86f,
                        40f, 74f
                    )

                    close()
                }

                listOf(
                    traco1,
                    traco2,
                    traco3
                ).forEach { caminho ->

                    paint.style = Paint.Style.STROKE

                    paint.strokeWidth = 7f

                    paint.strokeJoin = Paint.Join.ROUND

                    paint.color = Color.WHITE

                    canvas.drawPath(
                        caminho,
                        paint
                    )

                    paint.style = Paint.Style.FILL

                    paint.color =
                        Color.parseColor(cor)

                    canvas.drawPath(
                        caminho,
                        paint
                    )
                }

                canvas.restore()
            }
        }
    }

    // ==========================================
    // CARTÕES DAS BEBIDAS
    // ==========================================

    private fun criarCartao(
        bebida: Bebida,
        x: Int,
        y: Int,
        atraso: Long
    ) {

        val largura = 220
        val altura = 115

        val grupo = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        posicionar(
            conteudo,
            grupo,
            x, y, largura, altura
        )

        val cartao = FrameLayout(this).apply {

            background = fundoArredondado(
                bebida.borda,
                22f
            )

            elevation = px(2).toFloat()

            contentDescription =
                "Selecionar ${bebida.nome}"
        }

        posicionar(
            grupo,
            cartao,
            0, 0, largura, altura
        )

        val moldura = FrameLayout(this).apply {

            background = fundoArredondado(
                "#FFFFFF",
                20f
            )
        }

        posicionar(
            cartao,
            moldura,
            2, 2, 216, 108
        )

        val interior = FrameLayout(this).apply {

            background = fundoArredondado(
                bebida.cor,
                18f,
                bebida.borda,
                1f
            )
        }

        posicionar(
            moldura,
            interior,
            3, 3, 210, 102
        )

        val imagem = adicionarImagem(
            interior,
            bebida.imagem,
            58, 3, 94, 73
        )

        val estrela = criarForma(
            "estrela",
            bebida.borda
        )

        posicionar(
            interior,
            estrela,
            27, 25, 20, 20
        )

        val etiqueta = criarTexto(
            bebida.nome,
            17f,
            azulTexto
        )

        etiqueta.background = fundoArredondado(
            bebida.etiqueta,
            14f,
            bebida.borda,
            1f
        )

        posicionar(
            interior,
            etiqueta,
            22, 76, 166, 23
        )

        flutuar(
            grupo,
            2.5f,
            2800L,
            animacoesTela,
            atraso
        )

        pulsar(
            grupo,
            animacoesTela,
            atraso,
            1.025f
        )

        flutuar(
            imagem,
            2f,
            2200L,
            animacoesTela,
            atraso
        )

        balancar(
            estrela,
            6f,
            2600L,
            animacoesTela,
            atraso
        )

        animacaoToque(cartao) {

            if (!popupAberto && !navegando) {

                mostrarPopup(bebida)
            }
        }
    }

    // ==========================================
    // POP-UP
    // ==========================================

    private fun mostrarPopup(
        bebida: Bebida
    ) {

        if (
            popupAberto ||
            navegando ||
            isFinishing ||
            isDestroyed
        ) return

        popupAberto = true

        animacoesTela.forEach {

            if (it.isStarted && !it.isPaused) {

                it.pause()
            }
        }

        val sobreposicao = FrameLayout(this).apply {

            setBackgroundColor(
                Color.parseColor("#55405268")
            )

            isClickable = true

            elevation = px(30).toFloat()

            clipChildren = false
            clipToPadding = false
        }

        popupAtual = sobreposicao

        tela.addView(
            sobreposicao,
            FrameLayout.LayoutParams(-1, -1)
        )

        val container = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        sobreposicao.addView(
            container,
            FrameLayout.LayoutParams(
                px(530),
                px(310),
                Gravity.CENTER
            )
        )

        // MOLDURA

        val moldura = FrameLayout(this).apply {

            background = fundoArredondado(
                azulBorda,
                30f
            )

            clipChildren = false
            clipToPadding = false

            isClickable = true
        }

        posicionar(
            container,
            moldura,
            0, 42, 530, 260
        )

        // NUVEM

        val nuvem = criarNuvemPopup()

        posicionar(
            container,
            nuvem,
            155, 8, 220, 60
        )

        // PAINEL CREME

        val painel = FrameLayout(this).apply {

            background = fundoArredondado(
                creme,
                24f
            )

            clipChildren = false
            clipToPadding = false

            isClickable = true
        }

        posicionar(
            container,
            painel,
            8, 50, 514, 244
        )

        // ESTRELA SUPERIOR

        val estrela = criarForma(
            "estrela",
            amarelo
        )

        posicionar(
            container,
            estrela,
            237, 0, 56, 56
        )

        // BOLINHAS DECORATIVAS

        val bolinha1 = criarCirculo(rosa)

        posicionar(
            container,
            bolinha1,
            210, 30, 7, 7
        )

        val bolinha2 = criarCirculo(rosa)

        posicionar(
            container,
            bolinha2,
            313, 30, 7, 7
        )

        // TÍTULO

        val titulo = criarTexto(
            "Que escolha gostosa!",
            25f,
            azulTexto
        )

        posicionar(
            painel,
            titulo,
            65, 12, 384, 40
        )

        // NOVOS TRACINHOS ROSAS

        val detalheEsquerdo =
            criarTracinhos(rosa)

        detalheEsquerdo.rotation = -65f



        // CÍRCULO DA BEBIDA

        val circulo = criarCirculo(
            bebida.cor
        )

        posicionar(
            painel,
            circulo,
            49, 62, 132, 132
        )

        // BEBIDA ESCOLHIDA

        val imagem = adicionarImagem(
            painel,
            bebida.imagem,
            65, 58, 100, 137
        )

        // CORAÇÕES

        val coracao1 = criarForma(
            "coracao",
            rosa
        )

        coracao1.rotation = -18f

        posicionar(
            painel,
            coracao1,
            25, 79, 30, 30
        )

        val coracao2 = criarForma(
            "coracao",
            rosa
        )

        coracao2.rotation = 15f

        posicionar(
            painel,
            coracao2,
            172, 146, 27, 27
        )

        val estrelaPequena = criarForma(
            "estrela",
            amarelo
        )

        posicionar(
            painel,
            estrelaPequena,
            177, 67, 24, 24
        )

        // TEXTO DA ESCOLHA

        val mensagem = criarTexto(
            "Sua bebida favorita é...",
            18f,
            "#637B96"
        )

        posicionar(
            painel,
            mensagem,
            218, 80, 270, 30
        )

        // NOME DA BEBIDA

        val nome = criarTexto(
            bebida.nome,
            26f,
            azulTexto
        )

        nome.background = fundoArredondado(
            bebida.cor,
            22f
        )

        posicionar(
            painel,
            nome,
            220, 120, 266, 54
        )

        // FRASE ADICIONAL REMOVIDA

        // BOTÃO FECHAR

        val fechar = criarTexto(
            "×",
            25f,
            "#6086AC"
        )

        fechar.background = fundoArredondado(
            "#DFEDF7",
            30f
        )

        fechar.contentDescription =
            "Fechar resultado"

        posicionar(
            container,
            fechar,
            488, 34, 35, 35
        )

        animacaoToque(fechar) {

            fecharPopup()
        }

        // BOTÃO VOLTAR

        val botaoVoltar = criarBotaoPopup(
            painel,
            "← Voltar",
            28, 203, 216, 34,
            "#D3E8F5",
            "#395681"
        ) {

            voltarParaCincoOpcoes()
        }

        // BOTÃO CONTINUAR

        val botaoContinuar = criarBotaoPopup(
            painel,
            "Continuar →",
            270, 203, 216, 34,
            "#D9EBC8",
            "#54774B"
        ) {

            abrirProximoNivel()
        }

        // ENTRADA SUAVE

        container.alpha = 0f

        container.scaleX = 0.90f
        container.scaleY = 0.90f

        container.translationY =
            px(10).toFloat()

        container.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(320L)
            .setInterpolator(
                DecelerateInterpolator()
            )
            .start()

        // ANIMAÇÕES

        balancar(
            estrela,
            5f,
            2000L,
            animacoesPopup
        )

        flutuar(
            imagem,
            3f,
            2000L,
            animacoesPopup
        )

        flutuar(
            coracao1,
            3f,
            2400L,
            animacoesPopup
        )

        flutuar(
            coracao2,
            3f,
            2600L,
            animacoesPopup,
            600L
        )

        balancar(
            estrelaPequena,
            7f,
            2300L,
            animacoesPopup
        )

        flutuar(
            botaoVoltar,
            1.5f,
            2400L,
            animacoesPopup
        )

        flutuar(
            botaoContinuar,
            1.5f,
            2400L,
            animacoesPopup,
            1200L
        )

        sobreposicao.postDelayed({

            if (
                popupAtual === sobreposicao &&
                !navegando &&
                !isFinishing &&
                hasWindowFocus()
            ) {

                tocarSomPopup()
            }

        }, 220L)
    }

    // ==========================================
    // NUVEM DO POP-UP
    // ==========================================

    private fun criarNuvemPopup(): View {

        return object : View(this) {

            private val paint = Paint(
                Paint.ANTI_ALIAS_FLAG
            )

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                canvas.save()

                canvas.scale(
                    width / 220f,
                    height / 60f
                )

                val caminho = Path().apply {

                    moveTo(0f, 60f)

                    lineTo(0f, 43f)

                    cubicTo(
                        0f, 28f,
                        14f, 23f,
                        30f, 25f
                    )

                    cubicTo(
                        36f, 7f,
                        59f, 4f,
                        76f, 19f
                    )

                    cubicTo(
                        87f, -3f,
                        103f, 0f,
                        110f, 10f
                    )

                    cubicTo(
                        126f, -3f,
                        142f, 3f,
                        148f, 19f
                    )

                    cubicTo(
                        167f, 4f,
                        184f, 9f,
                        190f, 25f
                    )

                    cubicTo(
                        207f, 22f,
                        220f, 30f,
                        220f, 43f
                    )

                    lineTo(220f, 60f)

                    close()
                }

                paint.style = Paint.Style.FILL

                paint.color =
                    Color.parseColor(azulBorda)

                canvas.drawPath(
                    caminho,
                    paint
                )

                canvas.restore()
            }
        }
    }

    // ==========================================
    // ESTRELAS E CORAÇÕES
    // ==========================================

    private fun criarForma(
        tipo: String,
        cor: String
    ): View {

        return object : View(this) {

            private val paint = Paint(
                Paint.ANTI_ALIAS_FLAG
            )

            override fun onDraw(canvas: Canvas) {

                super.onDraw(canvas)

                canvas.save()

                canvas.scale(
                    width / 100f,
                    height / 100f
                )

                val caminho = Path()

                when (tipo) {

                    "estrela" -> {

                        caminho.apply {

                            moveTo(44f, 12f)

                            quadTo(
                                50f, 0f,
                                56f, 12f
                            )

                            lineTo(66f, 33f)
                            lineTo(89f, 36f)

                            quadTo(
                                100f, 38f,
                                92f, 47f
                            )

                            lineTo(75f, 64f)
                            lineTo(79f, 87f)

                            quadTo(
                                81f, 99f,
                                70f, 93f
                            )

                            lineTo(50f, 82f)
                            lineTo(30f, 93f)

                            quadTo(
                                19f, 99f,
                                21f, 87f
                            )

                            lineTo(25f, 64f)
                            lineTo(8f, 47f)

                            quadTo(
                                0f, 38f,
                                11f, 36f
                            )

                            lineTo(34f, 33f)

                            close()
                        }
                    }

                    "coracao" -> {

                        caminho.apply {

                            moveTo(50f, 89f)

                            cubicTo(
                                35f, 78f,
                                5f, 55f,
                                5f, 31f
                            )

                            cubicTo(
                                5f, 6f,
                                36f, 1f,
                                50f, 25f
                            )

                            cubicTo(
                                64f, 1f,
                                95f, 6f,
                                95f, 31f
                            )

                            cubicTo(
                                95f, 55f,
                                65f, 78f,
                                50f, 89f
                            )

                            close()
                        }
                    }
                }

                paint.style = Paint.Style.FILL

                paint.color =
                    Color.parseColor(cor)

                canvas.drawPath(
                    caminho,
                    paint
                )

                canvas.restore()
            }
        }
    }

    // ==========================================
    // CÍRCULOS
    // ==========================================

    private fun criarCirculo(
        cor: String
    ): View {

        return View(this).apply {

            background = GradientDrawable().apply {

                shape = GradientDrawable.OVAL

                setColor(
                    Color.parseColor(cor)
                )
            }
        }
    }

    // ==========================================
    // BOTÕES DO POP-UP
    // ==========================================

    private fun criarBotaoPopup(
        pai: FrameLayout,
        texto: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        cor: String,
        corTexto: String,
        acao: () -> Unit
    ): View {

        val grupo = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        posicionar(
            pai,
            grupo,
            x, y, largura, altura
        )

        val botao = criarTexto(
            texto,
            17f,
            corTexto
        )

        botao.background = fundoArredondado(
            cor,
            18f
        )

        botao.contentDescription = texto

        posicionar(
            grupo,
            botao,
            0, 0, largura, altura
        )

        animacaoToque(botao) {

            if (popupAtual != null) {

                acao()
            }
        }

        return grupo
    }

    // ==========================================
    // ANIMAÇÃO AO PRESSIONAR
    // ==========================================

    private fun animacaoToque(
        view: View,
        acao: () -> Unit
    ) {

        view.isClickable = true
        view.isFocusable = true

        view.setOnTouchListener { v, evento ->

            when (evento.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    v.animate().cancel()

                    v.animate()
                        .scaleX(0.94f)
                        .scaleY(0.94f)
                        .setDuration(110L)
                        .start()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    v.animate().cancel()

                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(180L)
                        .setInterpolator(
                            DecelerateInterpolator()
                        )
                        .start()
                }
            }

            false
        }

        view.isSoundEffectsEnabled = false

        view.setOnClickListener {

            if (
                !navegando &&
                !isFinishing &&
                !isDestroyed
            ) {

                tocarSomBolhinha()

                acao()
            }
        }
    }

    // ==========================================
    // FLUTUAÇÃO
    // ==========================================

    private fun flutuar(
        view: View,
        distancia: Float,
        duracao: Long,
        lista: MutableList<ObjectAnimator>,
        atraso: Long = 0L
    ) {

        val animacao = ObjectAnimator.ofFloat(
            view,
            View.TRANSLATION_Y,
            0f,
            -distancia * escala
        )

        animacao.duration = duracao
        animacao.startDelay = atraso

        animacao.repeatCount =
            ValueAnimator.INFINITE

        animacao.repeatMode =
            ValueAnimator.REVERSE

        animacao.interpolator =
            AccelerateDecelerateInterpolator()

        lista.add(animacao)

        animacao.start()
    }

    // ==========================================
    // PULSAÇÃO
    // ==========================================

    private fun pulsar(
        view: View,
        lista: MutableList<ObjectAnimator>,
        atraso: Long = 0L,
        intensidade: Float = 1.04f
    ) {

        val animacao =
            ObjectAnimator.ofPropertyValuesHolder(

                view,

                PropertyValuesHolder.ofFloat(
                    View.SCALE_X,
                    1f,
                    intensidade
                ),

                PropertyValuesHolder.ofFloat(
                    View.SCALE_Y,
                    1f,
                    intensidade
                )
            )

        animacao.duration = 2600L

        animacao.startDelay = atraso

        animacao.repeatCount =
            ValueAnimator.INFINITE

        animacao.repeatMode =
            ValueAnimator.REVERSE

        animacao.interpolator =
            AccelerateDecelerateInterpolator()

        lista.add(animacao)

        animacao.start()
    }

    // ==========================================
    // BALANÇO
    // ==========================================

    private fun balancar(
        view: View,
        angulo: Float,
        duracao: Long,
        lista: MutableList<ObjectAnimator>,
        atraso: Long = 0L
    ) {

        val inicial = view.rotation

        val animacao = ObjectAnimator.ofFloat(
            view,
            View.ROTATION,
            inicial,
            inicial - angulo,
            inicial + angulo,
            inicial
        )

        animacao.duration = duracao

        animacao.startDelay = atraso

        animacao.repeatCount =
            ValueAnimator.INFINITE

        animacao.repeatMode =
            ValueAnimator.RESTART

        animacao.interpolator =
            AccelerateDecelerateInterpolator()

        lista.add(animacao)

        animacao.start()
    }

    // ==========================================
    // NOVO SOM DE BOLHINHA
    // ==========================================

    private fun gerarSomBolhinha(): ShortArray {

        val duracao = 0.16

        val quantidade =
            (taxaAudio * duracao).toInt()

        val dados = ShortArray(quantidade)

        var fase = 0.0

        for (i in dados.indices) {

            val t = i.toDouble() / taxaAudio

            val progresso = t / duracao

            val frequencia =
                480.0 + 260.0 * progresso

            fase +=
                2.0 * PI * frequencia / taxaAudio

            val entrada =
                minOf(1.0, t / 0.012)

            val envelope =
                entrada * exp(-progresso * 5.0)

            val onda =
                sin(fase) +
                        0.08 * sin(fase * 2.0)

            dados[i] = (
                    onda * envelope * 16000
                    ).toInt()
                .coerceIn(-32768, 32767)
                .toShort()
        }

        return dados
    }

    // ==========================================
    // SOM SUAVE DE ABERTURA DO POP-UP
    // ==========================================

    private fun gerarSomPopup(): ShortArray {

        val dados = DoubleArray(
            (taxaAudio * 0.65).toInt()
        )

        adicionarNota(
            dados,
            523.25,
            0.0,
            0.25,
            0.45
        )

        adicionarNota(
            dados,
            659.25,
            0.12,
            0.30,
            0.35
        )

        adicionarNota(
            dados,
            783.99,
            0.24,
            0.38,
            0.30
        )

        return converterAudio(dados)
    }

    // ==========================================
    // MÚSICA AMBIENTE INFANTIL
    // ==========================================

    private fun gerarMusicaAmbiente(): ShortArray {

        val duracao = 16.0

        val dados = DoubleArray(
            (taxaAudio * duracao).toInt()
        )

        // MELODIA ORIGINAL SUAVE

        val notas = doubleArrayOf(
            523.25,
            659.25,
            587.33,
            523.25,
            440.00,
            523.25,
            659.25,
            587.33,
            493.88,
            587.33,
            659.25,
            523.25,
            440.00,
            493.88,
            523.25
        )

        notas.forEachIndexed { indice, nota ->

            adicionarNota(
                dados,
                nota,
                indice * 1.0,
                0.85,
                0.32
            )
        }

        // ACOMPANHAMENTO DELICADO

        val acompanhamento = doubleArrayOf(
            261.63,
            220.00,
            196.00,
            261.63
        )

        acompanhamento.forEachIndexed { indice, nota ->

            adicionarNota(
                dados,
                nota,
                indice * 4.0,
                3.2,
                0.16
            )

            adicionarNota(
                dados,
                nota * 1.5,
                indice * 4.0 + 1.0,
                2.5,
                0.08
            )
        }

        return converterAudio(dados)
    }

    // ==========================================
    // GERAR NOTAS COM TIMBRE SUAVE
    // ==========================================

    private fun adicionarNota(
        dados: DoubleArray,
        frequencia: Double,
        inicio: Double,
        duracao: Double,
        intensidade: Double
    ) {

        val inicioAmostra =
            (inicio * taxaAudio).toInt()

        val quantidade =
            (duracao * taxaAudio).toInt()

        for (i in 0 until quantidade) {

            val indice = inicioAmostra + i

            if (indice !in dados.indices) break

            val t = i.toDouble() / taxaAudio

            val entrada =
                minOf(1.0, t / 0.035)

            val saida =
                minOf(
                    1.0,
                    (duracao - t) / 0.12
                ).coerceAtLeast(0.0)

            val envelope =
                entrada *
                        saida *
                        exp(-t * 2.5)

            val onda =
                sin(2.0 * PI * frequencia * t) +
                        0.12 *
                        sin(4.0 * PI * frequencia * t)

            dados[indice] +=
                onda * envelope * intensidade
        }
    }

    private fun converterAudio(
        dados: DoubleArray
    ): ShortArray {

        return ShortArray(dados.size) { indice ->

            (
                    dados[indice] * 24000
                    ).toInt()
                .coerceIn(-32768, 32767)
                .toShort()
        }
    }

    // ==========================================
    // CRIAR REPRODUTOR DE ÁUDIO
    // ==========================================

    private fun criarAudio(
        dados: ShortArray,
        volume: Float,
        repetir: Boolean
    ): AudioTrack? {

        var audio: AudioTrack? = null

        return try {

            val atributos =
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_GAME
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_MUSIC
                    )
                    .build()

            val formato =
                AudioFormat.Builder()
                    .setSampleRate(taxaAudio)
                    .setEncoding(
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    .setChannelMask(
                        AudioFormat.CHANNEL_OUT_MONO
                    )
                    .build()

            audio = AudioTrack.Builder()
                .setAudioAttributes(atributos)
                .setAudioFormat(formato)
                .setBufferSizeInBytes(
                    dados.size * 2
                )
                .setTransferMode(
                    AudioTrack.MODE_STATIC
                )
                .build()

            if (
                audio.write(
                    dados,
                    0,
                    dados.size
                ) != dados.size
            ) {

                audio.release()

                return null
            }

            audio.setVolume(volume)

            if (repetir) {

                audio.setLoopPoints(
                    0,
                    dados.size,
                    -1
                )
            }

            audio

        } catch (_: Exception) {

            audio?.release()

            null
        }
    }

    // ==========================================
    // REPRODUZIR MÚSICA
    // ==========================================

    private fun iniciarMusicaAmbiente() {

        try {

            if (musicaAmbiente == null) {

                musicaAmbiente = criarAudio(
                    gerarMusicaAmbiente(),
                    volumeMusica,
                    true
                )
            }

            musicaAmbiente?.play()

        } catch (_: Exception) {
        }
    }

    // ==========================================
    // REPRODUZIR BOLHINHA
    // ==========================================

    private fun tocarSomBolhinha() {

        try {

            liberarAudio(somToque)

            somToque = criarAudio(
                gerarSomBolhinha(),
                volumeToque,
                false
            )

            somToque?.play()

        } catch (_: Exception) {
        }
    }

    // ==========================================
    // REPRODUZIR SOM DO POP-UP
    // ==========================================

    private fun tocarSomPopup() {

        try {

            liberarAudio(somPopup)

            somPopup = criarAudio(
                gerarSomPopup(),
                volumePopup,
                false
            )

            somPopup?.play()

        } catch (_: Exception) {
        }
    }

    private fun liberarAudio(
        audio: AudioTrack?
    ) {

        try {

            audio?.release()

        } catch (_: Exception) {
        }
    }

    // ==========================================
    // FECHAR POP-UP
    // ==========================================

    private fun fecharPopup() {

        val popup = popupAtual ?: return

        popupAtual = null

        cancelarAnimacoes(animacoesPopup)

        popup.animate()
            .alpha(0f)
            .setDuration(180L)
            .withEndAction {

                tela.removeView(popup)

                popupAberto = false

                if (!navegando && hasWindowFocus()) {

                    animacoesTela.forEach {

                        if (it.isPaused) {

                            it.resume()
                        }
                    }
                }
            }
            .start()
    }

    private fun cancelarAnimacoes(
        lista: MutableList<ObjectAnimator>
    ) {

        lista.forEach {

            it.cancel()
        }

        lista.clear()
    }

    // ==========================================
    // VOLTAR PARA OS CINCO JOGOS
    // ==========================================

    private fun voltarParaCincoOpcoes() {

        if (navegando || isFinishing) return

        navegando = true

        cancelarAnimacoes(animacoesTela)
        cancelarAnimacoes(animacoesPopup)

        val intent = Intent(
            this,
            CincoOpcoesDosJogosActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

        startActivity(intent)

        finish()

        aplicarTransicao()
    }

    // ==========================================
    // PRÓXIMO NÍVEL
    // ==========================================

    private fun abrirProximoNivel() {

        if (navegando || isFinishing) return

        navegando = true

        try {

            val classe = Class.forName(
                "$packageName.ProveNaImaginacao2Activity"
            )

            startActivity(
                Intent(this, classe)
            )

            cancelarAnimacoes(animacoesTela)
            cancelarAnimacoes(animacoesPopup)

            finish()

            aplicarTransicao()

        } catch (_: Exception) {

            navegando = false

            Toast.makeText(
                this,
                "Próximo nível indisponível.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    @Suppress("DEPRECATION")
    private fun aplicarTransicao() {

        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
    }

    // ==========================================
    // CRIAR TEXTOS
    // ==========================================

    private fun criarTexto(
        conteudo: String,
        tamanho: Float,
        cor: String
    ): TextView {

        return TextView(this).apply {

            text = conteudo

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor(cor)
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                tamanho * escala
            )

            typeface = fonte

            includeFontPadding = false

            isClickable = false
            isFocusable = false
        }
    }

    // ==========================================
    // FUNDOS ARREDONDADOS
    // ==========================================

    private fun fundoArredondado(
        cor: String,
        raio: Float,
        borda: String? = null,
        espessura: Float = 0f
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(
                Color.parseColor(cor)
            )

            cornerRadius = raio * escala

            if (
                borda != null &&
                espessura > 0
            ) {

                setStroke(
                    (espessura * escala)
                        .toInt()
                        .coerceAtLeast(1),

                    Color.parseColor(borda)
                )
            }
        }
    }

    private fun fundoTracejado(
        cor: String,
        raio: Float
    ): GradientDrawable {

        return GradientDrawable().apply {

            setColor(Color.TRANSPARENT)

            cornerRadius = raio * escala

            setStroke(
                (1.3f * escala)
                    .toInt()
                    .coerceAtLeast(1),

                Color.parseColor(cor),

                6f * escala,

                4f * escala
            )
        }
    }

    // ==========================================
    // ADICIONAR IMAGENS
    // ==========================================

    private fun adicionarImagem(
        pai: FrameLayout,
        nome: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int
    ): ImageView {

        val imagem = ImageView(this)

        val recurso = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (recurso != 0) {

            imagem.setImageResource(recurso)

        } else {

            Toast.makeText(
                this,
                "Imagem não encontrada: $nome",
                Toast.LENGTH_SHORT
            ).show()
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        imagem.isClickable = false
        imagem.isFocusable = false

        posicionar(
            pai,
            imagem,
            x, y, largura, altura
        )

        return imagem
    }

    // ==========================================
    // POSICIONAMENTO
    // ==========================================

    private fun px(valor: Int): Int {

        return (valor * escala).toInt()
    }

    private fun posicionar(
        pai: FrameLayout,
        view: View,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int
    ) {

        val parametros = FrameLayout.LayoutParams(
            px(largura).coerceAtLeast(1),
            px(altura).coerceAtLeast(1)
        )

        parametros.leftMargin = px(x)

        parametros.topMargin = px(y)

        pai.addView(
            view,
            parametros
        )
    }

    // ==========================================
    // CICLO DE VIDA
    // ==========================================

    override fun onResume() {

        super.onResume()

        iniciarMusicaAmbiente()

        val lista = if (popupAberto) {

            animacoesPopup

        } else {

            animacoesTela
        }

        lista.forEach {

            if (it.isPaused) {

                it.resume()
            }
        }
    }

    override fun onPause() {

        try {

            musicaAmbiente?.pause()

        } catch (_: Exception) {
        }

        liberarAudio(somToque)
        liberarAudio(somPopup)

        somToque = null
        somPopup = null

        animacoesTela.forEach {

            if (it.isStarted && !it.isPaused) {

                it.pause()
            }
        }

        animacoesPopup.forEach {

            if (it.isStarted && !it.isPaused) {

                it.pause()
            }
        }

        super.onPause()
    }

    override fun onDestroy() {

        cancelarAnimacoes(animacoesTela)
        cancelarAnimacoes(animacoesPopup)

        liberarAudio(musicaAmbiente)
        liberarAudio(somToque)
        liberarAudio(somPopup)

        musicaAmbiente = null
        somToque = null
        somPopup = null

        super.onDestroy()
    }
}