
package com.example.mobilejogos

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TelaInicialActivity : Activity() {

    // ==========================================
    // ELEMENTOS DA TELA
    // ==========================================

    private lateinit var root: FrameLayout
    private lateinit var girafa: ImageView
    private lateinit var botaoJogar: ImageView

    private var larguraTela = 0
    private var alturaTela = 0

    private val larguraBase = 874f
    private val alturaBase = 393f

    // ==========================================
    // CORES DO LÚDICA KIDS
    // ==========================================

    private val azul = Color.rgb(95, 145, 207)
    private val azulEscuro = Color.rgb(43, 84, 145)
    private val azulClaro = Color.rgb(232, 244, 255)
    private val amarelo = Color.rgb(255, 221, 139)

    private val coresEstrelas = listOf(
        Color.rgb(255, 211, 85),
        Color.rgb(255, 233, 152),
        Color.rgb(120, 172, 237),
        Color.WHITE,
        Color.rgb(255, 183, 198)
    )

    // ==========================================
    // CONTROLE DE SOM E ANIMAÇÕES
    // ==========================================

    private val sonsAtivos = mutableListOf<MediaPlayer>()
    private val animacoesContinuas = mutableListOf<ValueAnimator>()

    private var popupAberto = false

    // ==========================================
    // INICIAR TELA
    // ==========================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_tela_inicial)

        root = findViewById(R.id.telaInicialRoot)

        root.setBackgroundColor(
            Color.rgb(205, 228, 242)
        )

        root.clipChildren = false
        root.clipToPadding = false

        root.post {
            if (!isFinishing && !isDestroyed) {
                larguraTela = root.width
                alturaTela = root.height

                montarTela()
                iniciarAnimacoes()
            }
        }
    }

    // ==========================================
    // MEDIDAS PROPORCIONAIS
    // ==========================================

    private fun x(valor: Float): Int {
        return (valor / larguraBase * larguraTela).toInt()
    }

    private fun y(valor: Float): Int {
        return (valor / alturaBase * alturaTela).toInt()
    }

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    // ==========================================
    // FUNDOS ARREDONDADOS
    // ==========================================

    private fun fundoArredondado(
        cor: Int,
        raio: Int,
        borda: Int? = null
    ): GradientDrawable {

        return GradientDrawable().apply {
            cornerRadius = dp(raio).toFloat()
            setColor(cor)

            if (borda != null) {
                setStroke(dp(2), borda)
            }
        }
    }

    // ==========================================
    // ADICIONAR IMAGEM
    // ==========================================

    private fun adicionarImagem(
        imagem: Int,
        posX: Float,
        posY: Float,
        largura: Float,
        altura: Float,
        descricao: String
    ): ImageView {

        val imagemView = ImageView(this).apply {
            setImageResource(imagem)
            scaleType = ImageView.ScaleType.FIT_CENTER
            contentDescription = descricao
        }

        root.addView(
            imagemView,
            FrameLayout.LayoutParams(
                x(largura),
                y(altura)
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                leftMargin = x(posX)
                topMargin = y(posY)
            }
        )

        return imagemView
    }

    // ==========================================
    // REPRODUÇÃO DOS SONS
    // ==========================================

    private fun tocarSom(
        recurso: Int,
        volume: Float = 0.30f
    ) {

        val player = MediaPlayer.create(this, recurso) ?: return

        sonsAtivos.add(player)

        player.setVolume(volume, volume)

        player.setOnCompletionListener {
            sonsAtivos.remove(it)
            it.release()
        }

        player.start()
    }

    // SOM DO SOL E DA GIRAFINHA
    private fun somClique() {
        tocarSom(R.raw.clique_suave, 0.45f)
    }

    // SOM MÁGICO DO BOTÃO JOGAR
    private fun somMagico() {
        tocarSom(R.raw.sominicio, 0.50f)
    }

    // SOM DA ENTRADA DA GIRAFINHA
    private fun somBrilho() {
        tocarSom(R.raw.brilho_suave, 0.25f)
    }

    // ==========================================
    // VERIFICAÇÃO DE LOGIN
    // ==========================================

    private fun usuarioEstaLogado(): Boolean {

        // Ainda não criamos o sistema de login.
        // O acesso continuará bloqueado até
        // implementarmos a autenticação real.
        return false
    }

    private fun verificarAcesso(
        acao: () -> Unit
    ) {

        if (popupAberto) return

        if (usuarioEstaLogado()) {
            acao()
        } else {
            mostrarPopupLogin()
        }
    }

    // ==========================================
    // NAVEGAÇÃO
    // ==========================================

    private fun abrirQuemVaiJogar() {

        startActivity(
            Intent(
                this,
                QuemVaiJogarActivity::class.java
            )
        )
    }

    // ==========================================
    // MONTAR TELA INICIAL
    // ==========================================

    private fun montarTela() {

        animacoesContinuas.forEach {
            it.cancel()
        }

        animacoesContinuas.clear()
        root.removeAllViews()

        // ======================================
        // GRAMA E MORRINHOS
        // ======================================

        val grama = ImageView(this).apply {
            setImageResource(R.drawable.gramainicio)
            scaleType = ImageView.ScaleType.FIT_XY
        }

        root.addView(
            grama,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                y(285f)
            ).apply {
                gravity = Gravity.BOTTOM
            }
        )

        // ======================================
        // NUVENS
        // ======================================

        val nuvemEsquerda = adicionarImagem(
            R.drawable.nuveminicio,
            140f, 55f, 105f, 90f,
            "Nuvem esquerda"
        )

        val nuvemDireita = adicionarImagem(
            R.drawable.nuveminicio,
            640f, 52f, 118f, 94f,
            "Nuvem direita"
        )

        val nuvemCentral = adicionarImagem(
            R.drawable.nuveminicio,
            278f, 178f, 60f, 52f,
            "Nuvem pequena"
        )

        // ======================================
        // BOTÃO SOL DOS PAIS
        // ======================================

        val botaoPais = adicionarImagem(
            R.drawable.solpais,
            18f, 6f, 100f, 102f,
            "Área dos pais"
        )

        botaoPais.setOnClickListener {

            if (!botaoPais.isEnabled || popupAberto) {
                return@setOnClickListener
            }

            botaoPais.isEnabled = false

            // SOM DE CLIQUE SUAVE
            somClique()

            // GLITTER
            animarEstrelinhas(
                botaoPais,
                18,
                45,
                125
            )

            // SOL GIRA NO PRÓPRIO EIXO
            botaoPais.pivotX = botaoPais.width / 2f
            botaoPais.pivotY = botaoPais.height / 2f

            botaoPais.rotation = 0f

            val giro = ObjectAnimator.ofFloat(
                botaoPais,
                View.ROTATION,
                0f,
                360f
            )

            giro.duration = 850L
            giro.interpolator =
                AccelerateDecelerateInterpolator()

            giro.addListener(
                object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(
                        animation: Animator
                    ) {

                        botaoPais.rotation = 0f
                        botaoPais.isEnabled = true

                        if (!isFinishing && !isDestroyed) {
                            verificarAcesso {
                                mostrarPopupCpf()
                            }
                        }
                    }
                }
            )

            giro.start()
        }

        // ======================================
        // BOTÃO DA GIRAFINHA NO CANTO
        // ======================================

        val cabecaGirafa = adicionarImagem(
            R.drawable.girafabotao,
            785f, 38f, 50f, 50f,
            "Quem vai jogar?"
        )

        cabecaGirafa.setOnClickListener {

            if (!cabecaGirafa.isEnabled || popupAberto) {
                return@setOnClickListener
            }

            cabecaGirafa.isEnabled = false

            // SOM DE CLIQUE SUAVE
            somClique()

            // BRILHINHOS
            animarEstrelinhas(
                cabecaGirafa,
                16,
                38,
                100
            )

            // ANIMAÇÃO DA CABECINHA
            cabecaGirafa.animate()
                .rotation(-15f)
                .translationY(-y(7f).toFloat())
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(150)
                .withEndAction {

                    cabecaGirafa.animate()
                        .rotation(13f)
                        .translationY(y(3f).toFloat())
                        .setDuration(180)
                        .withEndAction {

                            cabecaGirafa.animate()
                                .rotation(0f)
                                .translationY(0f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(180)
                                .withEndAction {

                                    cabecaGirafa.isEnabled = true

                                    verificarAcesso {
                                        abrirQuemVaiJogar()
                                    }
                                }
                                .start()
                        }
                        .start()
                }
                .start()
        }

        // ======================================
        // GIRAFINHA PRINCIPAL
        // ======================================

        girafa = adicionarImagem(
            R.drawable.girafapescoco,
            354f, 99f, 165f, 255f,
            "Girafinha do Lúdica Kids"
        )

        // ======================================
        // BOTÃO JOGAR
        // ======================================

        botaoJogar = adicionarImagem(
            R.drawable.jogarinicio,
            288f, 306f, 286f, 69f,
            "Jogar"
        )

        botaoJogar.bringToFront()

        botaoJogar.setOnClickListener {

            if (!botaoJogar.isEnabled || popupAberto) {
                return@setOnClickListener
            }

            botaoJogar.isEnabled = false

            // SOM MÁGICO ESCOLHIDO
            somMagico()

            // GLITTER
            animarEstrelinhas(
                botaoJogar,
                28,
                70,
                175
            )

            // ANIMAÇÃO DE CLIQUE
            botaoJogar.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(120)
                .withEndAction {

                    botaoJogar.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(160)
                        .withEndAction {

                            botaoJogar.isEnabled = true

                            verificarAcesso {
                                abrirQuemVaiJogar()
                            }
                        }
                        .start()
                }
                .start()
        }

        // ======================================
        // ANIMAÇÃO DAS NUVENS
        // ======================================

        animarNuvem(
            nuvemEsquerda,
            12f,
            3500L
        )

        animarNuvem(
            nuvemDireita,
            -15f,
            4200L
        )

        animarNuvem(
            nuvemCentral,
            8f,
            3000L
        )
    }

    // ==========================================
    // ANIMAR NUVENS
    // ==========================================

    private fun animarNuvem(
        nuvem: ImageView,
        distancia: Float,
        duracao: Long
    ) {

        val deslocamento = x(distancia).toFloat()

        val animacao = ObjectAnimator.ofFloat(
            nuvem,
            View.TRANSLATION_X,
            -deslocamento,
            deslocamento
        ).apply {

            duration = duracao
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE

            interpolator =
                AccelerateDecelerateInterpolator()
        }

        animacoesContinuas.add(animacao)
        animacao.start()
    }

    // ==========================================
    // GIRAFINHA E BOTÃO SOBEM JUNTOS
    // ==========================================

    private fun iniciarAnimacoes() {

        val distanciaInicial = y(210f).toFloat()

        girafa.translationY = distanciaInicial
        botaoJogar.translationY = distanciaInicial

        girafa.alpha = 1f
        botaoJogar.alpha = 1f

        // SOM DELICADO DA ENTRADA
        root.postDelayed({

            if (!isFinishing && !isDestroyed) {
                somBrilho()
            }

        }, 400)

        // ANIMAÇÃO DA GIRAFINHA
        girafa.animate()
            .translationY(0f)
            .setDuration(1700)
            .setStartDelay(400)
            .setInterpolator(
                OvershootInterpolator(0.7f)
            )
            .withEndAction {

                animarEstrelinhas(
                    girafa,
                    24,
                    80,
                    190
                )

                animarGirafaParada()
            }
            .start()

        // BOTÃO SOBE JUNTO
        botaoJogar.animate()
            .translationY(0f)
            .setDuration(1700)
            .setStartDelay(400)
            .setInterpolator(
                OvershootInterpolator(0.7f)
            )
            .start()
    }

    // ==========================================
    // GIRAFINHA RESPIRANDO
    // ==========================================

    private fun animarGirafaParada() {

        val animacao = ObjectAnimator.ofFloat(
            girafa,
            View.TRANSLATION_Y,
            0f,
            -y(5f).toFloat()
        ).apply {

            duration = 1600L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE

            interpolator =
                AccelerateDecelerateInterpolator()
        }

        animacoesContinuas.add(animacao)
        animacao.start()
    }

    // ==========================================
    // GLITTER E BRILHINHOS
    // ==========================================

    private fun animarEstrelinhas(
        alvo: View,
        quantidade: Int,
        distanciaMinima: Int,
        distanciaMaxima: Int
    ) {

        val posicaoAlvo = IntArray(2)
        val posicaoRoot = IntArray(2)

        alvo.getLocationOnScreen(posicaoAlvo)
        root.getLocationOnScreen(posicaoRoot)

        val centroX =
            posicaoAlvo[0] -
                    posicaoRoot[0] +
                    alvo.width / 2f

        val centroY =
            posicaoAlvo[1] -
                    posicaoRoot[1] +
                    alvo.height / 2f

        repeat(quantidade) { indice ->

            val estrela = TextView(this).apply {

                text = if (indice % 3 == 0) "✦" else "★"

                textSize =
                    Random.nextInt(12, 24).toFloat()

                setTextColor(
                    coresEstrelas.random()
                )

                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD

                elevation = dp(12).toFloat()
            }

            val tamanho = dp(35)

            root.addView(
                estrela,
                FrameLayout.LayoutParams(
                    tamanho,
                    tamanho
                ).apply {

                    leftMargin =
                        (centroX - tamanho / 2f).toInt()

                    topMargin =
                        (centroY - tamanho / 2f).toInt()
                }
            )

            val angulo =
                (2.0 * Math.PI * indice / quantidade) +
                        Random.nextDouble(-0.25, 0.25)

            val distancia =
                x(
                    Random.nextInt(
                        distanciaMinima,
                        distanciaMaxima
                    ).toFloat()
                ).toDouble()

            estrela.scaleX = 0.15f
            estrela.scaleY = 0.15f
            estrela.alpha = 1f

            estrela.animate()
                .translationX(
                    (cos(angulo) * distancia).toFloat()
                )
                .translationY(
                    (sin(angulo) * distancia).toFloat()
                )
                .rotation(
                    Random.nextFloat() * 360f
                )
                .scaleX(1.15f)
                .scaleY(1.15f)
                .alpha(0f)
                .setDuration(
                    Random.nextLong(650, 1050)
                )
                .setInterpolator(
                    AccelerateDecelerateInterpolator()
                )
                .withEndAction {
                    root.removeView(estrela)
                }
                .start()
        }
    }

    // ==========================================
    // POP-UP INFANTIL DE LOGIN
    // ==========================================

    private fun mostrarPopupLogin() {

        if (popupAberto) return

        popupAberto = true

        val dialog = Dialog(this)

        val larguraTela =
            resources.displayMetrics.widthPixels

        val alturaTela =
            resources.displayMetrics.heightPixels

        val scroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = false
            isFillViewport = false
            clipChildren = false
            clipToPadding = false
        }

        val container = FrameLayout(this).apply {
            clipChildren = false
            clipToPadding = false
        }

        scroll.addView(
            container,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // MOLDURA AZUL
        val molduraAzul = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            background = fundoArredondado(
                azul,
                30
            )

            setPadding(
                dp(6),
                dp(6),
                dp(6),
                dp(6)
            )
        }

        container.addView(
            molduraAzul,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {

                leftMargin = dp(8)
                rightMargin = dp(8)
                topMargin = dp(45)
                bottomMargin = dp(10)
            }
        )

        // MOLDURA AMARELA
        val molduraAmarela = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            background = fundoArredondado(
                amarelo,
                27
            )

            setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
            )
        }

        molduraAzul.addView(
            molduraAmarela,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // INTERIOR
        val conteudo = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL

            setPadding(
                dp(22),
                dp(52),
                dp(22),
                dp(18)
            )

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.WHITE,
                    azulClaro
                )
            ).apply {
                cornerRadius = dp(24).toFloat()
            }
        }

        molduraAmarela.addView(
            conteudo,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // GIRAFINHA
        val fundoGirafa = FrameLayout(this).apply {

            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL

                setColor(
                    Color.rgb(255, 245, 216)
                )

                setStroke(
                    dp(3),
                    Color.WHITE
                )
            }

            elevation = dp(15).toFloat()
        }

        container.addView(
            fundoGirafa,
            FrameLayout.LayoutParams(
                dp(105),
                dp(105)
            ).apply {
                gravity =
                    Gravity.TOP or Gravity.CENTER_HORIZONTAL
            }
        )

        val imagemGirafa = ImageView(this).apply {

            setImageResource(
                R.drawable.girafapescoco
            )

            scaleType =
                ImageView.ScaleType.FIT_CENTER
        }

        fundoGirafa.addView(
            imagemGirafa,
            FrameLayout.LayoutParams(
                dp(88),
                dp(94),
                Gravity.CENTER
            )
        )

        // ESTRELINHA ESQUERDA
        val estrelaEsquerda = TextView(this).apply {
            text = "✦"
            textSize = 27f
            setTextColor(amarelo)
            gravity = Gravity.CENTER
            elevation = dp(15).toFloat()
        }

        container.addView(
            estrelaEsquerda,
            FrameLayout.LayoutParams(
                dp(40),
                dp(40)
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                topMargin = dp(76)
                leftMargin = dp(33)
            }
        )

        // ESTRELINHA DIREITA
        val estrelaDireita = TextView(this).apply {
            text = "✦"
            textSize = 25f
            setTextColor(azul)
            gravity = Gravity.CENTER
            elevation = dp(15).toFloat()
        }

        container.addView(
            estrelaDireita,
            FrameLayout.LayoutParams(
                dp(40),
                dp(40)
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                topMargin = dp(82)
                rightMargin = dp(33)
            }
        )

        // TÍTULO
        val titulo = TextView(this).apply {
            text = "Obaaa! Vamos brincar?"
            textSize = 22f
            setTextColor(azulEscuro)

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )

            gravity = Gravity.CENTER
        }

        conteudo.addView(
            titulo,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // MENSAGEM
        val mensagem = TextView(this).apply {

            text =
                "Antes de começar a aventura, peça para um responsável entrar ou criar uma conta!"

            textSize = 14f

            setTextColor(
                Color.rgb(89, 110, 141)
            )

            gravity = Gravity.CENTER

            setLineSpacing(
                dp(2).toFloat(),
                1f
            )
        }

        conteudo.addView(
            mensagem,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(9)
            }
        )

        // PLAQUINHA
        val plaquinha = TextView(this).apply {

            text = "Uma aventura especial espera por você!"

            textSize = 12f

            setTextColor(azulEscuro)

            gravity = Gravity.CENTER

            setPadding(
                dp(14),
                dp(8),
                dp(14),
                dp(8)
            )

            background = fundoArredondado(
                Color.rgb(255, 244, 211),
                18
            )
        }

        conteudo.addView(
            plaquinha,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(13)
            }
        )

        // BOTÃO ENTENDI
        val botaoFechar = TextView(this).apply {

            text = "ENTENDI!"

            textSize = 17f
            setTextColor(Color.WHITE)

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )

            gravity = Gravity.CENTER
            elevation = dp(4).toFloat()

            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.rgb(112, 175, 239),
                    Color.rgb(70, 129, 208)
                )
            ).apply {
                cornerRadius = dp(25).toFloat()
            }

            isClickable = true
            isFocusable = true

            setOnClickListener {
                somClique()
                dialog.dismiss()
            }
        }

        conteudo.addView(
            botaoFechar,
            LinearLayout.LayoutParams(
                dp(210),
                dp(46)
            ).apply {
                topMargin = dp(17)
            }
        )

        dialog.setContentView(scroll)

        dialog.setOnDismissListener {
            popupAberto = false
        }

        dialog.show()

        dialog.window?.apply {

            setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )

            addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )

            setDimAmount(0.50f)
            setGravity(Gravity.CENTER)

            setLayout(
                minOf(
                    dp(460),
                    (larguraTela * 0.88f).toInt()
                ),
                minOf(
                    dp(365),
                    (alturaTela * 0.92f).toInt()
                )
            )
        }

        // ANIMAÇÃO DE ENTRADA
        container.scaleX = 0.85f
        container.scaleY = 0.85f
        container.alpha = 0f

        container.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(300)
            .setInterpolator(
                OvershootInterpolator(0.5f)
            )
            .start()

        // PULINHO DA GIRAFINHA
        fundoGirafa.translationY = dp(10).toFloat()

        fundoGirafa.animate()
            .translationY(0f)
            .setDuration(450)
            .start()

        // ESTRELINHAS DECORATIVAS
        estrelaEsquerda.alpha = 0f
        estrelaDireita.alpha = 0f

        estrelaEsquerda.animate()
            .alpha(1f)
            .rotation(20f)
            .setDuration(450)
            .start()

        estrelaDireita.animate()
            .alpha(1f)
            .rotation(-20f)
            .setDuration(450)
            .start()
    }

    // ==========================================
    // POP-UP PROVISÓRIO DE CPF
    // ==========================================

    private fun mostrarPopupCpf() {

        val dialog = Dialog(this)

        val conteudo = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            setPadding(
                dp(25),
                dp(22),
                dp(25),
                dp(22)
            )

            background = fundoArredondado(
                azulClaro,
                28,
                azul
            )
        }

        val titulo = TextView(this).apply {
            text = "Área dos responsáveis"
            textSize = 22f
            setTextColor(azulEscuro)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        conteudo.addView(titulo)

        val mensagem = TextView(this).apply {

            text =
                "Em breve, vamos confirmar o CPF do responsável para liberar esta área!"

            textSize = 15f
            setTextColor(azulEscuro)
            gravity = Gravity.CENTER

            setPadding(
                dp(10),
                dp(15),
                dp(10),
                dp(15)
            )
        }

        conteudo.addView(mensagem)

        val fechar = TextView(this).apply {

            text = "ENTENDI!"
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD

            background = fundoArredondado(
                azul,
                22
            )

            setOnClickListener {
                somClique()
                dialog.dismiss()
            }
        }

        conteudo.addView(
            fechar,
            LinearLayout.LayoutParams(
                dp(190),
                dp(46)
            )
        )

        dialog.setContentView(conteudo)
        dialog.show()

        dialog.window?.apply {

            setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )

            setLayout(
                minOf(
                    dp(420),
                    (resources.displayMetrics.widthPixels * 0.85f).toInt()
                ),
                WindowManager.LayoutParams.WRAP_CONTENT
            )

            setGravity(Gravity.CENTER)
            setDimAmount(0.5f)

            addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )
        }
    }

    // ==========================================
    // LIBERAR RECURSOS
    // ==========================================

    override fun onDestroy() {

        animacoesContinuas.forEach {
            it.cancel()
        }

        animacoesContinuas.clear()

        sonsAtivos.toList().forEach { player ->

            try {
                player.stop()
            } catch (_: Exception) {
            }

            try {
                player.release()
            } catch (_: Exception) {
            }
        }

        sonsAtivos.clear()

        super.onDestroy()
    }
}
