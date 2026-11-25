
package com.example.mobilejogos

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class TelaInicialActivity : Activity() {

    // ==========================================
    // ELEMENTOS PRINCIPAIS
    // ==========================================

    private lateinit var root: FrameLayout
    private lateinit var girafa: ImageView
    private lateinit var botaoJogar: ImageView

    private var larguraTela = 0
    private var alturaTela = 0

    private val larguraBase = 874f
    private val alturaBase = 393f

    private var popupAberto = false
    private var verificandoSessao = false

    // ==========================================
    // CORES DO GLITTER
    // ==========================================

    private val coresEstrelas = listOf(
        Color.rgb(255, 211, 85),
        Color.rgb(255, 233, 152),
        Color.rgb(120, 172, 237),
        Color.WHITE,
        Color.rgb(255, 183, 198)
    )

    // ==========================================
    // SONS E ANIMAÇÕES
    // ==========================================

    private val sonsAtivos = mutableListOf<MediaPlayer>()
    private var audioAmbiente: MediaPlayer? = null

    private val animacoesContinuas =
        mutableListOf<ValueAnimator>()

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

    private fun x(valor: Float): Int =
        (valor / larguraBase * larguraTela).toInt()

    private fun y(valor: Float): Int =
        (valor / alturaBase * alturaTela).toInt()

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

    // ==========================================
    // ADICIONAR IMAGENS
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
    // SONS
    // ==========================================

    private fun tocarSom(
        recurso: Int,
        volume: Float,
        duracaoMaximaMs: Long? = null
    ) {
        val player = MediaPlayer.create(
            this,
            recurso
        ) ?: return

        sonsAtivos.add(player)
        player.setVolume(volume, volume)

        player.setOnCompletionListener {
            sonsAtivos.remove(it)
            it.release()
        }

        player.start()

        if (duracaoMaximaMs != null) {
            root.postDelayed({
                encerrarSomComFade(player, volume)
            }, duracaoMaximaMs)
        }
    }

    private fun encerrarSomComFade(
        player: MediaPlayer,
        volumeInicial: Float
    ) {
        if (!sonsAtivos.contains(player)) return

        ValueAnimator.ofFloat(
            volumeInicial,
            0f
        ).apply {
            duration = 200L

            addUpdateListener {
                if (sonsAtivos.contains(player)) {
                    try {
                        val volume =
                            it.animatedValue as Float

                        player.setVolume(volume, volume)
                    } catch (_: Exception) {
                    }
                }
            }

            addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(
                        animation: Animator
                    ) {
                        if (sonsAtivos.remove(player)) {
                            try {
                                player.stop()
                            } catch (_: Exception) {
                            }
                            try {
                                player.release()
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            )

            start()
        }
    }

    private fun somClique() {
        tocarSom(R.raw.clique_suave, 0.45f)
    }

    private fun somMagico() {
        // O som mágico foi transferido para
        // o login realizado com sucesso.
        somClique()
    }

    private fun somBrilho() {
        tocarSom(R.raw.brilho_suave, 0.25f)
    }

    // ==========================================
    // MÚSICA AMBIENTE
    // ==========================================

    private fun iniciarSomAmbiente() {
        if (audioAmbiente == null) {
            audioAmbiente = MediaPlayer.create(
                this,
                R.raw.fundo
            )?.apply {
                isLooping = true
                setVolume(0.17f, 0.17f)
            }
        }

        try {
            if (audioAmbiente?.isPlaying != true) {
                audioAmbiente?.start()
            }
        } catch (_: Exception) {
        }
    }

    override fun onResume() {
        super.onResume()
        iniciarSomAmbiente()
    }

    override fun onPause() {
        try {
            audioAmbiente?.pause()
        } catch (_: Exception) {
        }
        super.onPause()
    }

    // ==========================================
    // AUTENTICAÇÃO
    // ==========================================

    private fun usuarioEstaLogado(): Boolean {
        return LudicaSessao.obter(this) != null
    }

    // ==========================================
    // POP-UP DE LOGIN
    // ==========================================

    private fun abrirPopupLogin() {
        if (popupAberto || isFinishing || isDestroyed) {
            return
        }

        popupAberto = true

        // Impede reutilizar token temporário antigo.
        LoginDialog.encerrarSessao()

        // ESCONDE O BOTÃO JOGAR E A GIRAFINHA.
        botaoJogar.visibility = View.INVISIBLE
        girafa.visibility = View.INVISIBLE

        LoginDialog(this) {
            // EXECUTA QUANDO O POP-UP FECHA.
            popupAberto = false

            botaoJogar.visibility = View.VISIBLE
            girafa.visibility = View.VISIBLE

            val token = LoginDialog.tokenDaSessao()
            val id = LoginDialog.idDaSessao()

            if (
                !token.isNullOrBlank() &&
                !id.isNullOrBlank()
            ) {
                try {
                    LudicaSessao.salvar(
                        this,
                        token,
                        id
                    )

                    if (!isFinishing && !isDestroyed) {
                        abrirTelaJogos()
                    }
                } catch (_: Exception) {
                    Toast.makeText(
                        this,
                        "Não foi possível guardar sua sessão.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.mostrar()
    }

    // ==========================================
    // VERIFICAR LOGIN
    // ==========================================

    private fun verificarAcesso(
        aoEstarLogado: () -> Unit
    ) {
        if (popupAberto || verificandoSessao) return

        val sessao = LudicaSessao.obter(this)

        if (sessao == null) {
            abrirPopupLogin()
            return
        }

        verificandoSessao = true

        // CONFIRMA SE O TOKEN CONTINUA VÁLIDO.
        ApiService.listarCriancas(
            sessao.second,
            sessao.first
        ) { resultado ->

            verificandoSessao = false

            if (isFinishing || isDestroyed) {
                return@listarCriancas
            }

            resultado.onSuccess {
                aoEstarLogado()
            }

            resultado.onFailure { falha ->
                val codigo =
                    (falha as? ApiService.ApiException)
                        ?.statusCode

                if (codigo == 401 || codigo == 403) {
                    LudicaSessao.limpar(this)
                    LoginDialog.encerrarSessao()
                    abrirPopupLogin()
                } else {
                    Toast.makeText(
                        this,
                        "Sem conexão para verificar sua sessão. Tente novamente.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // ==========================================
    // ABRIR TELA INICIAL DOS JOGOS
    // ==========================================

    private fun abrirTelaJogos() {
        if (isFinishing || isDestroyed) return

        startActivity(
            Intent(
                this,
                TelaInicialJogosGeral::class.java
            )
        )
    }

    // ==========================================
    // ABRIR QUEM VAI JOGAR
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
    // ÁREA DOS PAIS
    // ==========================================

    private fun mostrarPopupCpf() {
        AlertDialog.Builder(this)
            .setTitle("Área dos responsáveis")
            .setMessage(
                "Em breve, vamos confirmar o CPF do responsável para liberar esta área!"
            )
            .setPositiveButton("ENTENDI") { dialog, _ ->
                somClique()
                dialog.dismiss()
            }
            .show()
    }

    // ==========================================
    // MONTAR CENÁRIO
    // ==========================================

    private fun montarTela() {
        animacoesContinuas.forEach {
            it.cancel()
        }
        animacoesContinuas.clear()

        root.removeAllViews()

        // GRAMA E MORRINHOS
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

        // NUVENS
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
        // SOL DOS PAIS
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
            somClique()

            animarEstrelinhas(
                botaoPais,
                18,
                45,
                125
            )

            botaoPais.pivotX = botaoPais.width / 2f
            botaoPais.pivotY = botaoPais.height / 2f
            botaoPais.rotation = 0f

            val giro = ObjectAnimator.ofFloat(
                botaoPais,
                View.ROTATION,
                0f,
                360f
            ).apply {
                duration = 850L
                interpolator =
                    AccelerateDecelerateInterpolator()
            }

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
        // BOTÃO DA GIRAFINHA
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
            somClique()

            animarEstrelinhas(
                cabecaGirafa,
                16,
                38,
                100
            )

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

            // AGORA USA O CLIQUE SUAVE
            somMagico()

            animarEstrelinhas(
                botaoJogar,
                28,
                70,
                175
            )

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
                                abrirTelaJogos()
                            }
                        }
                        .start()
                }
                .start()
        }

        // NUVENS ANIMADAS
        animarNuvem(nuvemEsquerda, 12f, 3500L)
        animarNuvem(nuvemDireita, -15f, 4200L)
        animarNuvem(nuvemCentral, 8f, 3000L)
    }

    // ==========================================
    // ANIMAÇÃO DAS NUVENS
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
    // ANIMAÇÃO DE ENTRADA
    // ==========================================

    private fun iniciarAnimacoes() {
        val distanciaInicial = y(210f).toFloat()

        girafa.translationY = distanciaInicial
        botaoJogar.translationY = distanciaInicial

        girafa.alpha = 1f
        botaoJogar.alpha = 1f

        root.postDelayed({
            if (!isFinishing && !isDestroyed) {
                somBrilho()
            }
        }, 400)

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
    // GLITTER
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

            val distancia = x(
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
    // LIBERAR RECURSOS
    // ==========================================

    override fun onDestroy() {
        animacoesContinuas.forEach {
            it.cancel()
        }
        animacoesContinuas.clear()

        if (::root.isInitialized) {
            for (i in 0 until root.childCount) {
                root.getChildAt(i)
                    .animate()
                    .cancel()
            }
        }

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

        try {
            audioAmbiente?.release()
        } catch (_: Exception) {
        }
        audioAmbiente = null

        super.onDestroy()
    }
}
