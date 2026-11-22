
package com.example.mobilejogos

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class IdentificandoEmocoesActivity : Activity() {

    private lateinit var tela: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 393f

    private var popupAberto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_emocoes)

        tela = findViewById(R.id.telaEmocoes)

        tela.post {
            montarTela()
        }
    }

    // ==========================================
    // MONTAR TELA
    // ==========================================

    private fun montarTela() {

        tela.removeAllViews()
        popupAberto = false

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // PLANO DE FUNDO
        adicionarImagem(
            R.drawable.planodefundodasemocoes,
            0, 0, 874, 393,
            sx, sy,
            ImageView.ScaleType.FIT_XY
        )

        // GIRAFINHA FELIZ
        adicionarImagem(
            R.drawable.girafafeliz,
            352, 195, 175, 205,
            sx, sy
        )

        // TÍTULO
        adicionarImagem(
            R.drawable.tituloemocoesatualizado,
            205, 2, 490, 85,
            sx, sy
        )

        // INSTRUÇÃO
        adicionarImagem(
            R.drawable.indicacaodeapertarnanuvem,
            230, 91, 430, 42,
            sx, sy
        )

        // BOTÃO VOLTAR
        val voltar = adicionarImagem(
            R.drawable.botaoretornoverde,
            19, 25, 40, 40,
            sx, sy
        )

        voltar.isClickable = true
        voltar.contentDescription = "Voltar aos jogos"

        voltar.setOnClickListener {
            voltarParaJogos()
        }

        // NUVEM BRAVA
        criarNuvem(
            R.drawable.nuvembrava,
            "brava",
            82, 138, 140, 93,
            sx, sy
        )

        // NUVEM FELIZ - CORRETA
        criarNuvem(
            R.drawable.nuvemfeliz,
            "feliz",
            232, 138, 140, 93,
            sx, sy
        )

        // NUVEM TRISTE
        criarNuvem(
            R.drawable.nuvemtriste,
            "triste",
            514, 138, 140, 93,
            sx, sy
        )

        // NUVEM DÚVIDA
        criarNuvem(
            R.drawable.nuvemduvida,
            "duvida",
            666, 138, 140, 93,
            sx, sy
        )
    }

    // ==========================================
    // CRIAR NUVENS
    // ==========================================

    private fun criarNuvem(
        recurso: Int,
        emocao: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ) {

        val nuvem = adicionarImagem(
            recurso,
            x, y, largura, altura,
            sx, sy
        )

        nuvem.isClickable = true
        nuvem.isFocusable = true
        nuvem.contentDescription = "Emoção $emocao"

        nuvem.setOnClickListener {

            if (popupAberto) {
                return@setOnClickListener
            }

            if (emocao == "feliz") {

                mostrarPopupAcerto()

            } else {

                Toast.makeText(
                    this,
                    "A girafinha não está se sentindo assim, tente outra emoção",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ==========================================
    // POP-UP DE ACERTO
    // ==========================================

    private fun mostrarPopupAcerto() {

        if (popupAberto) return

        popupAberto = true

        val dialog = Dialog(this)

        val painel = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            setPadding(
                dp(20),
                dp(18),
                dp(20),
                dp(20)
            )

            background = GradientDrawable().apply {

                setColor(Color.WHITE)

                cornerRadius = dp(25).toFloat()

                setStroke(
                    dp(4),
                    Color.parseColor("#B5E187")
                )
            }
        }

        // TÍTULO
        val titulo = TextView(this).apply {

            text = "Parabéns!"

            textSize = 27f

            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#639B30")
            )
        }

        painel.addView(titulo)

        // MENSAGEM
        val mensagem = TextView(this).apply {

            text = "Você encontrou a emoção da girafinha!"

            textSize = 17f

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#474747")
            )

            setPadding(
                dp(2),
                dp(12),
                dp(2),
                dp(22)
            )
        }

        painel.addView(mensagem)

        // BOTÕES
        val botoes = LinearLayout(this).apply {

            orientation = LinearLayout.HORIZONTAL

            gravity = Gravity.CENTER
        }

        // BOTÃO VOLTAR
        val voltar = criarBotao(
            "← Voltar",
            "#83BDEB"
        )

        botoes.addView(
            voltar,
            LinearLayout.LayoutParams(
                0,
                dp(49),
                1f
            )
        )

        voltar.setOnClickListener {

            dialog.dismiss()

            voltarParaJogos()
        }

        // BOTÃO PRÓXIMO NÍVEL
        val proximo = criarBotao(
            "Próximo nível →",
            "#8ACF8B"
        )

        val parametros = LinearLayout.LayoutParams(
            0,
            dp(49),
            1f
        )

        parametros.marginStart = dp(12)

        botoes.addView(
            proximo,
            parametros
        )

        proximo.setOnClickListener {

            dialog.dismiss()

            val intent = Intent(
                this,
                IdentificandoEmocoes2Activity::class.java
            )

            startActivity(intent)

            finish()
        }

        painel.addView(botoes)

        // CONFIGURAR POP-UP
        dialog.setContentView(painel)
        dialog.setCancelable(false)

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )

        dialog.setOnDismissListener {
            popupAberto = false
        }

        dialog.show()

        dialog.window?.apply {

            addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
            )

            val parametrosJanela = attributes
            parametrosJanela.dimAmount = 0.55f
            attributes = parametrosJanela

            setLayout(
                minOf(
                    (
                            resources.displayMetrics.widthPixels
                                    * 0.78f
                            ).toInt(),
                    dp(480)
                ),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    // ==========================================
    // CRIAR BOTÕES DO POP-UP
    // ==========================================

    private fun criarBotao(
        textoBotao: String,
        cor: String
    ): TextView {

        return TextView(this).apply {

            text = textoBotao

            gravity = Gravity.CENTER

            textSize = 15f

            typeface = Typeface.DEFAULT_BOLD

            setTextColor(Color.WHITE)

            isClickable = true

            background = GradientDrawable().apply {

                setColor(Color.parseColor(cor))

                cornerRadius = dp(15).toFloat()
            }
        }
    }

    // ==========================================
    // VOLTAR AOS CINCO JOGOS
    // ==========================================

    private fun voltarParaJogos() {

        val intent = Intent(
            this,
            CincoOpcoesDosJogosActivity::class.java
        )

        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

        startActivity(intent)

        finish()
    }

    // ==========================================
    // ADICIONAR IMAGENS
    // ==========================================

    private fun adicionarImagem(
        recurso: Int,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float,
        escala: ImageView.ScaleType =
            ImageView.ScaleType.FIT_CENTER
    ): ImageView {

        val imagem = ImageView(this)

        imagem.setImageResource(recurso)
        imagem.scaleType = escala

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        tela.addView(imagem, parametros)

        return imagem
    }

    // ==========================================
    // CONVERTER DP
    // ==========================================

    private fun dp(valor: Int): Int {

        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }
}
