package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.min
import kotlin.random.Random

class BibliotecaSonoraActivity : Activity() {

    private lateinit var tela: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 393f
    private var sx = 1f
    private var sy = 1f

    private var audioAtual: MediaPlayer? = null
    private var audioRetorno: MediaPlayer? = null
    private var audioEfeito: MediaPlayer? = null
    private var retornando = false
    private var popupAberto = false
    private var podeResponder = false
    private var rodadaAtual = 0
    private var telaMontada = false
    private var jogoIniciado = false
    private var larguraMontada = 0
    private var alturaMontada = 0

    private val animacoes = mutableMapOf<View, ObjectAnimator>()
    private val imagens = mutableMapOf<String, ImageView>()
    private var camadaPopup: FrameLayout? = null
    private var animacaoPopup: ObjectAnimator? = null
    private var progressoTexto: TextView? = null
    private var painelEtapas: FrameLayout? = null

    // O dinossauro só entra como resposta correta se o áudio som_dinossauro
    // existir em res/raw. Sem ele, continua visível como opção de imagem.
    private data class Desafio(val imagem: String, val nome: String, val audio: Int)
    private var desafios = emptyList<Desafio>()

    private enum class Estilo { INTRO, ACERTO, ERRO, FINAL }

    private data class Tema(
        val faixaTopo: Int,
        val faixaBase: Int,
        val borda: Int,
        val circulo: Int,
        val botaoTopo: Int,
        val botaoBase: Int,
        val botaoSombra: Int,
        val corTitulo: Int,
        val corBotao: Int,
        val dica: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Usa também a área do "notch" nas laterais, sem faixa vazia.
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        aplicarModoImersivo()

        setContentView(R.layout.activity_biblioteca_sonora)
        tela = findViewById(R.id.telaBibliotecaSonora)

        // Se o tamanho da tela mudar depois de montada (barras sumindo, por exemplo),
        // remonta com as medidas certas para não sobrar espaço vazio à direita/embaixo.
        tela.addOnLayoutChangeListener { _, l, t, r, b, _, _, _, _ ->
            val w = r - l
            val h = b - t
            if (telaMontada && w > 0 && h > 0 && (w != larguraMontada || h != alturaMontada)) {
                tela.post {
                    if (!isFinishing && !isDestroyed) {
                        montarTela()
                        if (jogoIniciado) iniciarRodada() else mostrarIntroducao()
                    }
                }
            }
        }

        desafios = criarDesafios().shuffled()
        tela.post {
            if (!isFinishing && !isDestroyed) {
                montarTela()
                mostrarIntroducao()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun aplicarModoImersivo() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) aplicarModoImersivo()
    }

    private fun raw(nome: String): Int =
        resources.getIdentifier(nome, "raw", packageName)

    private fun criarDesafios(): List<Desafio> {
        return listOf(
            Triple("urso", "urso", "som_urso"),
            Triple("bola", "bola", "som_bola"),
            Triple("cavalinho", "cavalinho", "som_cavalinho"),
            Triple("meninocomaviao", "avião", "som_aviao"),
            Triple("dinossauro", "dinossauro", "som_dinossauro"),
            Triple("brinquedos", "brinquedos", "som_brinquedos")
        ).mapNotNull { (imagem, nome, som) ->
            val id = raw(som)
            if (id != 0) Desafio(imagem, nome, id) else null
        }
    }

    private fun montarTela() {
        tela.removeAllViews()
        animacoes.values.forEach { it.cancel() }
        animacoes.clear()
        animacaoPopup?.cancel()
        animacaoPopup = null
        imagens.clear()
        painelEtapas = null
        camadaPopup = null
        popupAberto = false
        podeResponder = false
        retornando = false

        sx = tela.width.toFloat() / larguraBase
        sy = tela.height.toFloat() / alturaBase
        larguraMontada = tela.width
        alturaMontada = tela.height
        telaMontada = true

        // Fundo e imagens originais do seu projeto.
        val planoFundo = adicionarImagem("planofundobibliotecasonora", 0, 0, 874, 393)
        // Preenche a tela inteira, sem sobrar faixa nas bordas.
        planoFundo.scaleType = ImageView.ScaleType.FIT_XY
        planoFundo.layoutParams = FrameLayout.LayoutParams(-1, -1)

        val voltar = adicionarImagem("botaoretornoazul", 19, 25, 40, 40)
        voltar.contentDescription = "Voltar aos jogos"
        voltar.setOnClickListener {
            if (!retornando) {
                retornando = true
                pararAudio()
                animarPulsacao(voltar)
                tocarSomRetorno()
            }
        }

        // Título original, menor e centralizado para dar espaço ao jogo.
        // Título visual removido conforme solicitado.

        // Botão ouvir som: pílula sólida no topo direito
        val repetir = texto("🔊  Ouvir som", 16f, Color.rgb(43, 101, 148)).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.rgb(215, 244, 255), Color.rgb(159, 221, 250))
            ).apply {
                cornerRadius = px(24).toFloat()
                setStroke(px(2), Color.rgb(130, 202, 238))
            }
            elevation = px(4).toFloat()
            contentDescription = "Ouvir novamente o som desta rodada"
            isClickable = true
            isFocusable = true
            setOnClickListener {
                if (!popupAberto && podeResponder && !retornando) {
                    animarPulsacao(this)
                    tocarSomDesafio()
                }
            }
        }
        adicionar(repetir, 700, 16, 150, 46)

        // Progresso completo dos elementos, no rodapé. Os círculos ficam
        // coloridos conforme cada som é acertado. Não interfere nos brinquedos.
        val trilha = FrameLayout(this).apply {
            background = fundo(
                Color.argb(237, 250, 253, 255), 19,
                Color.rgb(183, 220, 238), 2
            )
            elevation = px(3).toFloat()
        }
        adicionar(trilha, 310, 348, 252, 38)
        progressoTexto = texto("", 12f, Color.rgb(52, 109, 142))
        colocar(trilha, progressoTexto!!, 9, 5, 56, 28)
        painelEtapas = FrameLayout(this)
        colocar(trilha, painelEtapas!!, 67, 4, 178, 30)

        adicionarOpcao("urso", 40, 172, 140, 180)
        adicionarOpcao("bola", 188, 276, 108, 108)
        adicionarOpcao("cavalinho", 258, 135, 175, 170)
        adicionarOpcao("meninocomaviao", 450, 78, 180, 200)
        adicionarOpcao("dinossauro", 680, 95, 190, 220)
        adicionarOpcao("brinquedos", 490, 280, 185, 110)

        atualizarProgresso()
    }

    private fun adicionarOpcao(nome: String, x: Int, y: Int, w: Int, h: Int) {
        val imagem = adicionarImagem(nome, x, y, w, h)
        imagens[nome] = imagem
        imagem.isClickable = true
        imagem.isFocusable = true
        imagem.contentDescription = "Escolher $nome"
        imagem.setOnClickListener {
            if (!popupAberto && podeResponder && !retornando) {
                // Bloqueia toques repetidos enquanto a imagem pulsa uma única vez.
                podeResponder = false
                animarPulsacao(imagem)
                imagem.postDelayed({
                    // Mostra o resultado somente depois da pulsação terminar.
                    if (!isFinishing && !isDestroyed && !retornando && !popupAberto) {
                        verificarEscolha(nome)
                    }
                }, 350L)
            }
        }
    }

    private fun atualizarProgresso() {
        val total = desafios.size
        progressoTexto?.text = if (total == 0) "Sem sons" else "${min(rodadaAtual + 1, total)}/$total"
        painelEtapas?.let { painel ->
            painel.removeAllViews()
            if (total == 0) return@let
            val diametro = if (total > 6) 19 else 23
            val passo = 176f / total
            for (indice in 0 until total) {
                val concluido = indice < rodadaAtual
                val atual = indice == rodadaAtual
                val fundoEtapa = when {
                    concluido -> Color.rgb(172, 228, 183)
                    atual -> Color.rgb(188, 228, 250)
                    else -> Color.rgb(244, 248, 251)
                }
                val bordaEtapa = when {
                    concluido -> Color.rgb(110, 190, 134)
                    atual -> Color.rgb(110, 177, 219)
                    else -> Color.rgb(210, 226, 235)
                }
                val etapa = texto("${indice + 1}", 12f, Color.rgb(51, 105, 136)).apply {
                    background = fundo(fundoEtapa, 13, bordaEtapa, 2)
                }
                colocar(painel, etapa,
                    (indice * passo + (passo - diametro) / 2).toInt(),
                    (30 - diametro) / 2,
                    diametro, diametro)
            }
        }
    }

    private fun mostrarIntroducao() {
        abrirPopup(
            estilo = Estilo.INTRO,
            titulo = "Brinquedoteca Sonora",
            mensagem = "Escute o som e toque na imagem que combina com ele!",
            botao = "COMEÇAR"
        ) {
            if (desafios.isEmpty()) {
                Toast.makeText(this, "Adicione os áudios na pasta res/raw", Toast.LENGTH_LONG).show()
            } else {
                jogoIniciado = true
                iniciarRodada()
            }
        }
    }

    private fun iniciarRodada() {
        if (rodadaAtual !in desafios.indices) return
        podeResponder = true
        atualizarProgresso()
        tela.postDelayed({
            if (!popupAberto && podeResponder && !isFinishing) tocarSomDesafio()
        }, 280L)
    }

    private fun tocarSomDesafio() {
        if (rodadaAtual !in desafios.indices) return
        pararAudio()
        try {
            val player = MediaPlayer.create(this, desafios[rodadaAtual].audio)
            if (player == null) {
                Toast.makeText(this, "Não foi possível reproduzir o som", Toast.LENGTH_SHORT).show()
                return
            }
            audioAtual = player
            player.setVolume(0.85f, 0.85f)
            player.setOnCompletionListener { concluido ->
                if (audioAtual === concluido) audioAtual = null
                concluido.release()
            }
            player.start()
        } catch (_: Exception) {
            pararAudio()
            Toast.makeText(this, "Não foi possível reproduzir o som", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verificarEscolha(nome: String) {
        val desafio = desafios.getOrNull(rodadaAtual) ?: return
        podeResponder = false
        pararAudio()

        if (nome == desafio.imagem) {
            // A celebração FINAL só aparece após acertar o último desafio.
            val ultima = rodadaAtual == desafios.lastIndex
            // Se existir res/raw/comemoracao, toca no final; senão usa o clique suave.
            tocarEfeito(if (ultima && raw("comemoracao") != 0) "comemoracao" else "clique_suave")
            abrirPopup(
                estilo = if (ultima) Estilo.FINAL else Estilo.ACERTO,
                titulo = if (ultima) "Parabéns!" else "Muito bem!",
                mensagem = if (ultima) {
                    "Você acertou todos os sons! Você é um campeão, amiguinho!"
                } else {
                    "Isso mesmo! O som é de ${desafio.nome}!"
                },
                botao = if (ultima) "JOGAR DE NOVO" else "PRÓXIMO SOM"
            ) {
                if (ultima) {
                    rodadaAtual = 0
                    desafios = criarDesafios().shuffled()
                } else {
                    rodadaAtual++
                }
                iniciarRodada()
            }
            // Confete visível sobre o pop-up de acerto (também no último som).
            camadaPopup?.postDelayed({
                if (popupAberto && !isFinishing && !isDestroyed) {
                    if (ultima) festaFinal() else brilhoAcerto()
                }
            }, 220L)
        } else {
            tocarEfeito("clique_pop")
            abrirPopup(
                estilo = Estilo.ERRO,
                titulo = "Quase lá!",
                mensagem = "Não foi dessa vez. Escute de novo e tente outra imagem!",
                botao = "TENTAR DE NOVO"
            ) {
                podeResponder = true
                tocarSomDesafio()
            }
        }
    }

    // Apenas três famílias de cor pastel: azul clarinho, amarelo carinho e verde clarinho.
    private fun temaAzul(dica: String) = Tema(
        faixaTopo = Color.rgb(196, 230, 251),
        faixaBase = Color.rgb(154, 210, 243),
        borda = Color.rgb(168, 216, 244),
        circulo = Color.rgb(233, 247, 255),
        botaoTopo = Color.rgb(160, 214, 245),
        botaoBase = Color.rgb(122, 194, 238),
        botaoSombra = Color.rgb(86, 158, 208),
        corTitulo = Color.rgb(44, 90, 130),
        corBotao = Color.rgb(34, 78, 118),
        dica = dica
    )

    private fun temaVerde(dica: String) = Tema(
        faixaTopo = Color.rgb(206, 242, 210),
        faixaBase = Color.rgb(164, 227, 178),
        borda = Color.rgb(174, 227, 188),
        circulo = Color.rgb(235, 251, 239),
        botaoTopo = Color.rgb(170, 230, 184),
        botaoBase = Color.rgb(130, 211, 152),
        botaoSombra = Color.rgb(88, 172, 112),
        corTitulo = Color.rgb(38, 112, 72),
        corBotao = Color.rgb(28, 98, 60),
        dica = dica
    )

    private fun temaAmarelo(dica: String) = Tema(
        faixaTopo = Color.rgb(255, 241, 178),
        faixaBase = Color.rgb(255, 228, 130),
        borda = Color.rgb(250, 230, 146),
        circulo = Color.rgb(255, 250, 228),
        botaoTopo = Color.rgb(255, 230, 134),
        botaoBase = Color.rgb(255, 212, 92),
        botaoSombra = Color.rgb(226, 172, 44),
        corTitulo = Color.rgb(148, 98, 10),
        corBotao = Color.rgb(128, 84, 6),
        dica = dica
    )

    private fun temaDe(estilo: Estilo): Tema = when (estilo) {
        Estilo.INTRO -> temaAzul("Vamos brincar?")
        Estilo.ACERTO -> temaVerde("Você está indo muito bem!")
        Estilo.ERRO -> temaAmarelo("Você consegue!")
        Estilo.FINAL -> temaAmarelo("")
    }

    // Botão "gordinho" com sombra sólida embaixo (efeito 3D), bom para dedos pequenos.
    private fun botao3D(conteudo: String, tema: Tema): TextView {
        val sombra = GradientDrawable().apply {
            setColor(tema.botaoSombra)
            cornerRadius = px(29).toFloat()
        }
        val topo = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(tema.botaoTopo, tema.botaoBase)
        ).apply {
            cornerRadius = px(29).toFloat()
        }
        val camadas = LayerDrawable(arrayOf(sombra, topo)).apply {
            setLayerInset(1, 0, 0, 0, py(6))
        }
        return texto(conteudo, 21f, tema.corBotao).apply {
            background = camadas
            setPadding(px(10), 0, px(10), py(6))
            elevation = px(4).toFloat()
            isClickable = true
            isFocusable = true
        }
    }

    // Pop-up horizontal e colorido: faixa de título no topo, mascote grande à esquerda,
    // texto e botão grande à direita. Tudo fica dentro do cartão.
    private fun abrirPopup(
        estilo: Estilo,
        titulo: String,
        mensagem: String,
        botao: String,
        acao: () -> Unit
    ) {
        popupAberto = true
        podeResponder = false
        animacaoPopup?.cancel()
        animacaoPopup = null
        camadaPopup?.let { tela.removeView(it) }

        val tema = temaDe(estilo)
        val erro = estilo == Estilo.ERRO
        val comEstrelas = estilo == Estilo.ACERTO || estilo == Estilo.FINAL
        val corTexto = Color.rgb(70, 92, 118)

        val camada = FrameLayout(this).apply {
            setBackgroundColor(Color.argb(150, 30, 55, 80))
            isClickable = true
            isFocusable = true
            // Fica acima do "Ouvir som", do progresso e das imagens (que têm elevation).
            elevation = px(50).toFloat()
        }
        camadaPopup = camada
        tela.addView(camada, FrameLayout.LayoutParams(-1, -1))

        val ehFinal = estilo == Estilo.FINAL
        val larg = if (ehFinal) 560 else 540
        val alt = if (ehFinal) 322 else 290

        val cartao = FrameLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                if (ehFinal) intArrayOf(Color.rgb(255, 254, 240), Color.rgb(255, 246, 210))
                else intArrayOf(Color.WHITE, Color.rgb(250, 253, 255))
            ).apply {
                cornerRadius = px(34).toFloat()
                setStroke(px(if (ehFinal) 8 else 6), tema.borda)
            }
            elevation = px(12).toFloat()
        }
        camada.addView(
            cartao,
            FrameLayout.LayoutParams(px(larg), py(alt), Gravity.CENTER)
        )

        // Faixa colorida do título
        val faixa = View(this).apply {
            val r = px(28).toFloat()
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(tema.faixaTopo, tema.faixaBase)
            ).apply {
                setCornerRadii(floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f))
            }
        }
        colocar(cartao, faixa, 6, 6, larg - 12, 70)

        val enfeite = if (comEstrelas) "★" else "✦"
        val corEnfeite = tema.corTitulo
        colocar(cartao, texto(enfeite, 28f, corEnfeite), 20, 14, 46, 54)
        colocar(cartao, texto(enfeite, 28f, corEnfeite), larg - 66, 14, 46, 54)

        val tituloView = texto(titulo, if (ehFinal) 36f else 29f, tema.corTitulo)
        colocar(cartao, tituloView, 72, 10, larg - 144, 62)

        // Final: três estrelas douradas que "estouram" uma a uma abaixo do título
        if (ehFinal) {
            val dourado = Color.rgb(255, 196, 45)
            for (i in 0..2) {
                val tam = if (i == 1) 46f else 36f
                val estrela = texto("★", tam, dourado).apply {
                    scaleX = 0f
                    scaleY = 0f
                }
                val centro = larg / 2 + (i - 1) * 64
                colocar(cartao, estrela, centro - 28, 80, 56, 52)
                estrela.animate()
                    .scaleX(1f).scaleY(1f)
                    .rotation(if (i == 1) 0f else if (i == 0) -12f else 12f)
                    .setStartDelay(500L + i * 220L)
                    .setDuration(420L)
                    .setInterpolator(OvershootInterpolator(2.4f))
                    .start()
            }
        }

        // Mascote: círculo pastel com a girafa
        val ms = if (ehFinal) 160 else 170
        val mascote = FrameLayout(this)
        colocar(cartao, mascote, if (ehFinal) 40 else 36, if (ehFinal) 142 else 98, ms, ms)

        val circulo = View(this).apply {
            background = fundo(tema.circulo, ms / 2, tema.borda, 6)
        }
        colocar(mascote, circulo, 0, 0, ms, ms)

        // A própria girafa: feliz no acerto/início/final, em dúvida no erro
        val girafa = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            val nomeImagem = if (erro) "girafaduvida" else "girafafeliz"
            val id = resources.getIdentifier(nomeImagem, "drawable", packageName)
            if (id != 0) setImageResource(id)
        }
        val inset = if (ehFinal) 12 else 10
        colocar(mascote, girafa, inset, inset, ms - 2 * inset, ms - 2 * inset)

        // Coluna da direita: mensagem, botão e linha de baixo
        val rx = if (ehFinal) 230 else 226
        val rw = larg - rx - 24

        val msg = texto(mensagem, 20f, corTexto).apply {
            maxLines = 3
            setPadding(px(4), 0, px(4), 0)
        }
        colocar(cartao, msg, rx, if (ehFinal) 138 else 90, rw, if (ehFinal) 76 else 84)

        val continuar = botao3D(botao, tema).apply {
            setOnClickListener {
                if (camadaPopup !== camada) return@setOnClickListener
                tocarEfeito("clique_suave")
                fecharPopup()
                acao()
            }
        }
        colocar(cartao, continuar, rx, if (ehFinal) 222 else 184, rw, if (ehFinal) 58 else 62)

        if (ehFinal) {
            val sair = texto("Voltar aos jogos", 14f, Color.rgb(75, 123, 155)).apply {
                background = fundo(Color.rgb(235, 246, 252), 15, Color.rgb(190, 220, 238), 2)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    fecharPopup()
                    voltarParaCincoOpcoes()
                }
            }
            colocar(cartao, sair, rx + (rw - 190) / 2, 286, 190, 30)
        } else {
            colocar(cartao, texto(tema.dica, 14f, Color.rgb(130, 150, 168)), rx, 252, rw, 26)
        }

        // Entrada do cartão com um "pulinho"
        cartao.scaleX = 0.8f
        cartao.scaleY = 0.8f
        cartao.alpha = 0f
        cartao.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setInterpolator(OvershootInterpolator(1.2f))
            .setDuration(320L)
            .start()

        // Mascote: pula de alegria no acerto, balança de leve no erro
        val animMascote = if (erro) {
            ObjectAnimator.ofFloat(mascote, View.ROTATION, 0f, -9f, 9f, -6f, 6f, 0f).apply {
                duration = 700L
                startDelay = 350L
            }
        } else {
            ObjectAnimator.ofFloat(
                mascote, View.TRANSLATION_Y, 0f, -py(if (ehFinal) 22 else 14).toFloat(), 0f
            ).apply {
                duration = if (ehFinal) 420L else 480L
                startDelay = 350L
                repeatCount = if (ehFinal) 9 else 3
            }
        }
        animacaoPopup = animMascote
        animMascote.start()
    }

    private fun fecharPopup() {
        animacaoPopup?.cancel()
        animacaoPopup = null
        camadaPopup?.let { tela.removeView(it) }
        camadaPopup = null
        popupAberto = false
    }

    // Comemoração final: várias ondas de glitter + chuva de brilho caindo do topo.
    private fun festaFinal() {
        val camada = camadaPopup ?: return
        brilhoAcerto()
        chuvaDeBrilho()
        longArrayOf(700L, 1500L, 2400L, 3400L).forEach { atraso ->
            camada.postDelayed({
                if (popupAberto && camadaPopup === camada && !isFinishing && !isDestroyed) {
                    brilhoAcerto()
                    chuvaDeBrilho()
                }
            }, atraso)
        }
    }

    private fun chuvaDeBrilho() {
        val camada = camadaPopup ?: return
        if (camada.width <= 0 || camada.height <= 0) return

        val cores = intArrayOf(
            Color.rgb(255, 205, 70),
            Color.rgb(138, 205, 245),
            Color.rgb(140, 218, 164),
            Color.rgb(255, 232, 150),
            Color.WHITE
        )
        repeat(36) { indice ->
            val tamanho = px(Random.nextInt(18, 32))
            val peca = texto(
                if (indice % 3 == 0) "★" else "✦",
                Random.nextInt(14, 26).toFloat(),
                cores[indice % cores.size]
            ).apply {
                isClickable = false
                isFocusable = false
                alpha = 0f
            }
            camada.addView(peca, FrameLayout.LayoutParams(tamanho, tamanho).apply {
                leftMargin = Random.nextInt(0, (camada.width - tamanho).coerceAtLeast(1))
                topMargin = 0
            })
            peca.animate()
                .alpha(1f)
                .setStartDelay(Random.nextLong(0L, 700L))
                .setDuration(150L)
                .withEndAction {
                    peca.animate()
                        .translationY(camada.height.toFloat())
                        .rotation(Random.nextInt(-360, 361).toFloat())
                        .alpha(0.2f)
                        .setDuration(Random.nextLong(1400L, 2400L))
                        .withEndAction {
                            if (peca.parent === camada) camada.removeView(peca)
                        }.start()
                }.start()
        }
    }

    // Partículas acima do cartão de acerto, sem cobrir os botões permanentemente.
    private fun brilhoAcerto() {
        val camada = camadaPopup ?: return
        if (camada.width <= 0 || camada.height <= 0) return

        val cores = intArrayOf(
            Color.rgb(255, 214, 102),
            Color.rgb(138, 205, 245),
            Color.rgb(140, 218, 164),
            Color.rgb(255, 232, 150),
            Color.WHITE
        )
        val cx = camada.width / 2f
        val cy = camada.height / 2f
        val raioX = px(480).toFloat()
        val raioY = py(250).toFloat()

        repeat(55) { indice ->
            val tamanho = px(Random.nextInt(16, 29))
            val estrela = texto(
                if (indice % 3 == 0) "★" else "✦",
                Random.nextInt(13, 23).toFloat(),
                cores[indice % cores.size]
            ).apply {
                isClickable = false
                isFocusable = false
                alpha = 0f
                scaleX = 0.3f
                scaleY = 0.3f
            }
            camada.addView(estrela, FrameLayout.LayoutParams(tamanho, tamanho).apply {
                leftMargin = (cx - tamanho / 2f + Random.nextFloat() * raioX - raioX / 2f).toInt()
                topMargin = (cy - tamanho / 2f + Random.nextFloat() * raioY - raioY / 2f).toInt()
            })
            estrela.animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setStartDelay(Random.nextLong(0L, 320L))
                .setDuration(220L)
                .withEndAction {
                    estrela.animate()
                        .translationX(px(Random.nextInt(-170, 171)).toFloat())
                        .translationY(py(Random.nextInt(-115, 116)).toFloat())
                        .rotation(Random.nextInt(-160, 161).toFloat())
                        .alpha(0f)
                        .setDuration(Random.nextLong(650L, 1100L))
                        .withEndAction {
                            if (estrela.parent === camada) camada.removeView(estrela)
                        }.start()
                }.start()
        }
    }

    private fun adicionarImagem(nome: String, x: Int, y: Int, w: Int, h: Int): ImageView {
        val view = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            val id = resources.getIdentifier(nome, "drawable", packageName)
            if (id != 0) setImageResource(id)
            else Toast.makeText(this@BibliotecaSonoraActivity, "Imagem não encontrada: $nome", Toast.LENGTH_SHORT).show()
        }
        adicionar(view, x, y, w, h)
        return view
    }

    private fun adicionar(view: View, x: Int, y: Int, w: Int, h: Int) {
        tela.addView(view, FrameLayout.LayoutParams(px(w), py(h)).apply {
            leftMargin = px(x)
            topMargin = py(y)
        })
    }

    private fun colocar(pai: FrameLayout, view: View, x: Int, y: Int, w: Int, h: Int) {
        pai.addView(view, FrameLayout.LayoutParams(px(w), py(h)).apply {
            leftMargin = px(x)
            topMargin = py(y)
        })
    }

    private fun texto(conteudo: String, tamanho: Float, cor: Int): TextView = TextView(this).apply {
        text = conteudo
        gravity = Gravity.CENTER
        setTextColor(cor)
        typeface = Typeface.create("sans-serif-rounded", Typeface.BOLD)
        includeFontPadding = false
        setTextSize(TypedValue.COMPLEX_UNIT_PX, tamanho * min(sx, sy))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val escala = min(sx, sy)
            setAutoSizeTextTypeUniformWithConfiguration(
                (tamanho * escala * 0.65f).toInt().coerceAtLeast(7),
                (tamanho * escala).toInt().coerceAtLeast(8),
                1,
                TypedValue.COMPLEX_UNIT_PX
            )
        }
    }

    private fun fundo(cor: Int, raio: Int, borda: Int, espessura: Int = 2) =
        GradientDrawable().apply {
            setColor(cor)
            cornerRadius = px(raio).toFloat()
            setStroke(px(espessura).coerceAtLeast(1), borda)
        }

    private fun px(valor: Int) = (valor * sx).toInt().coerceAtLeast(1)
    private fun py(valor: Int) = (valor * sy).toInt().coerceAtLeast(1)

    private fun animarPulsacao(view: View) {
        animacoes.remove(view)?.cancel()
        view.scaleX = 1f
        view.scaleY = 1f
        val animacao = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0.93f, 1.06f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.93f, 1.06f, 1f)
        ).apply {
            duration = 330L
            repeatCount = 0 // somente uma pulsação por toque
        }
        animacoes[view] = animacao
        animacao.start()
    }

    private fun tocarEfeito(nome: String) {
        val id = raw(nome)
        if (id == 0) return
        try {
            audioEfeito?.release()
            val player = MediaPlayer.create(this, id) ?: return
            audioEfeito = player
            player.setVolume(0.38f, 0.38f)
            player.setOnCompletionListener {
                if (audioEfeito === it) audioEfeito = null
                it.release()
            }
            player.start()
        } catch (_: Exception) { }
    }

    private fun tocarSomRetorno() {
        val id = raw("clique_pop")
        if (id == 0) {
            voltarParaCincoOpcoes()
            return
        }
        try {
            val player = MediaPlayer.create(this, id)
            if (player == null) {
                voltarParaCincoOpcoes()
                return
            }
            audioRetorno = player
            player.setOnCompletionListener {
                if (audioRetorno === it) audioRetorno = null
                it.release()
                voltarParaCincoOpcoes()
            }
            player.setOnErrorListener { _, _, _ ->
                voltarParaCincoOpcoes()
                true
            }
            player.start()
        } catch (_: Exception) {
            voltarParaCincoOpcoes()
        }
    }

    private fun voltarParaCincoOpcoes() {
        if (isFinishing) return
        pararAudio()
        startActivity(Intent(this, CincoOpcoesDosJogosActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }

    private fun pararAudio() {
        val player = audioAtual
        audioAtual = null
        if (player != null) {
            try {
                if (player.isPlaying) player.stop()
            } catch (_: Exception) { }
            try { player.release() } catch (_: Exception) { }
        }
    }

    override fun onStop() {
        pararAudio()
        super.onStop()
    }

    override fun onDestroy() {
        pararAudio()
        try { audioRetorno?.release() } catch (_: Exception) { }
        try { audioEfeito?.release() } catch (_: Exception) { }
        audioRetorno = null
        audioEfeito = null
        animacaoPopup?.cancel()
        animacaoPopup = null
        animacoes.values.forEach { it.cancel() }
        animacoes.clear()
        super.onDestroy()
    }
}
