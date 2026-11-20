package com.example.mobilejogos

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
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

    private var popupAberto = false

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

        tela.post {
            montarTela()
        }
    }

    // =====================================================
    // MONTAR TELA
    // =====================================================

    private fun montarTela() {

        tela.removeAllViews()

        popupAberto = false

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // =================================================
        // NOVO PLANO DE FUNDO - FUNDOSABOR
        // =================================================

        val fundo = ImageView(this)

        fundo.setImageResource(
            R.drawable.fundosabor
        )

        fundo.scaleType = ImageView.ScaleType.FIT_XY

        fundo.isClickable = false

        tela.addView(
            fundo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // BOTÃO DE VOLTAR
        // =================================================

        val voltar = adicionarImagem(
            "botaoretornoazul",
            18, 25, 40, 39,
            sx, sy
        )

        voltar.isClickable = true
        voltar.contentDescription = "Voltar"

        voltar.setOnClickListener {
            finish()
        }

        // =================================================
        // TÍTULO
        // =================================================

        adicionarImagem(
            "titulobebidafavorita",
            207, 10, 600, 90,
            sx, sy
        )

        // =================================================
        // BALÃO COM A PERGUNTA
        // =================================================

        adicionarImagem(
            "balaopergunta",
            85, 80, 340, 135,
            sx, sy
        )

        val pergunta = TextView(this).apply {

            text = "Qual dessas bebidas\nvocê mais gosta?"

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#303030")
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                18f * sy
            )

            typeface = Typeface.DEFAULT_BOLD
        }

        adicionarView(
            pergunta,
            125, 108, 235, 48,
            sx, sy
        )

        // =================================================
        // MENINO ABAIXO DO BALÃO
        // =================================================

        adicionarImagem(
            "menino",
            55, 190, 150, 210,
            sx, sy
        )

        // =================================================
        // ÁGUA
        // =================================================

        criarOpcao(
            "agua",
            "Água",
            395, 105,
            sx, sy
        )

        // =================================================
        // SUCO
        // =================================================

        criarOpcao(
            "suco",
            "Suco",
            600, 105,
            sx, sy
        )

        // =================================================
        // MILKSHAKE
        // =================================================

        criarOpcao(
            "milkshake",
            "Milkshake",
            395, 245,
            sx, sy
        )

        // =================================================
        // REFRIGERANTE
        // =================================================

        criarOpcao(
            "refrigerante",
            "Refrigerante",
            600, 245,
            sx, sy
        )
    }

    // =====================================================
    // CRIAR CARTÕES DAS BEBIDAS
    // =====================================================

    private fun criarOpcao(
        nomeImagem: String,
        nomeBebida: String,
        x: Int,
        y: Int,
        sx: Float,
        sy: Float
    ) {

        // =================================================
        // SOMBRA DO CARTÃO
        // =================================================

        val sombra = View(this)

        sombra.background = GradientDrawable().apply {

            setColor(
                Color.parseColor("#E8E7E7")
            )

            cornerRadius = 17f * sx
        }

        adicionarView(
            sombra,
            x + 13,
            y + 13,
            155,
            100,
            sx,
            sy
        )

        // =================================================
        // CARTÃO AZUL
        // =================================================

        val cartao = FrameLayout(this)

        cartao.background = GradientDrawable().apply {

            setColor(
                Color.parseColor("#96CBEB")
            )

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

        tela.addView(
            cartao,
            parametros
        )

        // =================================================
        // IMAGEM DA BEBIDA
        // =================================================

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

        alimento.scaleType =
            ImageView.ScaleType.FIT_CENTER

        alimento.contentDescription = nomeBebida

        val parametrosImagem = FrameLayout.LayoutParams(
            (115 * sx).toInt(),
            (72 * sy).toInt(),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL
        )

        parametrosImagem.topMargin =
            (3 * sy).toInt()

        cartao.addView(
            alimento,
            parametrosImagem
        )

        // =================================================
        // NOME DA BEBIDA
        // =================================================

        val texto = TextView(this)

        texto.text = nomeBebida

        texto.gravity = Gravity.CENTER

        texto.setTextColor(
            Color.parseColor("#24549B")
        )

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

        // =================================================
        // CLIQUE NA BEBIDA
        // =================================================

        cartao.setOnClickListener {

            if (!popupAberto) {

                mostrarBebidaPreferida(
                    nomeImagem,
                    nomeBebida,
                    sx,
                    sy
                )
            }
        }
    }

    // =====================================================
    // POP-UP DA BEBIDA PREFERIDA
    // =====================================================

    private fun mostrarBebidaPreferida(
        nomeImagem: String,
        nomeBebida: String,
        sx: Float,
        sy: Float
    ) {

        if (popupAberto) {
            return
        }

        popupAberto = true

        // =================================================
        // FUNDO ESCURECIDO
        // =================================================

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

        // =================================================
        // PAINEL CENTRAL
        // =================================================

        val painel = FrameLayout(this)

        painel.background = GradientDrawable().apply {

            setColor(Color.WHITE)

            cornerRadius = 25f * sx

            setStroke(
                (4 * sx).toInt().coerceAtLeast(1),
                Color.parseColor("#96CBEB")
            )
        }

        painel.elevation = 12f * sx

        painel.isClickable = true

        val parametrosPainel = FrameLayout.LayoutParams(
            (430 * sx).toInt(),
            (300 * sy).toInt(),
            Gravity.CENTER
        )

        sobreposicao.addView(
            painel,
            parametrosPainel
        )

        // =================================================
        // TÍTULO DO POP-UP
        // =================================================

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
            (55 * sy).toInt()
        )

        parametrosTitulo.topMargin =
            (10 * sy).toInt()

        painel.addView(
            titulo,
            parametrosTitulo
        )

        // =================================================
        // IMAGEM DA BEBIDA ESCOLHIDA
        // =================================================

        val imagem = ImageView(this)

        val id = resources.getIdentifier(
            nomeImagem,
            "drawable",
            packageName
        )

        if (id != 0) {

            imagem.setImageResource(id)
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        imagem.contentDescription = nomeBebida

        val parametrosImagem = FrameLayout.LayoutParams(
            (145 * sx).toInt(),
            (105 * sy).toInt(),
            Gravity.TOP or Gravity.CENTER_HORIZONTAL
        )

        parametrosImagem.topMargin =
            (65 * sy).toInt()

        painel.addView(
            imagem,
            parametrosImagem
        )

        // =================================================
        // NOME DA BEBIDA ESCOLHIDA
        // =================================================

        val nome = TextView(this)

        nome.text = nomeBebida

        nome.gravity = Gravity.CENTER

        nome.setTextColor(
            Color.parseColor("#24549B")
        )

        nome.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            24f * sy
        )

        nome.typeface = Typeface.DEFAULT_BOLD

        val parametrosNome = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            (40 * sy).toInt()
        )

        parametrosNome.topMargin =
            (170 * sy).toInt()

        painel.addView(
            nome,
            parametrosNome
        )

        // =================================================
        // BOTÃO VOLTAR - AZUL
        // =================================================

        criarBotaoPopup(
            painel,
            "← Voltar",
            25,
            230,
            175,
            48,
            "#83BDEB",
            sx,
            sy
        ) {

            popupAberto = false
            tela.removeView(sobreposicao)
        }

        // =================================================
        // BOTÃO PRÓXIMO NÍVEL - VERDE
        // =================================================

        criarBotaoPopup(
            painel,
            "Próximo nível →",
            220,
            230,
            185,
            48,
            "#8ACF8B",
            sx,
            sy
        ) {

            abrirProximoNivel()
        }

        // =================================================
        // TOCAR FORA FECHA O POP-UP
        // =================================================

        sobreposicao.setOnClickListener {

            popupAberto = false

            tela.removeView(sobreposicao)
        }
    }

    // =====================================================
    // CRIAR BOTÕES DO POP-UP
    // =====================================================

    private fun criarBotaoPopup(
        painel: FrameLayout,
        texto: String,
        x: Int,
        y: Int,
        largura: Int,
        altura: Int,
        cor: String,
        sx: Float,
        sy: Float,
        acao: () -> Unit
    ) {

        val botao = TextView(this)

        botao.text = texto

        botao.gravity = Gravity.CENTER

        botao.setTextColor(Color.WHITE)

        botao.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            18f * sy
        )

        botao.typeface = Typeface.create(
            "sans-serif",
            Typeface.BOLD
        )

        botao.background = GradientDrawable().apply {

            setColor(
                Color.parseColor(cor)
            )

            cornerRadius = 17f * sx
        }

        botao.elevation = 3f * sx

        botao.isClickable = true

        botao.setOnClickListener {
            acao()
        }

        val parametros = FrameLayout.LayoutParams(
            (largura * sx).toInt().coerceAtLeast(1),
            (altura * sy).toInt().coerceAtLeast(1)
        )

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        painel.addView(
            botao,
            parametros
        )
    }

    // =====================================================
    // ABRIR PRÓXIMO NÍVEL
    // =====================================================

    private fun abrirProximoNivel() {

        try {

            val classeProximoNivel = Class.forName(
                "$packageName.ProveNaImaginacao2Activity"
            )

            val intent = Intent(
                this,
                classeProximoNivel
            )

            startActivity(intent)

            finish()

        } catch (erro: Exception) {

            Log.e(
                "ProveNaImaginacao",
                "Erro ao abrir próximo nível",
                erro
            )

            Toast.makeText(
                this,
                "O próximo nível ainda não está disponível!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =====================================================
    // ADICIONAR IMAGEM
    // =====================================================

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

            Log.e(
                "ProveNaImaginacao",
                "Imagem não encontrada: $nome"
            )
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        adicionarView(
            imagem,
            x,
            y,
            largura,
            altura,
            sx,
            sy
        )

        return imagem
    }

    // =====================================================
    // ADICIONAR ELEMENTOS NA TELA
    // =====================================================

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

        parametros.leftMargin =
            (x * sx).toInt()

        parametros.topMargin =
            (y * sy).toInt()

        tela.addView(
            view,
            parametros
        )
    }
}