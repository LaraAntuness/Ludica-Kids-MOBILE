
package com.example.mobilejogos

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class DescubraOCheiroActivity : Activity() {

    private lateinit var tela: FrameLayout

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

        setContentView(R.layout.activity_descubra_o_cheiro)

        tela = findViewById(R.id.telaDescubraCheiro)
        tela.setBackgroundColor(Color.parseColor("#E8F4FC"))

        tela.post {
            montarTela()
        }
    }

    private fun montarTela() {
        tela.removeAllViews()

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // Fundo da tela
        tela.setBackgroundColor(Color.parseColor("#E8F4FC"))

        // Botão de voltar
        val voltar = adicionarImagem(
            "botaoretornoazul",
            18, 25, 40, 39, sx, sy
        )

        voltar.contentDescription = "Voltar"
        voltar.isClickable = true

        voltar.setOnClickListener {
            finish()
        }

        // Título da tela
        adicionarImagem(
            "titulodescubracomocheiro",
            207, 10, 465, 80, sx, sy
        )

        // Menina à esquerda
        adicionarImagem(
            "meninadeverde",
            55, 190, 150, 210, sx, sy
        )

        // Balão de pergunta
        criarBalao(sx, sy)

        // Morango: resposta correta
        criarOpcao(
            "morango",
            395, 105, sx, sy
        ) {
            mostrarResposta(true)
        }

        // Pizza: resposta incorreta
        criarOpcao(
            "pizza",
            600, 105, sx, sy
        ) {
            mostrarResposta(false)
        }

        // Brócolis: resposta incorreta
        criarOpcao(
            "brocolis",
            395, 245, sx, sy
        ) {
            mostrarResposta(false)
        }

        // Macarrão: resposta incorreta
        criarOpcao(
            "macarrao",
            600, 245, sx, sy
        ) {
            mostrarResposta(false)
        }
    }

    private fun criarBalao(
        sx: Float,
        sy: Float
    ) {
        // Balão com a ponta incluída na imagem
        adicionarImagem(
            "balaopergunta",
            85, 80, 340, 135, sx, sy
        )

        // Pergunta sobreposta ao balão
        val pergunta = TextView(this)

        pergunta.text = "Qual alimento tem um\ncheiro doce?"
        pergunta.gravity = Gravity.CENTER
        pergunta.setTextColor(Color.parseColor("#303030"))

        pergunta.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            18f * sy
        )

        pergunta.setTypeface(
            Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )
        )

        pergunta.includeFontPadding = true
        pergunta.maxLines = 2

        pergunta.setPadding(
            (5 * sx).toInt(),
            (2 * sy).toInt(),
            (5 * sx).toInt(),
            (2 * sy).toInt()
        )

        adicionarView(
            pergunta,
            125, 108, 235, 48, sx, sy
        )
    }

    private fun criarOpcao(
        nomeImagem: String,
        x: Int,
        y: Int,
        sx: Float,
        sy: Float,
        acao: () -> Unit
    ) {
        // Sombra cinza-clara
        val sombra = View(this)

        sombra.background = GradientDrawable().apply {
            setColor(Color.parseColor("#E8E7E7"))
            cornerRadius = 17f * sx
        }

        adicionarView(
            sombra,
            x + 13, y + 13, 155, 100, sx, sy
        )

        // Cartão azul
        val cartao = FrameLayout(this)

        cartao.background = GradientDrawable().apply {
            setColor(Color.parseColor("#96CBEB"))
            cornerRadius = 17f * sx
        }

        val parametros = FrameLayout.LayoutParams(
            (155 * sx).toInt().coerceAtLeast(1),
            (100 * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        tela.addView(cartao, parametros)

        // Imagem do alimento
        val alimento = ImageView(this)

        val id = resources.getIdentifier(
            nomeImagem,
            "drawable",
            packageName
        )

        if (id != 0) {
            alimento.setImageResource(id)
        } else {
            Toast.makeText(
                this,
                "Imagem não encontrada: $nomeImagem",
                Toast.LENGTH_LONG
            ).show()

            android.util.Log.e(
                "DescubraOCheiro",
                "Imagem não encontrada: $nomeImagem"
            )
        }

        alimento.scaleType = ImageView.ScaleType.FIT_CENTER
        alimento.contentDescription = nomeImagem

        val parametrosImagem = FrameLayout.LayoutParams(
            (115 * sx).toInt(),
            (84 * sy).toInt(),
            Gravity.CENTER
        )

        cartao.addView(alimento, parametrosImagem)

        // Clique em todo o cartão
        cartao.isClickable = true
        cartao.isFocusable = true

        cartao.setOnClickListener {
            acao()
        }
    }

    private fun adicionarImagem(
        nome: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ): ImageView {

        val imagem = ImageView(this)

        val id = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (id != 0) {
            imagem.setImageResource(id)
        } else {
            Toast.makeText(
                this,
                "Imagem não encontrada: $nome",
                Toast.LENGTH_LONG
            ).show()

            android.util.Log.e(
                "DescubraOCheiro",
                "Imagem não encontrada: $nome"
            )
        }

        imagem.scaleType = ImageView.ScaleType.FIT_CENTER

        adicionarView(
            imagem,
            x, y, largura, altura, sx, sy
        )

        return imagem
    }

    private fun adicionarView(
        view: View,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        sx: Float,
        sy: Float
    ) {
        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        tela.addView(view, parametros)
    }

    // Mensagem de resposta
    private fun mostrarResposta(correta: Boolean) {

        val mensagem = if (correta) {
            "Parabéns! Morango tem um cheirinho doce! 🍓"
        } else {
            "Tente novamente!"
        }

        Toast.makeText(
            this,
            mensagem,
            Toast.LENGTH_LONG
        ).show()
    }
}
