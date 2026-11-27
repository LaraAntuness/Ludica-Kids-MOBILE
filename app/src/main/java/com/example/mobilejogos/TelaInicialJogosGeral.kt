package com.example.mobilejogos

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class TelaInicialJogosGeral : AppCompatActivity() {

    private lateinit var tela: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 402f

    private var sx = 1f
    private var sy = 1f
    private var escala = 1f

    private var navegando = false

    private var popupPagamento: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // TELA HORIZONTAL

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // TELA CHEIA

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // CRIAR TELA DIRETAMENTE PELO KOTLIN

        tela = FrameLayout(this)

        tela.setBackgroundColor(
            Color.rgb(225, 243, 252)
        )

        tela.clipChildren = false
        tela.clipToPadding = false

        setContentView(tela)

        tela.post {
            montarTela()
        }
    }

    // =====================================================
    // MONTAR TELA
    // =====================================================

    private fun montarTela() {

        tela.removeAllViews()

        sx = tela.width / larguraBase
        sy = tela.height / alturaBase

        escala = minOf(sx, sy)

        // =================================================
        // PLANO DE FUNDO ORIGINAL
        // =================================================

        val fundo = ImageView(this).apply {

            setImageResource(
                R.drawable.planofundopcoesgeral
            )

            scaleType = ImageView.ScaleType.FIT_XY
        }

        tela.addView(
            fundo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // BOTÃO RETORNO
        // =================================================

        val voltar = adicionarImagem(
            "botaoretornoazulclaro",
            19, 18, 45, 45
        )

        voltar.contentDescription = "Voltar"

        voltar.isClickable = true
        voltar.isFocusable = true
        voltar.isSoundEffectsEnabled = false

        animarClique(voltar)

        voltar.setOnClickListener {

            voltarTela()
        }

        // =================================================
        // BUBBA - CANTO SUPERIOR DIREITO
        // =================================================

        val bubba = adicionarImagem(
            "girafabotao",
            794, 8, 45, 50
        )

        bubba.contentDescription = "Quem vai jogar"

        bubba.isClickable = true
        bubba.isFocusable = true
        bubba.isSoundEffectsEnabled = false

        animarClique(bubba)

        bubba.setOnClickListener {

            if (
                navegando ||
                isFinishing ||
                popupPagamento != null
            ) {
                return@setOnClickListener
            }

            tocarSomClique()

            abrirTela(
                QuemVaiJogarActivity::class.java
            )
        }

        // =================================================
        // TÍTULO LÚDICA KIDS
        // =================================================

        adicionarImagem(
            "tituloopcoes3",
            302, 5, 270, 65
        )

        // =================================================
        // FRASE DE BOAS-VINDAS
        // =================================================

        val frase = criarTexto(
            "Qual aventura vamos jogar?",
            19f,
            Color.rgb(79, 124, 145),
            true
        )

        adicionarView(
            frase,
            237, 72, 400, 30
        )

        // =================================================
        // CARTÃO 1 - IDENTIFICANDO AS EMOÇÕES
        // =================================================

        criarCartao(
            titulo = "Identificando\nas Emoções",
            descricao = "Descubra seus sentimentos",
            imagem = "rostogirafa",
            textoBotao = "Jogar",

            x = 95,
            y = 125,
            largura = 210,
            altura = 220,

            corFundo = Color.rgb(255, 244, 217),
            corBorda = Color.rgb(229, 185, 101),
            corTexto = Color.rgb(146, 103, 44),
            corBotao = Color.rgb(241, 192, 101),

            atraso = 0L

        ) {

            abrirTela(
                IdentificandoEmocoesActivity::class.java
            )
        }

        // =================================================
        // CARTÃO 2 - RESPIRANDO COM O DRAGÃO
        // =================================================

        criarCartao(
            titulo = "Respirando\ncom o Dragão",
            descricao = "Respire e relaxe",
            imagem = "rostodragao",
            textoBotao = "Jogar",

            x = 332,
            y = 125,
            largura = 210,
            altura = 220,

            corFundo = Color.rgb(229, 245, 255),
            corBorda = Color.rgb(130, 190, 223),
            corTexto = Color.rgb(65, 127, 158),
            corBotao = Color.rgb(121, 185, 224),

            atraso = 100L

        ) {

            abrirTela(
                RespirandoComDragaoActivity::class.java
            )
        }

        // =================================================
        // CARTÃO 3 - CINCO SENTIDOS MÁGICOS
        // =================================================

        criarCartao(
            titulo = "Cinco Sentidos\nMágicos",
            descricao = "Explore os cinco sentidos",
            imagem = "cadeado",
            textoBotao = "Desbloquear",

            x = 569,
            y = 125,
            largura = 210,
            altura = 220,

            corFundo = Color.rgb(255, 233, 241),
            corBorda = Color.rgb(223, 160, 186),
            corTexto = Color.rgb(166, 95, 128),
            corBotao = Color.rgb(231, 157, 187),

            atraso = 200L

        ) {

            mostrarPopupPagamento()
        }
    }

    // =====================================================
    // CRIAR CARTÕES DOS JOGOS
    // =====================================================

    private fun criarCartao(
        titulo: String,
        descricao: String,
        imagem: String,
        textoBotao: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        corFundo: Int,
        corBorda: Int,
        corTexto: Int,
        corBotao: Int,
        atraso: Long,
        acao: () -> Unit
    ) {

        val container = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false

            isClickable = true
            isFocusable = true
            isSoundEffectsEnabled = false

            contentDescription = "$titulo. $textoBotao"
        }

        adicionarView(
            container,
            x, y, largura, altura + 8
        )

        // =================================================
        // SOMBRA DO CARTÃO
        // =================================================

        val sombra = View(this).apply {

            background = criarFundo(
                corBorda,
                23f,
                corBorda,
                0f
            )

            alpha = 0.85f
        }

        container.addView(
            sombra,
            parametros(
                0, 7, largura, altura
            )
        )

        // =================================================
        // FUNDO DO CARTÃO
        // =================================================

        val cartao = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false

            background = criarFundo(
                corFundo,
                23f,
                corBorda,
                2f
            )
        }

        container.addView(
            cartao,
            parametros(
                0, 0, largura, altura
            )
        )

        // =================================================
        // CÍRCULO DECORATIVO
        // =================================================

        val circulo = View(this).apply {

            background = criarFundo(
                Color.argb(180, 255, 255, 255),
                50f,
                Color.TRANSPARENT,
                0f
            )
        }

        cartao.addView(
            circulo,
            parametros(
                62, 10, 86, 86
            )
        )

        // =================================================
        // IMAGEM DO JOGO
        // =================================================

        val imagemJogo = ImageView(this).apply {

            val id = resources.getIdentifier(
                imagem,
                "drawable",
                packageName
            )

            if (id != 0) {

                setImageResource(id)
            }

            scaleType =
                ImageView.ScaleType.FIT_CENTER

            isClickable = false
            isFocusable = false
        }

        cartao.addView(
            imagemJogo,
            parametros(
                53, 5, 104, 96
            )
        )

        // =================================================
        // ESTRELINHAS DECORATIVAS
        // =================================================

        val estrela1 = criarTexto(
            "✦",
            18f,
            corBorda,
            true
        )

        cartao.addView(
            estrela1,
            parametros(
                25, 21, 25, 28
            )
        )

        val estrela2 = criarTexto(
            "✧",
            16f,
            corBorda,
            true
        )

        cartao.addView(
            estrela2,
            parametros(
                163, 50, 25, 28
            )
        )

        // =================================================
        // TÍTULO DO JOGO
        // =================================================

        val textoTitulo = criarTexto(
            titulo,
            20f,
            corTexto,
            true
        )

        textoTitulo.maxLines = 2

        cartao.addView(
            textoTitulo,
            parametros(
                7, 99, 196, 49
            )
        )

        // =================================================
        // DESCRIÇÃO DO JOGO
        // =================================================

        val textoDescricao = criarTexto(
            descricao,
            11.5f,
            corTexto,
            false
        )

        cartao.addView(
            textoDescricao,
            parametros(
                5, 150, 200, 22
            )
        )

        // =================================================
        // BOTÃO JOGAR OU DESBLOQUEAR
        // =================================================

        val botaoJogar = criarTexto(
            textoBotao,
            16f,
            corTexto,
            true
        )

        botaoJogar.background = criarFundo(
            corBotao,
            18f,
            corBorda,
            0f
        )

        botaoJogar.isSoundEffectsEnabled = false

        cartao.addView(
            botaoJogar,
            parametros(
                29, 179, 152, 32
            )
        )

        // =================================================
        // CLIQUE NO CARTÃO
        // =================================================

        animarClique(container)

        container.setOnClickListener {

            if (
                navegando ||
                isFinishing ||
                popupPagamento != null
            ) {
                return@setOnClickListener
            }

            tocarSomClique()

            acao()
        }

        // =================================================
        // ANIMAÇÃO DE ENTRADA
        // =================================================

        container.alpha = 0f

        container.translationY = 18f * sy

        container.scaleX = 0.95f
        container.scaleY = 0.95f

        container.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(atraso)
            .setDuration(450L)
            .setInterpolator(
                DecelerateInterpolator()
            )
            .start()
    }

    // =====================================================
    // POP-UP DOS CINCO SENTIDOS PAGOS
    // =====================================================

    private fun mostrarPopupPagamento() {

        if (
            popupPagamento != null ||
            navegando ||
            isFinishing
        ) return

        // =================================================
        // FUNDO ESCURECIDO
        // =================================================

        val camada = FrameLayout(this).apply {

            setBackgroundColor(
                Color.argb(110, 35, 45, 60)
            )

            isClickable = true
            isFocusable = true
            isSoundEffectsEnabled = false

            elevation = 25f
        }

        popupPagamento = camada

        tela.addView(
            camada,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // CAIXA DO POP-UP
        // =================================================

        val popup = FrameLayout(this).apply {

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(255, 253, 254),
                    Color.rgb(255, 231, 241)
                )
            ).apply {

                cornerRadius = 30f * escala

                setStroke(
                    (3f * escala)
                        .toInt()
                        .coerceAtLeast(1),

                    Color.rgb(231, 172, 199)
                )
            }

            elevation = 15f

            isClickable = true

            clipChildren = false
            clipToPadding = false
        }

        camada.addView(
            popup,
            FrameLayout.LayoutParams(
                (470 * sx).toInt(),
                (310 * sy).toInt(),
                Gravity.CENTER
            )
        )

        // FUNÇÃO PARA POSICIONAR ELEMENTOS

        fun colocar(
            view: View,
            x: Int,
            y: Int,
            largura: Int,
            altura: Int
        ) {

            popup.addView(
                view,
                parametros(
                    x, y, largura, altura
                )
            )
        }

        // =================================================
        // CÍRCULO ATRÁS DO CADEADO
        // =================================================

        val circulo = View(this).apply {

            background = criarFundo(
                Color.rgb(255, 239, 209),
                60f,
                Color.TRANSPARENT,
                0f
            )
        }

        colocar(
            circulo,
            192, 17, 86, 86
        )

        // =================================================
        // IMAGEM DO CADEADO
        // =================================================

        val cadeado = ImageView(this).apply {

            setImageResource(
                R.drawable.cadeado
            )

            scaleType =
                ImageView.ScaleType.FIT_CENTER
        }

        colocar(
            cadeado,
            188, 10, 94, 94
        )

        // =================================================
        // ESTRELINHAS DECORATIVAS
        // =================================================

        colocar(
            criarTexto(
                "✦",
                28f,
                Color.rgb(241, 194, 102),
                true
            ),
            132, 25, 35, 38
        )

        colocar(
            criarTexto(
                "✧",
                24f,
                Color.rgb(225, 158, 191),
                true
            ),
            303, 58, 35, 38
        )

        colocar(
            criarTexto(
                "✦",
                17f,
                Color.rgb(228, 171, 201),
                true
            ),
            165, 77, 25, 25
        )

        // =================================================
        // TÍTULO DO POP-UP
        // =================================================

        val titulo = criarTexto(
            "Uma aventura especial!",
            26f,
            Color.rgb(188, 112, 151),
            true
        )

        colocar(
            titulo,
            15, 108, 440, 37
        )

        // =================================================
        // MENSAGEM DE CONTEÚDO PAGO
        // =================================================

        val mensagem = criarTexto(
            "Os Cinco Sentidos Mágicos fazem\n" +
                    "parte do conteúdo pago.",
            18f,
            Color.rgb(136, 103, 125),
            true
        )

        mensagem.maxLines = 2

        colocar(
            mensagem,
            20, 151, 430, 55
        )

        // =================================================
        // MENSAGEM COMPLEMENTAR
        // =================================================

        val complemento = criarTexto(
            "Vamos desbloquear essa aventura?",
            16f,
            Color.rgb(136, 103, 125),
            false
        )

        colocar(
            complemento,
            15, 210, 440, 27
        )

        // =================================================
        // BOTÃO VOLTAR
        // =================================================

        val botaoVoltar = criarBotaoPopup(
            "Voltar",
            Color.rgb(216, 237, 249),
            Color.rgb(165, 203, 229),
            Color.rgb(84, 132, 162)
        )

        colocar(
            botaoVoltar,
            35, 247, 185, 44
        )

        botaoVoltar.setOnClickListener {

            if (navegando) return@setOnClickListener

            tocarSomClique()

            fecharPopupPagamento()
        }

        // =================================================
        // BOTÃO VAMOS LÁ!
        // ABRE AS CINCO OPÇÕES DOS JOGOS
        // =================================================

        val botaoVamosLa = criarBotaoPopup(
            "Vamos lá!",
            Color.rgb(244, 186, 212),
            Color.rgb(225, 160, 189),
            Color.rgb(160, 92, 129)
        )

        colocar(
            botaoVamosLa,
            250, 247, 185, 44
        )

        botaoVamosLa.setOnClickListener {

            if (
                navegando ||
                isFinishing
            ) {
                return@setOnClickListener
            }

            // SOM DE CLIQUE

            tocarSomClique()

            // FECHAR O POP-UP

            removerPopupImediatamente()

            // ABRIR CINCO SENTIDOS MÁGICOS

            abrirTela(
                CincoOpcoesDosJogosActivity::class.java
            )
        }

        // =================================================
        // ANIMAÇÃO DE ENTRADA DO POP-UP
        // =================================================

        camada.alpha = 0f

        camada.animate()
            .alpha(1f)
            .setDuration(200L)
            .start()

        popup.scaleX = 0.65f
        popup.scaleY = 0.65f
        popup.alpha = 0f

        popup.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(450L)
            .setInterpolator(
                OvershootInterpolator(1.2f)
            )
            .start()

        // =================================================
        // ANIMAÇÃO DO CADEADO
        // =================================================

        cadeado.scaleX = 0f
        cadeado.scaleY = 0f

        cadeado.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(150L)
            .setDuration(500L)
            .setInterpolator(
                OvershootInterpolator(1.8f)
            )
            .start()
    }

    // =====================================================
    // CRIAR BOTÕES DO POP-UP
    // =====================================================

    private fun criarBotaoPopup(
        texto: String,
        corFundo: Int,
        corBorda: Int,
        corTexto: Int
    ): TextView {

        return criarTexto(
            texto,
            19f,
            corTexto,
            true
        ).apply {

            background = criarFundo(
                corFundo,
                22f,
                corBorda,
                2f
            )

            elevation = 3f

            isClickable = true
            isFocusable = true

            isSoundEffectsEnabled = false

            animarClique(this)
        }
    }

    // =====================================================
    // FECHAR POP-UP COM ANIMAÇÃO
    // =====================================================

    private fun fecharPopupPagamento() {

        val camada = popupPagamento ?: return

        camada.animate().cancel()

        desativarCliques(camada)

        // CONTINUAR BLOQUEANDO O MENU DURANTE A ANIMAÇÃO

        camada.isClickable = true

        camada.animate()
            .alpha(0f)
            .setDuration(180L)
            .withEndAction {

                tela.removeView(camada)

                if (popupPagamento === camada) {

                    popupPagamento = null
                }
            }
            .start()
    }

    // =====================================================
    // REMOVER POP-UP ANTES DE ABRIR OUTRA TELA
    // =====================================================

    private fun removerPopupImediatamente() {

        val camada = popupPagamento ?: return

        camada.animate().cancel()

        tela.removeView(camada)

        popupPagamento = null
    }

    // =====================================================
    // EVITAR CLIQUES DUPLICADOS
    // =====================================================

    private fun desativarCliques(view: View) {

        view.isClickable = false
        view.isFocusable = false

        if (view is android.view.ViewGroup) {

            for (i in 0 until view.childCount) {

                desativarCliques(
                    view.getChildAt(i)
                )
            }
        }
    }

    // =====================================================
    // ANIMAÇÃO AO PRESSIONAR
    // =====================================================

    private fun animarClique(view: View) {

        view.setOnTouchListener { elemento, evento ->

            when (evento.actionMasked) {

                MotionEvent.ACTION_DOWN -> {

                    elemento.animate()
                        .scaleX(0.95f)
                        .scaleY(0.95f)
                        .setStartDelay(0L)
                        .setDuration(120L)
                        .start()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {

                    elemento.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setStartDelay(0L)
                        .setDuration(180L)
                        .setInterpolator(
                            DecelerateInterpolator()
                        )
                        .start()
                }
            }

            false
        }
    }

    // =====================================================
    // SOM SUAVE DE CLIQUE
    // =====================================================

    private fun tocarSomClique() {

        var player: MediaPlayer? = null

        try {

            val id = resources.getIdentifier(
                "clique_suave",
                "raw",
                packageName
            )

            if (id == 0) return

            val som = MediaPlayer.create(
                applicationContext,
                id
            ) ?: return

            player = som

            som.setVolume(
                0.45f,
                0.45f
            )

            som.setOnCompletionListener {

                it.release()
            }

            som.setOnErrorListener { mp, _, _ ->

                mp.release()

                true
            }

            som.start()

        } catch (_: Exception) {

            player?.release()
        }
    }

    // =====================================================
    // CRIAR FUNDO ARREDONDADO
    // =====================================================

    private fun criarFundo(
        cor: Int,
        raio: Float,
        corBorda: Int,
        espessura: Float
    ): GradientDrawable {

        return GradientDrawable().apply {

            shape = GradientDrawable.RECTANGLE

            setColor(cor)

            cornerRadius = raio * escala

            if (espessura > 0f) {

                setStroke(
                    (espessura * escala)
                        .toInt()
                        .coerceAtLeast(1),

                    corBorda
                )
            }
        }
    }

    // =====================================================
    // CRIAR TEXTO
    // =====================================================

    private fun criarTexto(
        conteudo: String,
        tamanho: Float,
        cor: Int,
        negrito: Boolean
    ): TextView {

        return TextView(this).apply {

            text = conteudo

            gravity = Gravity.CENTER

            includeFontPadding = false

            setTextColor(cor)

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                tamanho * escala
            )

            typeface = Typeface.create(
                "sans-serif-rounded",

                if (negrito) {
                    Typeface.BOLD
                } else {
                    Typeface.NORMAL
                }
            )
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
        altura: Int
    ): ImageView {

        val imagem = ImageView(this)

        val id = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (id != 0) {

            imagem.setImageResource(id)
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        adicionarView(
            imagem,
            x, y, largura, altura
        )

        return imagem
    }

    // =====================================================
    // TAMANHOS E POSIÇÕES
    // =====================================================

    private fun parametros(
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

            leftMargin = (x * sx).toInt()

            topMargin = (y * sy).toInt()
        }
    }

    private fun adicionarView(
        view: View,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int
    ) {

        tela.addView(
            view,
            parametros(
                x, y, largura, altura
            )
        )
    }

    // =====================================================
    // ABRIR OUTRAS TELAS
    // =====================================================

    private fun abrirTela(classe: Class<*>) {

        if (navegando || isFinishing) return

        navegando = true

        try {

            startActivity(
                Intent(this, classe)
            )

        } catch (_: Exception) {

            navegando = false

            Toast.makeText(
                this,
                "Não foi possível abrir essa tela.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =====================================================
    // BOTÃO VOLTAR
    // =====================================================

    private fun voltarTela() {

        if (popupPagamento != null) {

            tocarSomClique()

            fecharPopupPagamento()

            return
        }

        if (navegando || isFinishing) return

        navegando = true

        tocarSomClique()

        finish()
    }

    override fun onBackPressed() {

        voltarTela()
    }

    override fun onResume() {

        super.onResume()

        navegando = false
    }
}