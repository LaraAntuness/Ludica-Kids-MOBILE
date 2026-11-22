
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
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class IdentificandoEmocoes3Activity : Activity() {

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

        setContentView(R.layout.activity_emocoes3)
        tela = findViewById(R.id.telaEmocoes)

        tela.post { montarTela() }
    }

    // ============================================
    // MONTAR TELA
    // ============================================

    private fun montarTela() {
        tela.removeAllViews()
        popupAberto = false

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // FUNDO
        adicionarImagem(
            R.drawable.planodefundodasemocoes,
            0, 0, 874, 393, sx, sy,
            ImageView.ScaleType.FIT_XY
        )

        // GIRAFINHA COM DÚVIDA
        adicionarImagem(
            R.drawable.girafaduvida,
            352, 195, 175, 205, sx, sy
        )

        // TÍTULO
        adicionarImagem(
            R.drawable.tituloemocoesatualizado,
            205, 2, 490, 85, sx, sy
        )

        // INSTRUÇÃO
        adicionarImagem(
            R.drawable.indicacaodeapertarnanuvem,
            230, 91, 430, 42, sx, sy
        )

        // BOTÃO VOLTAR
        val voltar = adicionarImagem(
            R.drawable.botaoretornoverde,
            19, 25, 40, 40, sx, sy
        )

        voltar.contentDescription = "Voltar aos cinco jogos"
        voltar.setOnClickListener { voltarParaJogos() }

        // NUVENS
        criarNuvem(
            R.drawable.nuvembrava, "brava",
            82, 138, 140, 93, sx, sy
        )

        criarNuvem(
            R.drawable.nuvemfeliz, "feliz",
            232, 138, 140, 93, sx, sy
        )

        criarNuvem(
            R.drawable.nuvemtriste, "triste",
            514, 138, 140, 93, sx, sy
        )

        criarNuvem(
            R.drawable.nuvemduvida, "duvida",
            666, 138, 140, 93, sx, sy
        )
    }

    // ============================================
    // CLIQUES NAS NUVENS
    // ============================================

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
            recurso, x, y, largura, altura, sx, sy
        )

        nuvem.isClickable = true
        nuvem.isFocusable = true
        nuvem.contentDescription = "Emoção $emocao"

        nuvem.setOnClickListener {
            if (popupAberto) return@setOnClickListener

            if (emocao == "duvida") {
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

    // ============================================
    // POP-UP
    // ============================================

    private fun mostrarPopupAcerto() {
        if (popupAberto) return
        popupAberto = true

        val dialog = Dialog(this)

        val painel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(18), dp(20), dp(20))

            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(24).toFloat()
                setStroke(
                    dp(3),
                    Color.parseColor("#B5E187")
                )
            }
        }

        val titulo = TextView(this).apply {
            text = "Parabéns!"
            textSize = 27f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#639B30"))
        }
        painel.addView(titulo)

        val mensagem = TextView(this).apply {
            text = "Você encontrou a emoção da girafinha!"
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#474747"))
            setPadding(dp(2), dp(12), dp(2), dp(22))
        }
        painel.addView(mensagem)

        val botoes = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        // BOTÃO VOLTAR
        val voltar = criarBotao("← Voltar", "#83BDEB")
        botoes.addView(
            voltar,
            LinearLayout.LayoutParams(0, dp(49), 1f)
        )

        voltar.setOnClickListener {
            dialog.dismiss()
            voltarParaJogos()
        }

        // BOTÃO PRÓXIMO NÍVEL
        val proximo = criarBotao(
            "Próximo nível →", "#8ACF8B"
        )

        val params = LinearLayout.LayoutParams(
            0, dp(49), 1f
        )
        params.marginStart = dp(12)
        botoes.addView(proximo, params)

        proximo.setOnClickListener {
            dialog.dismiss()

            startActivity(
                Intent(
                    this,
                    IdentificandoEmocoes4Activity::class.java
                )
            )
            finish()
        }

        painel.addView(botoes)

        dialog.setContentView(painel)
        dialog.setCancelable(false)
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

            val p = attributes
            p.dimAmount = 0.55f
            attributes = p

            setLayout(
                minOf(
                    (resources.displayMetrics.widthPixels * 0.78f).toInt(),
                    dp(480)
                ),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    // ============================================
    // BOTÕES
    // ============================================

    private fun criarBotao(
        textoBotao: String,
        cor: String
    ): TextView {
        return TextView(this).apply {
            text = textoBotao
            textSize = 15f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            isClickable = true

            background = GradientDrawable().apply {
                setColor(Color.parseColor(cor))
                cornerRadius = dp(15).toFloat()
            }
        }
    }

    // ============================================
    // VOLTAR AOS CINCO JOGOS
    // ============================================

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

    // ============================================
    // IMAGENS
    // ============================================

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

        val imagem = ImageView(this).apply {
            setImageResource(recurso)
            scaleType = escala
        }

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        tela.addView(imagem, parametros)
        return imagem
    }

    private fun dp(valor: Int): Int {
        return (
                valor * resources.displayMetrics.density
                ).toInt()
    }
}
