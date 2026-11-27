package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import java.util.Locale
import kotlin.math.min
import kotlin.random.Random

/**
 * LÚDICA KIDS — BUBBA E AS NUVENS DAS EMOÇÕES
 * 3 níveis com 2 rodadas cada (6 rodadas). Landscape, sem XML.
 *
 * COMO A CENA FUNCIONA:
 * Existe um "palco" de 874 x 393 que é escalado de forma UNIFORME (sem esticar)
 * e centralizado na tela. O que sobra nas laterais/topo é preenchido pelo céu
 * (degradê azul claro) e por uma faixa de grama embaixo.
 *
 * RECURSOS EM res/drawable:
 * parquinho, girafafeliz, girafatriste, girafabrava, girafaassustada,
 * girafaduvida, girafauau, nuvemfeliz, nuvemtriste, nuvembrava, nuvemmedo,
 * nuvemincerta, nuvemuau, coelho, capivara, bola, botaoverde.
 * SOM EM res/raw: clique_suave. Opcional: brilho_suave, sominicio, som_erro. Ambiente: fundoemocoes.
 */
class IdentificandoEmocoesActivity : Activity() {

    // =====================================================
    // 1. POSIÇÕES E TAMANHOS — EDITE AQUI (coordenadas do palco 874 x 393)
    // Formato: x, y, largura, altura
    // =====================================================
    private val BASE_LARGURA = 874f
    private val BASE_ALTURA = 393f

    // PARQUINHO ATUALIZADO: imagem 874 x 402, transparente no céu.
    // Ela cobre a largura inteira e encosta no limite inferior do palco,
    // sem ser deformada ou ampliada além das dimensões originais.
    private val PARQUINHO_LARGURA = 874f
    private val PARQUINHO_ALTURA = 402f

    // POSIÇÕES DOS PERSONAGENS: x, y, largura, altura.
    // Bubba de um lado (direita); os amigos juntinhos do outro (esquerda).
    // Ajuste somente estes números caso queira mover ou redimensionar.
    private val posBubba = intArrayOf(671, 184, 170, 202)
    private val posCoelho = intArrayOf(94, 290, 90, 96)
    private val posCapivara = intArrayOf(178, 278, 126, 105)
    private val posBola = intArrayOf(315, 339, 46, 46)

    // Cinco nuvens: três originais + duas novas no céu à esquerda,
    // acima da área do escorregador e do balanço.
    // Coordenadas do palco 874 x 393; apenas as nuvens foram reposicionadas.
    private val nuvensX = intArrayOf(393, 598, 496, 55, 220)
    private val nuvensY = intArrayOf(82, 82, 174, 116, 96)
    private val nuvemLargura = 150
    private val nuvemAltura = 94

    // =====================================================
    // 2. CORES
    // =====================================================
    private val roxo = Color.rgb(120, 68, 156)
    private val roxoClaro = Color.rgb(240, 224, 255)
    private val azul = Color.rgb(51, 145, 232)
    private val azulEscuro = Color.rgb(36, 79, 137)
    private val rosa = Color.rgb(255, 142, 188)
    private val amarelo = Color.rgb(255, 220, 124)
    private val verdeSuave = Color.rgb(214, 244, 207)
    private val verdeBotao = Color.rgb(67, 176, 89)
    private val verdeBotaoBorda = Color.rgb(44, 135, 64)

    private lateinit var tela: FrameLayout
    private var escala = 1f
    private var ultimaLargura = 0
    private var ultimaAltura = 0
    private val animacoes = mutableListOf<ObjectAnimator>()
    private val sons = mutableListOf<MediaPlayer>()

    private var ambiente: MediaPlayer? = null
    private var narrador: TextToSpeech? = null
    private var vozDisponivel = false
    private var rodadaAtual = 0
    private var bloqueado = false
    private var avisoErro: View? = null
    private var overlay: FrameLayout? = null
    private var versaoDaRodada = 0
    private var ultimaRodadaNarrada = -1
    // Somente as nuvens são controladas pela narração.
    private val nuvensDaRodada = mutableListOf<ImageView>()
    private var nuvensJaDesceram = false
    private var narracaoEmAndamentoId: String? = null
    private var sequenciaNarracao = 0

    private enum class Emocao(val titulo: String, val nuvem: Int) {
        FELIZ("Feliz", R.drawable.nuvemfeliz),
        TRISTE("Triste", R.drawable.nuvemtriste),
        BRAVA("Brava", R.drawable.nuvembrava),
        MEDO("Medo", R.drawable.nuvemmedo),
        DUVIDA("Dúvida", R.drawable.nuvemincerta),
        SURPRESA("Surpresa", R.drawable.nuvemuau)
    }

    private data class Rodada(
        val historia: String,
        val emocao: Emocao,
        val girafa: Int,
        val opcoes: List<Emocao>,
        val decoracao: String,
        val explicacao: String
    )

    private val rodadas = listOf(
        Rodada(
            "Bubba desceu no escorregador e brincou com seus amigos!",
            Emocao.FELIZ, R.drawable.girafafeliz,
            listOf(Emocao.FELIZ, Emocao.TRISTE, Emocao.BRAVA),
            "🎉", "Bubba ficou feliz porque se divertiu com seus amigos!"
        ),
        Rodada(
            "A bola favorita da Bubba furou durante a brincadeira.",
            Emocao.TRISTE, R.drawable.girafatriste2,
            listOf(Emocao.SURPRESA, Emocao.TRISTE, Emocao.FELIZ),
            "", "Bubba ficou triste porque sua bola favorita estragou."
        ),
        Rodada(
            "O coelhinho pegou a bola da Bubba sem pedir.",
            Emocao.BRAVA, R.drawable.girafabrava,
            listOf(Emocao.TRISTE, Emocao.BRAVA, Emocao.DUVIDA),
            "💢", "Bubba ficou brava porque não gostou do que aconteceu."
        ),
        Rodada(
            "Bubba ouviu um barulho muito alto e inesperado!",
            Emocao.MEDO, R.drawable.girafaassustada,
            listOf(Emocao.MEDO, Emocao.SURPRESA, Emocao.FELIZ),
            "❗", "Bubba sentiu medo com o barulho inesperado."
        ),
        Rodada(
            "Seus amigos inventaram um jogo, mas Bubba não entendeu as regras.",
            Emocao.DUVIDA, R.drawable.girafaduvida,
            listOf(Emocao.BRAVA, Emocao.FELIZ, Emocao.DUVIDA),
            "❔", "Bubba ficou em dúvida porque não entendeu a brincadeira."
        ),
        Rodada(
            "Os amigos prepararam uma surpresa especial para a Bubba!",
            Emocao.SURPRESA, R.drawable.girafauau,
            listOf(Emocao.TRISTE, Emocao.SURPRESA, Emocao.MEDO),
            "🎁", "Bubba ficou surpresa com o carinho dos amigos!"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) { voltarParaJogos() }
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                )

        tela = FrameLayout(this).apply {
            clipChildren = false
            clipToPadding = false
        }
        setContentView(tela)
        configurarNarrador()
        iniciarAmbienteOpcional()

        tela.addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            val largura = right - left
            val altura = bottom - top
            if (largura > 0 && altura > 0 &&
                (largura != ultimaLargura || altura != ultimaAltura)
            ) {
                ultimaLargura = largura
                ultimaAltura = altura
                atualizarOrientacaoELayout(largura, altura)
            }
        }
    }

    private fun atualizarOrientacaoELayout(largura: Int, altura: Int) {
        if (isFinishing || isDestroyed) return

        if (largura <= altura) {
            animacoes.forEach { it.cancel() }
            animacoes.clear()
            tela.removeAllViews()
            tela.setBackgroundColor(Color.rgb(184, 230, 255))
            val aviso = TextView(this).apply {
                text = "Gire o celular para jogar! 🦒"
                textSize = 20f
                gravity = Gravity.CENTER
                setTextColor(roxo)
                typeface = Typeface.DEFAULT_BOLD
            }
            tela.addView(aviso, FrameLayout.LayoutParams(-1, -1))
            return
        }

        // Escala UNIFORME: nada é esticado, o palco só cresce/encolhe.
        escala = min(largura / BASE_LARGURA, altura / BASE_ALTURA)
        montarRodada()
    }

    // =====================================================
    // 3. FUNÇÕES PARA DESENHAR A TELA
    // =====================================================
    private fun px(valor: Int) = (valor * escala).toInt()
    private fun pxF(valor: Float) = (valor * escala).toInt()

    private fun pos(x: Int, y: Int, largura: Int, altura: Int) =
        FrameLayout.LayoutParams(px(largura), px(altura)).apply {
            leftMargin = px(x)
            topMargin = px(y)
        }

    private fun FrameLayout.add(v: View, x: Int, y: Int, w: Int, h: Int): View {
        addView(v, pos(x, y, w, h))
        return v
    }

    private fun forma(cor: Int, raio: Int, borda: Int? = null): GradientDrawable =
        GradientDrawable().apply {
            setColor(cor)
            cornerRadius = px(raio).toFloat()
            if (borda != null) setStroke(px(3).coerceAtLeast(1), borda)
        }

    private fun texto(
        conteudo: String,
        tamanho: Float,
        cor: Int = roxo,
        negrito: Boolean = true
    ) = TextView(this).apply {
        text = conteudo
        gravity = Gravity.CENTER
        setTextColor(cor)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, tamanho * escala.coerceAtLeast(0.65f))
        typeface = Typeface.create(
            "sans-serif-rounded",
            if (negrito) Typeface.BOLD else Typeface.NORMAL
        )
        includeFontPadding = false
    }

    private fun imagem(id: Int, descricao: String = ""): ImageView =
        ImageView(this).apply {
            setImageResource(id)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = descricao
        }

    private fun animar(
        view: View, propriedade: android.util.Property<View, Float>,
        valores: FloatArray, duracao: Long, linear: Boolean = false
    ) {
        val a = ObjectAnimator.ofFloat(view, propriedade, *valores).apply {
            duration = duracao
            repeatCount = ValueAnimator.INFINITE
            interpolator = if (linear) LinearInterpolator() else AccelerateDecelerateInterpolator()
        }
        animacoes.add(a)
        a.start()
    }

    private fun flutuar(view: View, distancia: Int, duracao: Long) =
        animar(view, View.TRANSLATION_Y, floatArrayOf(0f, -px(distancia).toFloat(), 0f), duracao)

    private fun pulsar(view: View, duracao: Long = 1150L) {
        animar(view, View.SCALE_X, floatArrayOf(1f, 1.055f, 1f), duracao)
        animar(view, View.SCALE_Y, floatArrayOf(1f, 1.055f, 1f), duracao)
    }

    private fun montarRodada() {
        versaoDaRodada++
        bloqueado = false
        overlay = null
        avisoErro = null
        nuvensDaRodada.clear()
        nuvensJaDesceram = false
        narracaoEmAndamentoId = null
        animacoes.forEach { it.cancel() }
        animacoes.clear()
        tela.removeAllViews()

        val rodada = rodadas[rodadaAtual]
        val nivel = rodadaAtual / 2 + 1
        val numeroRodada = rodadaAtual % 2 + 1

        // ---------- CÉU: degradê azul claro em TODA a tela ----------
        tela.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.rgb(150, 214, 255),
                Color.rgb(194, 234, 255),
                Color.rgb(238, 250, 255)
            )
        )

        // ---------- GRAMA: faixa que cobre as laterais embaixo ----------
        val topoPalco = (ultimaAltura - pxF(BASE_ALTURA)) / 2
        val alturaGrama = (ultimaAltura - (topoPalco + px(358))).coerceAtLeast(px(20))
        tela.addView(
            View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(Color.rgb(152, 216, 112), Color.rgb(112, 186, 86))
                )
            },
            FrameLayout.LayoutParams(-1, alturaGrama, Gravity.BOTTOM)
        )

        // ---------- PALCO (proporção fixa, centralizado) ----------
        val palco = FrameLayout(this).apply {
            clipChildren = false
            clipToPadding = false
        }
        tela.addView(
            palco,
            FrameLayout.LayoutParams(pxF(BASE_LARGURA), pxF(BASE_ALTURA), Gravity.CENTER)
        )

        // Nuvenzinhas decorativas, só enfeite (não clicáveis).
        listOf(
            intArrayOf(40, 70, 110, 38, 9000),
            intArrayOf(720, 215, 130, 42, 11000)
        ).forEach { d ->
            val deco = View(this).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.argb(150, 255, 255, 255))
                }
            }
            palco.add(deco, d[0], d[1], d[2], d[3])
            animar(deco, View.TRANSLATION_X, floatArrayOf(0f, px(36).toFloat(), 0f), d[4].toLong())
        }

        // ---------- PARQUINHO NOVO: proporção original, encostado embaixo ----------
        // Seu PNG já contém somente a paisagem/grama, com céu transparente.
        // Usamos o tamanho original da arte (874 x 402); o excesso de 9 unidades
        // fica acima do palco e é transparente, sem deformar ou esticar a imagem.
        val parquinho = imagem(R.drawable.parquinho, "Parquinho da Bubba")
        palco.addView(
            parquinho,
            FrameLayout.LayoutParams(
                pxF(PARQUINHO_LARGURA),
                pxF(PARQUINHO_ALTURA)
            ).apply {
                leftMargin = pxF((BASE_LARGURA - PARQUINHO_LARGURA) / 2f)
                topMargin = pxF(BASE_ALTURA - PARQUINHO_ALTURA)
            }
        )

        // ---------- BRINQUEDOS AO FUNDO (atrás dos personagens) ----------
        // Arquivos: res/drawable/escorrega.png e res/drawable/balanco.png.
        // Escorregador deslocado mais à esquerda e balanço mais à direita.
        // Ambos elevados e desenhados antes dos bichinhos (atrás deles).
        // Para ajustar: x, y, largura, altura.
        val escorrega = imagem(R.drawable.escorrega, "Escorregador do parque")
        palco.add(escorrega, 29, 178, 112, 155)

        val balanco = imagem(R.drawable.balanco, "Balanço do parque")
        palco.add(balanco, 307, 172, 112, 150)

        // ---------- BUBBA À DIREITA E AMIGUINHOS AGRUPADOS À ESQUERDA ----------
        val bubba = imagem(rodada.girafa, "Bubba mostrando ${rodada.emocao.titulo}")
        palco.add(bubba, posBubba[0], posBubba[1], posBubba[2], posBubba[3])

        val coelho = imagem(R.drawable.coelho, "Coelhinho")
        palco.add(coelho, posCoelho[0], posCoelho[1], posCoelho[2], posCoelho[3])

        val capivara = imagem(R.drawable.capivara, "Capivara")
        palco.add(capivara, posCapivara[0], posCapivara[1], posCapivara[2], posCapivara[3])

        // Somente na rodada triste (índice 1), usamos a imagem da bola murcha.
        val bola = imagem(
            if (rodadaAtual == 1) R.drawable.bolamurcha else R.drawable.bola,
            if (rodadaAtual == 1) "Bola murcha" else "Bola de futebol"
        )
        when (rodadaAtual) {
            2 -> palco.add(bola, posCoelho[0] + 75, posBola[1], posBola[2], posBola[3]) // bola próxima ao coelho
            1 -> palco.add(bola, posBola[0] - 5, posBola[1] - 5, posBola[2] + 10, posBola[3] + 10) // somente bola murcha um pouco maior
            else -> palco.add(bola, posBola[0], posBola[1], posBola[2], posBola[3])
        }

        // Balãozinho da situação: não aparece na rodada triste,
        // evitando o coração quebrado e um círculo vazio.
        if (rodada.decoracao.isNotBlank()) {
            val figurinha = texto(rodada.decoracao, 26f).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.WHITE)
                    setStroke(px(3), roxoClaro)
                }
            }
            palco.add(figurinha, 792, 144, 48, 48)
        }

        // ---------- TOPO: voltar | história | nível ----------
        val voltar = imagem(R.drawable.botaoverde, "Voltar para a tela inicial dos jogos").apply {
            isClickable = true
            isFocusable = true
            setOnClickListener {
                if (bloqueado && overlay != null) return@setOnClickListener
                tocarClique()
                animate().scaleX(0.9f).scaleY(0.9f).setDuration(90)
                    .withEndAction { voltarParaJogos() }.start()
            }
        }
        palco.add(voltar, 14, 10, 57, 52)

        // ---------- PERGUNTA: faixa suave, arredondada e sem emojis ----------
        // Preservamos o lugar e o botão de som; só refinamos o visual.
        val placaPergunta = FrameLayout(this).apply {
            background = formaGradiente(
                Color.rgb(255, 254, 250), Color.rgb(238, 249, 249),
                32, Color.rgb(204, 232, 226), 2
            )
            elevation = px(3).toFloat()
        }
        palco.add(placaPergunta, 170, 12, 430, 54)

        // Pequenas bolinhas pastéis, discretas e sem ícones.
        val detalheEsquerdo = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(255, 221, 182))
            }
        }
        placaPergunta.add(detalheEsquerdo, 17, 22, 11, 11)

        val pergunta = texto("Como a Bubba está se sentindo?", 19f, azulEscuro)
            .ajustar(13f, 19f).apply {
                gravity = Gravity.CENTER
                setPadding(px(6), 0, px(6), 0)
            }
        placaPergunta.add(pergunta, 34, 5, 362, 44)

        val detalheDireito = View(this).apply {
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(198, 228, 217))
            }
        }
        placaPergunta.add(detalheDireito, 402, 22, 11, 11)

        // Toque para ouvir a história novamente.
        val ouvir = texto("🔊", 27f, azulEscuro).apply {
            background = formaGradiente(
                Color.WHITE, Color.rgb(222, 242, 255),
                19, Color.rgb(165, 215, 242), 2
            )
            contentDescription = "Ouvir a situação da Bubba"
            isClickable = true
            isFocusable = true
            setOnClickListener {
                tocarClique()
                animate().scaleX(0.90f).scaleY(0.90f).setDuration(90)
                    .withEndAction {
                        animate().scaleX(1f).scaleY(1f).setDuration(130).start()
                    }.start()
                narrar(rodada.historia + " Como a Bubba está se sentindo?")
            }
        }
        palco.add(ouvir, 613, 12, 55, 54)

        val identificacao = texto("Nível $nivel  •  $numeroRodada/2", 16f, roxo).apply {
            background = forma(Color.WHITE, 18, amarelo)
        }
        palco.add(identificacao, 714, 12, 146, 38)

        // ---------- NUVENS: três originais + duas novas no céu à esquerda ----------
        // Cada rodada continua tendo uma única resposta correta.
        // Mantemos as três opções originais e acrescentamos duas emoções diferentes.
        val opcoesVisiveis = rodada.opcoes +
                Emocao.values().filter { it !in rodada.opcoes }.take(2)
        opcoesVisiveis.forEachIndexed { indice, emocao ->
            val nuvem = imagem(emocao.nuvem, "Nuvem ${emocao.titulo}")
            palco.add(nuvem, nuvensX[indice], nuvensY[indice], nuvemLargura, nuvemAltura)
            nuvem.isClickable = true
            nuvem.isFocusable = true
            nuvem.setOnClickListener {
                if (bloqueado) return@setOnClickListener
                tocarClique()
                // A pulsação já anima SCALE_X/SCALE_Y. Não alterar a escala
                // no clique: duas animações simultâneas produziam o "glitch".
                verificarResposta(emocao, nuvem)
            }
            // Começa fora da tela, acima do céu, até a narração terminar.
            nuvem.translationY = -px(nuvensY[indice] + nuvemAltura + 35).toFloat()
            nuvem.alpha = 0f
            nuvem.isClickable = false
            nuvem.isFocusable = false
            nuvensDaRodada.add(nuvem)
        }

        // Aviso de dica: carinha da Bubba + texto, logo abaixo das nuvens.
        val aviso = FrameLayout(this).apply {
            background = formaGradiente(Color.WHITE, Color.rgb(255, 244, 232), 24, Color.rgb(255, 190, 150), 3)
            elevation = px(5).toFloat()
            visibility = View.GONE
        }
        aviso.add(imagem(rodada.girafa, "Rostinho da Bubba"), 8, 3, 40, 40)
        aviso.add(
            texto("Olhe o rostinho da Bubba e tente outra nuvem! 💛", 14f, Color.rgb(150, 85, 45))
                .ajustar(9f, 15f).apply { maxLines = 2 },
            54, 4, 316, 38
        )
        palco.add(aviso, 247, 285, 380, 46)
        avisoErro = aviso

        // Reproduz a situação apenas uma vez ao iniciar cada rodada.
        val versao = versaoDaRodada
        palco.postDelayed({
            if (!isFinishing && !isDestroyed && versaoDaRodada == versao && overlay == null) {
                narrarRodadaSePossivel()
            }
        }, 500L)
        palco.postDelayed({
            if (versao == versaoDaRodada && !vozDisponivel && !nuvensJaDesceram) {
                descerNuvens(versao)
            }
        }, 6000L)
    }

    private fun voltarParaJogos() {
        if (isFinishing || isDestroyed) return
        val destino = Intent(this, TelaInicialJogosGeral::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(destino)
        finish()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
            event.keyCode == KeyEvent.KEYCODE_BACK
        ) {
            if (event.action == KeyEvent.ACTION_UP && !event.isCanceled) {
                voltarParaJogos()
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    // =====================================================
    // 4. LÓGICA DAS RESPOSTAS
    // =====================================================
    private fun verificarResposta(escolhida: Emocao, nuvem: ImageView) {
        if (bloqueado) return
        val rodada = rodadas[rodadaAtual]
        if (escolhida == rodada.emocao) {
            bloqueado = true
            tocarAcerto()
            glitter(nuvem, 42)
            nuvem.postDelayed({
                if (!isFinishing && !isDestroyed) abrirPopupAcerto(rodada)
            }, 500L)
        } else {
            tocarErro()
            // Erro delicado sem deslocamentos: não interfere na flutuação
            // nem na pulsação da nuvem.
            val versaoDaResposta = versaoDaRodada
            nuvem.setColorFilter(Color.rgb(255, 208, 213), android.graphics.PorterDuff.Mode.MULTIPLY)
            nuvem.postDelayed({
                if (versaoDaResposta == versaoDaRodada) nuvem.clearColorFilter()
            }, 330L)
            val aviso = avisoErro ?: return
            val estaVersao = versaoDaRodada
            aviso.animate().cancel()
            aviso.alpha = 1f
            aviso.visibility = View.VISIBLE
            aviso.bringToFront() // dica visível para qualquer uma das 5 nuvens
            aviso.postDelayed({
                if (versaoDaRodada == estaVersao) {
                    aviso.animate().alpha(0f).setDuration(250)
                        .withEndAction { aviso.visibility = View.GONE }.start()
                }
            }, 1800L)
        }
    }

    // =====================================================
    // POP-UP DE ACERTO — paleta suave (creme, céu, pêssego, menta)
    // =====================================================
    private fun formaGradiente(
        cor1: Int, cor2: Int, raio: Int, borda: Int? = null, larguraBorda: Int = 3
    ) = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(cor1, cor2)).apply {
        cornerRadius = px(raio).toFloat()
        if (borda != null) setStroke(px(larguraBorda).coerceAtLeast(1), borda)
    }

    private fun TextView.sombra(cor: Int = Color.argb(150, 170, 85, 60)): TextView {
        setShadowLayer(px(3).toFloat(), 0f, px(2).toFloat(), cor)
        return this
    }

    // Faz o texto encolher sozinho para NUNCA ficar cortado.
    private fun TextView.ajustar(minimo: Float, maximo: Float): TextView {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val e = escala.coerceAtLeast(0.65f)
            setAutoSizeTextTypeUniformWithConfiguration(
                (minimo * e).toInt().coerceAtLeast(8),
                (maximo * e).toInt().coerceAtLeast(10),
                1, TypedValue.COMPLEX_UNIT_PX
            )
        }
        return this
    }

    private fun abrirPopupAcerto(rodada: Rodada) {
        if (overlay != null || isFinishing || isDestroyed) return
        val ultima = rodadaAtual == rodadas.lastIndex
        val fimDeNivel = rodadaAtual % 2 == 1
        val textoBotao = when {
            ultima -> "Jogar de novo ▶"
            !fimDeNivel -> "Próxima rodada ▶"
            else -> "Próximo nível ▶"
        }
        val titulo = if (ultima) "Parabéns!" else "Muito bem!"
        val cabecalho = if (ultima) "Você aprendeu:" else "Você descobriu!"
        val descricao = if (ultima) {
            "Você ajudou a Bubba a reconhecer todas as emoções!"
        } else {
            rodada.explicacao
        }

        // Cores suaves (nada de roxo).
        val creme = Color.rgb(255, 253, 246)
        val ceuClaro = Color.rgb(228, 246, 255)
        val bordaCeu = Color.rgb(160, 212, 244)
        val manteiga = Color.rgb(255, 232, 166)
        val marrom = Color.rgb(150, 85, 45)

        // Fundo com vinheta azul-acinzentada suave.
        val escurecimento = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                gradientType = GradientDrawable.RADIAL_GRADIENT
                gradientRadius = ultimaLargura * 0.75f
                colors = intArrayOf(Color.argb(110, 60, 110, 160), Color.argb(190, 25, 55, 95))
            }
            isClickable = true
        }
        tela.addView(escurecimento, FrameLayout.LayoutParams(-1, -1))
        overlay = escurecimento

        // O cartão é desenhado em 560x300 e reduzido automaticamente se a tela for pequena,
        // então NADA fica cortado (nem a fita do título, em cima, nem o botão).
        val fator = min(
            1f,
            min(
                (ultimaLargura - px(36)).toFloat() / px(560),
                (ultimaAltura - px(24)).toFloat() / px(300)
            )
        )

        val cartao = FrameLayout(this).apply {
            background = formaGradiente(creme, ceuClaro, 34, bordaCeu, 4)
            elevation = px(12).toFloat()
            clipChildren = false
            clipToPadding = false
        }
        escurecimento.clipChildren = false
        escurecimento.addView(cartao, FrameLayout.LayoutParams(px(560), px(300), Gravity.CENTER))

        // Borda interna pontilhada.
        cartao.add(
            View(this).apply {
                background = GradientDrawable().apply {
                    setColor(Color.TRANSPARENT)
                    cornerRadius = px(27).toFloat()
                    setStroke(px(2), manteiga, px(8).toFloat(), px(6).toFloat())
                }
            }, 9, 9, 542, 282
        )

        // Halo amarelinho atrás da Bubba.
        cartao.add(
            View(this).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    gradientType = GradientDrawable.RADIAL_GRADIENT
                    gradientRadius = px(100).toFloat()
                    colors = intArrayOf(Color.rgb(255, 253, 235), Color.rgb(255, 232, 158))
                    setStroke(px(4), Color.WHITE)
                }
            }, 24, 46, 176, 176
        )

        val girafa = imagem(rodada.girafa, "Bubba")
        cartao.add(girafa, 34, 56, 156, 160)

        // Sem nuvem decorativa no pop-up: a Bubba é o destaque.

        // Nome da emoção (menta).
        val etiqueta = texto(rodada.emocao.titulo, 17f, Color.WHITE)
            .sombra(Color.argb(140, 30, 110, 100)).ajustar(10f, 17f).apply {
                background = formaGradiente(Color.rgb(132, 212, 192), Color.rgb(84, 184, 166), 17, Color.WHITE, 3)
                elevation = px(4).toFloat()
            }
        cartao.add(etiqueta, 46, 204, 132, 34)

        // Pontinhos de progresso (6 rodadas).
        repeat(rodadas.size) { i ->
            cartao.add(
                View(this).apply {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(if (i <= rodadaAtual) Color.rgb(255, 226, 149) else Color.rgb(214, 228, 240))
                    }
                }, 50 + i * 22, 256, 14, 14
            )
        }

        // Título delicado dentro do cartão: sem fita saindo das bordas.
        val fita = texto(titulo, 24f, azulEscuro).ajustar(14f, 24f).apply {
            background = formaGradiente(
                Color.rgb(255, 253, 226), Color.rgb(255, 239, 181),
                22, Color.rgb(255, 229, 164), 2
            )
        }
        cartao.add(fita, 216, 30, 322, 48)

        // Destaque preservado: Você descobriu! / Você aprendeu!
        val selo = texto(cabecalho, 17f, Color.rgb(64, 124, 115))
            .ajustar(12f, 17f)
        cartao.add(selo, 224, 85, 306, 26)

        // Explicação em painel suave com espaço para ler sem cortar.
        val painel = FrameLayout(this).apply {
            background = formaGradiente(
                Color.rgb(250, 255, 253), Color.rgb(230, 248, 244),
                19, Color.rgb(193, 228, 219), 2
            )
        }
        cartao.add(painel, 216, 117, 322, 84)
        val mensagem = texto(descricao, 17f, azulEscuro).ajustar(10f, 17f).apply {
            maxLines = 4
            setPadding(px(12), px(5), px(12), px(5))
        }
        painel.add(mensagem, 8, 5, 306, 74)

        // Botão verde bem clarinho, com texto escuro para facilitar a leitura.
        cartao.add(View(this).apply {
            background = forma(Color.rgb(145, 203, 158), 28)
        }, 216, 214, 322, 56)
        val continuar = texto(textoBotao, 21f, Color.rgb(48, 111, 76))
            .ajustar(11f, 22f).apply {
                background = formaGradiente(
                    Color.rgb(233, 252, 227), Color.rgb(184, 236, 196),
                    28, Color.rgb(247, 255, 245), 3
                )
                setOnClickListener {
                    if (overlay == null) return@setOnClickListener
                    tocarClique()
                    rodadaAtual = if (ultima) 0 else rodadaAtual + 1
                    montarRodada()
                    val versaoAtual = versaoDaRodada
                    // Glitter na NOVA rodada, após a cena ser montada.
                    tela.post {
                        if (!isFinishing && !isDestroyed &&
                            versaoDaRodada == versaoAtual && overlay == null
                        ) {
                            glitter(tela, if (ultima) 90 else 55)
                        }
                    }
                }
            }
        cartao.add(continuar, 216, 208, 322, 56)
        pulsar(continuar, 1100L)

        if (ultima) {
            val sair = texto("Voltar aos jogos", 14f, azulEscuro).ajustar(9f, 15f).apply {
                background = forma(Color.rgb(222, 240, 255), 12)
                setOnClickListener {
                    tocarClique()
                    voltarParaJogos()
                }
            }
            cartao.add(sair, 246, 268, 262, 22)
        }

        // Entrada animada.
        cartao.scaleX = 0.8f * fator
        cartao.scaleY = 0.8f * fator
        cartao.alpha = 0f
        cartao.animate().alpha(1f).scaleX(fator).scaleY(fator)
            .setDuration(320).setInterpolator(OvershootInterpolator(1.15f)).start()


        cartao.post {
            if (!isFinishing && !isDestroyed && overlay === escurecimento) {
                glitter(cartao, if (ultima) 85 else 55)
            }
        }
    }

    private fun glitter(alvo: View, quantidade: Int) {
        val lugar = IntArray(2)
        val origem = IntArray(2)
        alvo.getLocationOnScreen(lugar)
        tela.getLocationOnScreen(origem)
        val centroX = lugar[0] - origem[0] + alvo.width / 2f
        val centroY = lugar[1] - origem[1] + alvo.height / 2f
        val cores = intArrayOf(rosa, amarelo, azul, Color.rgb(255, 176, 120), Color.WHITE, verdeSuave)

        repeat(quantidade) { i ->
            val estrela = texto(
                if (i % 3 == 0) "★" else "✦",
                Random.nextInt(12, 23).toFloat(), cores.random()
            )
            val tamanho = px(30)
            tela.addView(estrela, FrameLayout.LayoutParams(tamanho, tamanho).apply {
                leftMargin = (centroX - tamanho / 2f).toInt()
                topMargin = (centroY - tamanho / 2f).toInt()
            })
            estrela.alpha = 1f
            estrela.scaleX = 0.25f
            estrela.scaleY = 0.25f
            estrela.animate()
                .translationX(px(Random.nextInt(-190, 190)).toFloat())
                .translationY(px(Random.nextInt(-155, 155)).toFloat())
                .rotation(Random.nextInt(-260, 260).toFloat())
                .scaleX(1.1f).scaleY(1.1f).alpha(0f)
                .setDuration(Random.nextLong(700L, 1300L))
                .withEndAction { tela.removeView(estrela) }
                .start()
        }
    }

    // =====================================================
    // 5. ÁUDIO / NARRAÇÃO
    // =====================================================
    private fun tocarNome(nome: String) {
        val id = resources.getIdentifier(nome, "raw", packageName)
        if (id == 0) return
        try {
            val player = MediaPlayer.create(this, id) ?: return
            sons.add(player)
            player.setVolume(0.52f, 0.52f)
            player.setOnCompletionListener {
                sons.remove(it)
                it.release()
            }
            player.start()
        } catch (_: Exception) { }
    }

    private fun tocarClique() = tocarNome("clique_suave")

    private fun tocarAcerto() {
        val disponivel = resources.getIdentifier("brilho_suave", "raw", packageName) != 0
        tocarNome(if (disponivel) "brilho_suave" else "sominicio")
    }

    private fun tocarErro() {
        val disponivel = resources.getIdentifier("som_erro", "raw", packageName) != 0
        tocarNome(if (disponivel) "som_erro" else "clique_suave")
    }

    private fun iniciarAmbienteOpcional() {
        val id = resources.getIdentifier("fundoemocoes", "raw", packageName)
        if (id == 0) return
        try {
            ambiente = MediaPlayer.create(this, id)?.apply {
                isLooping = true
                setVolume(0.08f, 0.08f)
                start()
            }
        } catch (_: Exception) { }
    }

    // As nuvens só aparecem quando o Android sinaliza o FINAL da fala.
    private fun configurarNarrador() {
        narrador = TextToSpeech(this) { resultado ->
            if (resultado == TextToSpeech.SUCCESS) {
                val configuracao = narrador?.setLanguage(Locale("pt", "BR"))
                vozDisponivel = configuracao != TextToSpeech.LANG_MISSING_DATA &&
                        configuracao != TextToSpeech.LANG_NOT_SUPPORTED
                narrador?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit

                    override fun onDone(utteranceId: String?) {
                        runOnUiThread { concluirNarracao(utteranceId) }
                    }

                    @Deprecated("Callback de compatibilidade do TTS")
                    override fun onError(utteranceId: String?) {
                        runOnUiThread { concluirNarracao(utteranceId) }
                    }
                })
            } else {
                vozDisponivel = false
            }
            runOnUiThread {
                if (vozDisponivel) narrarRodadaSePossivel()
                else descerNuvens(versaoDaRodada)
            }
        }
    }

    private fun narrarRodadaSePossivel() {
        if (versaoDaRodada <= 0 || overlay != null) return
        if (ultimaRodadaNarrada == versaoDaRodada) return
        if (!vozDisponivel) return // aguardamos a inicialização do TTS
        ultimaRodadaNarrada = versaoDaRodada
        narrar(rodadas[rodadaAtual].historia + " Como a Bubba está se sentindo?")
    }

    private fun narrar(frase: String) {
        if (!vozDisponivel) {
            descerNuvens(versaoDaRodada)
            return
        }
        // Se a criança repetir o áudio, as nuvens esperam outra vez lá no alto.
        esconderNuvensParaNarracao()
        val id = "bubba_${versaoDaRodada}_${++sequenciaNarracao}"
        narracaoEmAndamentoId = id
        val resultado = narrador?.speak(frase, TextToSpeech.QUEUE_FLUSH, null, id)
        if (resultado != TextToSpeech.SUCCESS) {
            narracaoEmAndamentoId = null
            descerNuvens(versaoDaRodada)
        }
    }

    private fun esconderNuvensParaNarracao() {
        nuvensJaDesceram = false
        nuvensDaRodada.forEachIndexed { indice, nuvem ->
            // Interrompe a flutuação anterior, caso a história seja repetida.
            animacoes.filter { it.target === nuvem }.forEach { it.cancel() }
            animacoes.removeAll { it.target === nuvem }
            nuvem.animate().cancel()
            nuvem.translationY = -px(nuvensY[indice] + nuvemAltura + 35).toFloat()
            nuvem.alpha = 0f
            nuvem.isClickable = false
            nuvem.isFocusable = false
        }
    }

    private fun concluirNarracao(id: String?) {
        if (id == null || id != narracaoEmAndamentoId) return
        narracaoEmAndamentoId = null
        descerNuvens(versaoDaRodada)
    }

    private fun descerNuvens(versao: Int) {
        if (isFinishing || isDestroyed || overlay != null ||
            versao != versaoDaRodada || nuvensJaDesceram || nuvensDaRodada.size != nuvensX.size) return
        nuvensJaDesceram = true
        nuvensDaRodada.forEachIndexed { indice, nuvem ->
            nuvem.animate().cancel()
            nuvem.animate()
                .translationY(0f)
                .alpha(1f)
                .setStartDelay(indice * 350L)
                .setDuration(1450L)
                .setInterpolator(OvershootInterpolator(0.85f))
                .withEndAction {
                    if (versao == versaoDaRodada && overlay == null) {
                        nuvem.isClickable = true
                        nuvem.isFocusable = true
                        // A flutuação começa apenas depois de descer.
                        flutuar(nuvem, 6 + indice * 2, 1800L + indice * 200L)
                        pulsar(nuvem, 1250L + indice * 180L)
                    }
                }.start()
        }
    }

    override fun onPause() {
        super.onPause()
        ambiente?.pause()
        narrador?.stop()
        narracaoEmAndamentoId = null
        if (!nuvensJaDesceram) descerNuvens(versaoDaRodada)
    }

    override fun onResume() {
        super.onResume()
        if (ambiente != null) {
            try { ambiente?.start() } catch (_: Exception) { }
        }
    }

    override fun onDestroy() {
        animacoes.forEach { it.cancel() }
        animacoes.clear()
        sons.toList().forEach {
            try { it.release() } catch (_: Exception) { }
        }
        sons.clear()
        try { ambiente?.release() } catch (_: Exception) { }
        ambiente = null
        narrador?.stop()
        narrador?.shutdown()
        narrador = null
        super.onDestroy()
    }
}
