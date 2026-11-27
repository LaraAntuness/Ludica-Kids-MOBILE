
package com.example.mobilejogos

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback

class ProveNaImaginacaoActivity : ComponentActivity() {

    private lateinit var tela: FrameLayout
    private lateinit var conteudo: FrameLayout
    private lateinit var visual: VisualJogosSensoriais

    private var escala = 1f

    private var popupAtual: FrameLayout? = null

    private var popupAberto = false
    private var navegando = false

    private var musica: MediaPlayer? = null
    private var efeito: MediaPlayer? = null

    private var animacaoBalao: ObjectAnimator? = null
    private var animacaoEstrela: ObjectAnimator? = null

    private data class Bebida(
        val imagem: String,
        val nome: String,
        val paleta: VisualJogosSensoriais.Paleta
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        aplicarModoImersivo()

        setContentView(
            R.layout.activity_prove_na_imaginacao
        )

        tela = findViewById(
            R.id.telaProveNaImaginacao
        )

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    tocarClique()

                    if (popupAberto) {
                        fecharPopup()
                    } else {
                        voltarParaCincoOpcoes()
                    }
                }
            }
        )

        tela.post {

            if (!isFinishing && !isDestroyed) {

                montarTela()
                iniciarMusica()
            }
        }
    }

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

    private fun montarTela() {

        tela.removeAllViews()

        escala = minOf(
            tela.width / 874f,
            tela.height / 393f
        ).coerceAtLeast(0.1f)

        visual = VisualJogosSensoriais(
            this,
            escala
        )

        // PLANO DE FUNDO ORIGINAL

        val fundo = visual.imagem(
            "fundosabor"
        ).apply {

            scaleType = ImageView.ScaleType.FIT_XY
        }

        tela.addView(
            fundo,
            FrameLayout.LayoutParams(-1, -1)
        )

        conteudo = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        tela.addView(
            conteudo,
            FrameLayout.LayoutParams(
                visual.px(874),
                visual.px(393),
                Gravity.CENTER
            )
        )

        // BOTÃO DE RETORNO

        val voltar = visual.imagem(
            "botaoretornoazul"
        )

        visual.colocar(
            conteudo,
            voltar,
            19, 20, 43, 43
        )

        voltar.contentDescription = "Voltar"

        visual.toque(voltar) {

            tocarClique()
            voltarParaCincoOpcoes()
        }

        // TÍTULO

        val titulo = visual.texto(
            "Bebida Favorita",
            28f,
            "#395681"
        ).apply {

            background = visual.fundo(
                "#FFFDF5",
                "#FFF1D7",
                26f,
                "#E9CD91",
                3
            )

            elevation = visual.px(5).toFloat()
        }

        visual.colocar(
            conteudo,
            titulo,
            237, 10, 400, 56
        )

        // MENININHO AUMENTADO

        visual.colocar(
            conteudo,
            visual.imagem("menino"),
            8, 155, 285, 238
        )

        // BALÃO ANIMADO

        val balao = visual.balao(
            conteudo,
            "Qual é a sua\nbebida favorita?",
            visual.azul
        )

        animacaoBalao = ObjectAnimator.ofFloat(
            balao,
            View.TRANSLATION_Y,
            0f,
            -visual.px(5).toFloat(),
            0f
        ).apply {

            duration = 2600L
            repeatCount = ValueAnimator.INFINITE
            start()
        }

        // BEBIDAS

        val bebidas = listOf(

            Bebida(
                "agua",
                "Água",
                visual.azul
            ),

            Bebida(
                "milkshake",
                "Milkshake",
                visual.rosa
            ),

            Bebida(
                "suco",
                "Suco",
                visual.amarelo
            ),

            Bebida(
                "refrigerante",
                "Refrigerante",
                visual.roxo
            )
        )

        bebidas.forEachIndexed { i, bebida ->

            val x = if (i % 2 == 0) 315 else 568

            val y = if (i < 2) 87 else 215

            visual.cartao(
                conteudo,
                bebida.imagem,
                bebida.nome,
                bebida.paleta,
                x,
                y
            ) {

                if (!popupAberto && !navegando) {

                    tocarClique()

                    mostrarPopup(bebida)
                }
            }
        }

        // NÍVEL NO RODAPÉ

        visual.progresso(
            conteudo,
            1
        )
    }

    // =========================================
    // POP-UP ROXO DE ESTRELINHAS
    // =========================================

    private fun mostrarPopup(bebida: Bebida) {

        if (popupAberto || navegando) return

        popupAberto = true

        animacaoBalao?.pause()

        tocarEfeito("brilho_suave", 0.20f)

        val camada = FrameLayout(this).apply {

            setBackgroundColor(
                Color.argb(150, 35, 25, 55)
            )

            isClickable = true
            isFocusable = true

            elevation = visual.px(50).toFloat()
        }

        popupAtual = camada

        tela.addView(
            camada,
            FrameLayout.LayoutParams(-1, -1)
        )

        val conjunto = FrameLayout(this).apply {

            clipChildren = false
            clipToPadding = false
        }

        camada.addView(
            conjunto,
            FrameLayout.LayoutParams(
                visual.px(560),
                visual.px(320),
                Gravity.CENTER
            )
        )

        // SOMBRA SÓLIDA

        val sombra = View(this).apply {

            background = visual.fundo(
                "#9873C3",
                "#7953AA",
                34f,
                "#7953AA",
                3
            )
        }

        visual.colocar(
            conjunto,
            sombra,
            5, 27, 550, 285
        )

        // CARTÃO PRINCIPAL

        val popup = FrameLayout(this).apply {

            background = visual.fundo(
                "#FFFCFF",
                "#F3E6FC",
                34f,
                "#A780D4",
                5
            )

            elevation = visual.px(6).toFloat()

            clipChildren = false
            clipToPadding = false
        }

        visual.colocar(
            conjunto,
            popup,
            5, 20, 550, 285
        )

        // BORDA INTERNA

        val borda = View(this).apply {

            background = visual.fundo(
                "#00FFFFFF",
                "#00FFFFFF",
                27f,
                "#DEC7F5",
                2
            )
        }

        visual.colocar(
            popup,
            borda,
            9, 9, 532, 267
        )

        // FAIXA ONDULADA

        visual.colocar(
            popup,
            visual.cabecalhoRoxo(),
            15, 6, 520, 75
        )

        // ESTRELA SUPERIOR

        val estrelaTopo = visual.estrela().apply {

            elevation = visual.px(15).toFloat()
        }

        visual.colocar(
            conjunto,
            estrelaTopo,
            247, -4, 66, 66
        )

        // ESTRELINHAS LATERAIS

        visual.colocar(
            popup,
            visual.estrela(),
            45, 21, 42, 42
        )

        visual.colocar(
            popup,
            visual.estrela(),
            463, 21, 42, 42
        )

        // TÍTULO DO POP-UP

        visual.colocar(
            popup,
            visual.texto(
                "Muito bem!",
                31f,
                "#61378C"
            ),
            105, 30, 340, 43
        )

        // ESTRELA GRANDE ATRÁS DA BEBIDA

        val estrelaGrande = visual.estrela()

        visual.colocar(
            popup,
            estrelaGrande,
            15, 83, 220, 190
        )

        // BEBIDA ORIGINAL

        val bebidaImagem = visual.imagem(
            bebida.imagem
        )

        visual.colocar(
            popup,
            bebidaImagem,
            65, 111, 120, 135
        )

        // TEXTO

        visual.colocar(
            popup,
            visual.texto(
                "Você escolheu:",
                20f,
                "#61378C"
            ),
            240, 88, 280, 30
        )

        // CÁPSULA COM O NOME

        val nome = visual.texto(
            bebida.nome,
            25f,
            bebida.paleta.texto
        ).apply {

            background = visual.fundo(
                bebida.paleta.topo,
                bebida.paleta.base,
                23f,
                bebida.paleta.borda,
                2
            )
        }

        visual.colocar(
            popup,
            nome,
            245, 122, 270, 43
        )

        // PRÓXIMO NÍVEL

        visual.botao(
            popup,
            "Próximo nível →",
            visual.verde,
            245, 175, 270, 51
        ) {

            tocarClique()
            abrirProximoNivel()
        }

        // VOLTAR

        visual.botao(
            popup,
            "← Voltar",
            visual.azul,
            270, 234, 220, 39
        ) {

            tocarClique()
            voltarParaCincoOpcoes()
        }

        // ANIMAÇÃO DE ENTRADA

        conjunto.scaleX = 0.78f
        conjunto.scaleY = 0.78f
        conjunto.alpha = 0f

        conjunto.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(380L)
            .setInterpolator(
                OvershootInterpolator(1.15f)
            )
            .start()

        // ESTRELA BALANÇANDO SUAVEMENTE

        animacaoEstrela = ObjectAnimator.ofFloat(
            estrelaTopo,
            View.ROTATION,
            -7f, 7f, -7f
        ).apply {

            duration = 2400L
            repeatCount = ValueAnimator.INFINITE
            start()
        }

        bebidaImagem.translationY =
            visual.px(8).toFloat()

        bebidaImagem.animate()
            .translationY(0f)
            .setDuration(550L)
            .setInterpolator(
                OvershootInterpolator(1.4f)
            )
            .start()
    }

    private fun fecharPopup() {

        animacaoEstrela?.cancel()
        animacaoEstrela = null

        popupAtual?.let {
            tela.removeView(it)
        }

        popupAtual = null
        popupAberto = false

        animacaoBalao?.resume()
    }

    private fun abrirProximoNivel() {

        if (navegando) return

        navegando = true

        startActivity(
            Intent(
                this,
                ProveNaImaginacao2Activity::class.java
            )
        )

        finish()
    }

    private fun voltarParaCincoOpcoes() {

        if (navegando) return

        navegando = true

        startActivity(
            Intent(
                this,
                CincoOpcoesDosJogosActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        )

        finish()
    }

    // =========================================
    // ÁUDIOS
    // =========================================

    private fun iniciarMusica() {

        if (musica != null) return

        try {

            musica = MediaPlayer.create(
                this,
                R.raw.opcaodesomfundo2
            )?.apply {

                isLooping = true

                setVolume(0.07f, 0.07f)

                start()
            }

        } catch (_: Exception) {
        }
    }

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

        try {

            val id = resources.getIdentifier(
                nome,
                "raw",
                packageName
            )

            if (id == 0) return

            efeito?.release()

            efeito = MediaPlayer.create(
                this, id
            )?.apply {

                setVolume(volume, volume)

                setOnCompletionListener {

                    if (efeito === it) {
                        efeito = null
                    }

                    it.release()
                }

                start()
            }

        } catch (_: Exception) {
        }
    }

    override fun onPause() {

        musica?.pause()

        animacaoBalao?.pause()
        animacaoEstrela?.pause()

        super.onPause()
    }

    override fun onResume() {

        super.onResume()

        musica?.start()

        if (!popupAberto) {
            animacaoBalao?.resume()
        }

        animacaoEstrela?.resume()
    }

    override fun onDestroy() {

        animacaoBalao?.cancel()
        animacaoEstrela?.cancel()

        musica?.release()
        efeito?.release()

        musica = null
        efeito = null

        super.onDestroy()
    }
}
