package com.example.mobilejogos

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast

class TelaInicialActivity : Activity() {

    private lateinit var root: FrameLayout

    private lateinit var girafa: ImageView
    private lateinit var botaoJogar: ImageView

    private var larguraTela = 0
    private var alturaTela = 0

    // Medidas da imagem de referência
    private val larguraBase = 874f
    private val alturaBase = 393f

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

        root.post {
            larguraTela = root.width
            alturaTela = root.height

            montarTela()
            iniciarAnimacoes()
        }
    }

    // ==========================================
    // AJUSTE PROPORCIONAL AO TAMANHO DA TELA
    // ==========================================

    private fun x(valor: Float): Int {
        return (valor / larguraBase * larguraTela).toInt()
    }

    private fun y(valor: Float): Int {
        return (valor / alturaBase * alturaTela).toInt()
    }

    private fun adicionarImagem(
        imagem: Int,
        posX: Float,
        posY: Float,
        largura: Float,
        altura: Float,
        descricao: String = ""
    ): ImageView {

        val imageView = ImageView(this).apply {

            setImageResource(imagem)

            scaleType = ImageView.ScaleType.FIT_CENTER

            contentDescription = descricao
        }

        val parametros = FrameLayout.LayoutParams(
            x(largura),
            y(altura)
        ).apply {

            leftMargin = x(posX)
            topMargin = y(posY)

            gravity = Gravity.TOP or Gravity.START
        }

        root.addView(imageView, parametros)

        return imageView
    }

    // ==========================================
    // MONTAGEM DO CENÁRIO
    // ==========================================

    private fun montarTela() {

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
        // NUVEM ESQUERDA
        // ======================================

        val nuvemEsquerda = adicionarImagem(
            R.drawable.nuveminicio,
            140f,
            55f,
            105f,
            90f,
            "Nuvem sorridente"
        )

        // ======================================
        // NUVEM DIREITA
        // ======================================

        val nuvemDireita = adicionarImagem(
            R.drawable.nuveminicio,
            640f,
            52f,
            118f,
            94f,
            "Nuvem sorridente"
        )

        // ======================================
        // NUVEM PEQUENA CENTRAL
        // ======================================

        val nuvemCentral = adicionarImagem(
            R.drawable.nuveminicio,
            278f,
            178f,
            60f,
            52f,
            "Nuvem pequena"
        )

        // ======================================
        // SOL DOS PAIS
        // ======================================

        val botaoPais = adicionarImagem(
            R.drawable.solpais,
            18f,
            6f,
            100f,
            102f,
            "Área dos pais"
        )

        botaoPais.setOnClickListener {

            Toast.makeText(
                this,
                "Área dos pais: em breve!",
                Toast.LENGTH_SHORT
            ).show()

            // Depois criaremos aqui o pop-up
            // com a calculadora para confirmar o CPF.
        }

        // ======================================
        // CABEÇA DA GIRAFINHA NO CANTO DIREITO
        // ======================================

        val cabecaGirafa = adicionarImagem(
            R.drawable.girafabotao,
            785f,
            38f,
            50f,
            50f,
            "Lúdica Kids"
        )

        // ======================================
        // GIRAFINHA PRINCIPAL
        // ======================================

        girafa = adicionarImagem(
            R.drawable.girafapescoco,
            354f,
            99f,
            165f,
            255f,
            "Girafinha do Lúdica Kids"
        )

        // ======================================
        // BOTÃO JOGAR
        // ======================================

        botaoJogar = adicionarImagem(
            R.drawable.jogarinicio,
            288f,
            306f,
            286f,
            69f,
            "Jogar"
        )

        // Deixa o botão na frente da girafinha
        botaoJogar.bringToFront()

        botaoJogar.setOnClickListener {

            botaoJogar.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(100)
                .withEndAction {

                    botaoJogar.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction {

                            startActivity(
                                Intent(
                                    this,
                                    QuemVaiJogarActivity::class.java
                                )
                            )
                        }
                        .start()
                }
                .start()
        }

        // ======================================
        // ANIMAÇÕES SUAVES DAS NUVENS
        // ======================================

        animarNuvem(nuvemEsquerda, 12f, 3500)
        animarNuvem(nuvemDireita, -15f, 4200)
        animarNuvem(nuvemCentral, 8f, 3000)

        // ======================================
        // SOL COM MOVIMENTO DELICADO
        // ======================================

        botaoPais.animate()
            .rotation(4f)
            .setDuration(2500)
            .setInterpolator(
                AccelerateDecelerateInterpolator()
            )
            .withEndAction {

                botaoPais.animate()
                    .rotation(-4f)
                    .setDuration(2500)
                    .start()
            }
            .start()

        // Pequena animação da cabeça no canto
        cabecaGirafa.animate()
            .rotation(8f)
            .setDuration(1200)
            .withEndAction {

                cabecaGirafa.animate()
                    .rotation(0f)
                    .setDuration(1200)
                    .start()
            }
            .start()
    }

    // ==========================================
    // ANIMAÇÃO DAS NUVENS
    // ==========================================

    private fun animarNuvem(
        nuvem: ImageView,
        distancia: Float,
        duracao: Long
    ) {

        nuvem.animate()
            .translationX(x(distancia).toFloat())
            .setDuration(duracao)
            .setInterpolator(
                AccelerateDecelerateInterpolator()
            )
            .withEndAction {

                nuvem.animate()
                    .translationX(x(distancia).toFloat())
                    .setDuration(duracao)
                    .setInterpolator(
                        AccelerateDecelerateInterpolator()
                    )
                    .withEndAction {

                        animarNuvem(
                            nuvem,
                            distancia,
                            duracao
                        )
                    }
                    .start()
            }
            .start()
    }

    // ==========================================
    // ANIMAÇÃO PRINCIPAL DA GIRAFINHA
    // ==========================================

    private fun iniciarAnimacoes() {

        // A girafa começa escondida atrás do botão
        girafa.translationY = y(210f).toFloat()

        // Sobe e aparece como no vídeo
        girafa.animate()
            .translationY(0f)
            .setDuration(1700)
            .setStartDelay(400)
            .setInterpolator(
                OvershootInterpolator(0.7f)
            )
            .withEndAction {

                // Depois faz um movimento suave
                animarGirafaParada()
            }
            .start()

        // Botão aparece suavemente
        botaoJogar.alpha = 0f

        botaoJogar.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(400)
            .start()
    }

    // ==========================================
    // GIRAFINHA RESPIRANDO SUAVEMENTE
    // ==========================================

    private fun animarGirafaParada() {

        girafa.animate()
            .translationY(-y(5f).toFloat())
            .rotation(1.5f)
            .setDuration(1600)
            .setInterpolator(
                AccelerateDecelerateInterpolator()
            )
            .withEndAction {

                girafa.animate()
                    .translationY(0f)
                    .rotation(-1.5f)
                    .setDuration(1600)
                    .setInterpolator(
                        AccelerateDecelerateInterpolator()
                    )
                    .withEndAction {

                        animarGirafaParada()
                    }
                    .start()
            }
            .start()
    }
}