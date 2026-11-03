
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

class ProveNaImaginacaoActivity : Activity() {

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

        setContentView(R.layout.activity_prove_na_imaginacao)

        tela = findViewById(R.id.telaProveNaImaginacao)

        tela.setBackgroundColor(Color.parseColor("#E8F4FC"))

        tela.post {
            montarTela()
        }
    }

    private fun montarTela() {
        tela.removeAllViews()

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        tela.setBackgroundColor(Color.parseColor("#E8F4FC"))

        // Botão de voltar
        val voltar = adicionarImagem(
            "botaoretornoazul",
            18, 25, 40, 39, sx, sy
        )


        voltar.contentDescription = "Voltar"

        voltar.setOnClickListener {
            finish()
        }

        // Título
        adicionarImagem(
            "titulobebidafavorita",
            207, 10, 600, 90, sx, sy
        )

        // Balão com a pergunta
        adicionarImagem(
            "balaopergunta",
            85, 80, 340, 135, sx, sy
        )

        val pergunta = TextView(this).apply {
            text = "Qual dessas bebidas\nvocê mais gosta?"
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#303030"))

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                18f * sy
            )

            typeface = Typeface.DEFAULT_BOLD
        }

        adicionarView(
            pergunta,
            125, 108, 235, 48, sx, sy
        )

        // Menino abaixo do balão
        adicionarImagem(
            "menino",
            55, 190, 150, 210, sx, sy
        )

        // Água - superior esquerdo
        criarOpcao(
            "agua",
            "Água",
            395, 105, sx, sy
        )

        // Suco - superior direito
        criarOpcao(
            "suco",
            "Suco",
            600, 105, sx, sy
        )

        // Milkshake - inferior esquerdo
        criarOpcao(
            "milkshake",
            "Milkshake",
            395, 245, sx, sy
        )

        // Refrigerante - inferior direito
        criarOpcao(
            "refrigerante",
            "Refrigerante",
            600, 245, sx, sy
        )
    }

    private fun criarOpcao(
        nomeImagem: String,
        nomeBebida: String,
        x: Int,
        y: Int,
        sx: Float,
        sy: Float
    ) {
        // Sombra do cartão
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

        cartao.isClickable = true
        cartao.isFocusable = true

        val parametros = FrameLayout.LayoutParams(
            (155 * sx).toInt().coerceAtLeast(1),
            (100 * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        tela.addView(cartao, parametros)

        // Imagem da bebida
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
        }

        alimento.scaleType = ImageView.ScaleType.FIT_CENTER
        alimento.contentDescription = nomeBebida

        val parametrosImagem = FrameLayout.LayoutParams(
            (115 * sx).toInt(),
            (72 * sy).toInt(),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL
        )

        parametrosImagem.topMargin = (3 * sy).toInt()

        cartao.addView(
            alimento,
            parametrosImagem
        )

        // Nome da bebida
        val texto = TextView(this)

        texto.text = nomeBebida
        texto.gravity = Gravity.CENTER
        texto.setTextColor(Color.parseColor("#24549B"))

        texto.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            13f * sy
        )

        texto.typeface = Typeface.DEFAULT_BOLD

        val parametrosTexto = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            (25 * sy).toInt(),
            Gravity.BOTTOM
        )

        cartao.addView(
            texto,
            parametrosTexto
        )

        // Todas as bebidas podem ser escolhidas
        cartao.setOnClickListener {
            mostrarBebidaPreferida(
                nomeImagem,
                nomeBebida,
                sx,
                sy
            )
        }
    }

    private fun mostrarBebidaPreferida(
        nomeImagem: String,
        nomeBebida: String,
        sx: Float,
        sy: Float
    ) {
        // Sobreposição escura sobre a tela
        val sobreposicao = FrameLayout(this)

        sobreposicao.setBackgroundColor(
            Color.parseColor("#99000000")
        )

        sobreposicao.isClickable = true

        tela.addView(
            sobreposicao,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Painel central
        val painel = FrameLayout(this)

        painel.background = GradientDrawable().apply {
            setColor(Color.WHITE)

            cornerRadius = 25f * sx

            setStroke(
                (4 * sx).toInt().coerceAtLeast(1),
                Color.parseColor("#96CBEB")
            )
        }

        val parametrosPainel = FrameLayout.LayoutParams(
            (350 * sx).toInt(),
            (285 * sy).toInt(),
            Gravity.CENTER
        )

        sobreposicao.addView(
            painel,
            parametrosPainel
        )

        // Mensagem principal
        val titulo = TextView(this)

        titulo.text = "Sua bebida preferida é:"
        titulo.gravity = Gravity.CENTER

        titulo.setTextColor(
            Color.parseColor("#24549B")
        )

        titulo.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            23f * sy
        )

        titulo.typeface = Typeface.DEFAULT_BOLD

        val parametrosTitulo = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            (65 * sy).toInt()
        )

        parametrosTitulo.topMargin = (15 * sy).toInt()

        painel.addView(
            titulo,
            parametrosTitulo
        )

        // Imagem da bebida selecionada
        val imagem = ImageView(this)

        val id = resources.getIdentifier(
            nomeImagem,
            "drawable",
            packageName
        )

        if (id != 0) {
            imagem.setImageResource(id)
        }

        imagem.scaleType = ImageView.ScaleType.FIT_CENTER
        imagem.contentDescription = nomeBebida

        val parametrosImagem = FrameLayout.LayoutParams(
            (150 * sx).toInt(),
            (135 * sy).toInt(),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL
        )

        parametrosImagem.topMargin = (80 * sy).toInt()

        painel.addView(
            imagem,
            parametrosImagem
        )

        // Nome da bebida escolhida
        val nome = TextView(this)

        nome.text = nomeBebida
        nome.gravity = Gravity.CENTER

        nome.setTextColor(
            Color.parseColor("#24549B")
        )

        nome.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            25f * sy
        )

        nome.typeface = Typeface.DEFAULT_BOLD

        val parametrosNome = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            (45 * sy).toInt(),
            Gravity.BOTTOM
        )

        parametrosNome.bottomMargin = (15 * sy).toInt()

        painel.addView(
            nome,
            parametrosNome
        )

        // Tocar fora do painel fecha a mensagem
        sobreposicao.setOnClickListener {
            tela.removeView(sobreposicao)
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
                "ProveNaImaginacao",
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

        tela.addView(
            view,
            parametros
        )
    }
}
