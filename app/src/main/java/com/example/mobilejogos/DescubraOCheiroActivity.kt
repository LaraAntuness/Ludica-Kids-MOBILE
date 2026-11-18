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
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class DescubraOCheiroActivity : Activity() {

    private lateinit var tela: FrameLayout

    private val larguraBase = 874f
    private val alturaBase = 393f

    private var jogoFinalizado = false
    private var popupAberto = false

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

        tela.post {
            montarTela()
        }
    }

    // =====================================================
    // MONTAR A TELA
    // =====================================================

    private fun montarTela() {

        tela.removeAllViews()

        jogoFinalizado = false
        popupAberto = false

        val sx = tela.width.toFloat() / larguraBase
        val sy = tela.height.toFloat() / alturaBase

        // =================================================
        // IMAGEM DE FUNDO
        // =================================================

        val fundo = ImageView(this)

        fundo.setImageResource(R.drawable.planodescubracomocheiro)

        fundo.scaleType = ImageView.ScaleType.FIT_XY

        fundo.isClickable = false
        fundo.isFocusable = false

        tela.addView(
            fundo,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // =================================================
        // BOTÃO VOLTAR
        // =================================================

        val voltar = adicionarImagem(
            "botaoretornoazul",
            18, 25, 40, 39,
            sx, sy
        )

        voltar.contentDescription = "Voltar"
        voltar.isClickable = true

        voltar.setOnClickListener {
            finish()
        }

        // =================================================
        // TÍTULO
        // =================================================

        adicionarImagem(
            "titulodescubracomocheiro",
            207, 10, 465, 80,
            sx, sy
        )

        // =================================================
        // PERSONAGEM
        // =================================================

        adicionarImagem(
            "meninadeverde",
            55, 190, 150, 210,
            sx, sy
        )

        // =================================================
        // BALÃO COM A PERGUNTA
        // =================================================

        criarBalao(sx, sy)

        // =================================================
        // BRIGADEIRO — RESPOSTA CERTA
        // =================================================

        criarOpcao(
            nomeImagem = "brigadeiro",
            x = 395,
            y = 105,
            sx = sx,
            sy = sy,
            deslocamentoImagemX = 8
        ) {
            mostrarResposta(true, sx, sy)
        }

        // =================================================
        // PIZZA — RESPOSTA ERRADA
        // =================================================

        criarOpcao(
            nomeImagem = "pizza",
            x = 600,
            y = 105,
            sx = sx,
            sy = sy
        ) {
            mostrarResposta(false, sx, sy)
        }

        // =================================================
        // BRÓCOLIS — RESPOSTA ERRADA
        // =================================================

        criarOpcao(
            nomeImagem = "brocolis",
            x = 395,
            y = 245,
            sx = sx,
            sy = sy
        ) {
            mostrarResposta(false, sx, sy)
        }

        // =================================================
        // MACARRÃO — RESPOSTA ERRADA
        // =================================================

        criarOpcao(
            nomeImagem = "macarrao",
            x = 600,
            y = 245,
            sx = sx,
            sy = sy
        ) {
            mostrarResposta(false, sx, sy)
        }
    }

    // =====================================================
    // BALÃO DE PERGUNTA
    // =====================================================

    private fun criarBalao(
        sx: Float,
        sy: Float
    ) {

        adicionarImagem(
            "balaopergunta",
            85, 80, 340, 135,
            sx, sy
        )

        val pergunta = TextView(this)

        pergunta.text = "Qual alimento tem um\ncheiro doce?"

        pergunta.gravity = Gravity.CENTER

        pergunta.setTextColor(
            Color.parseColor("#303030")
        )

        pergunta.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            18f * sy
        )

        pergunta.typeface = Typeface.create(
            "sans-serif",
            Typeface.BOLD
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
            125, 108, 235, 48,
            sx, sy
        )
    }

    // =====================================================
    // CRIAR OS CARTÕES DOS ALIMENTOS
    // =====================================================

    private fun criarOpcao(
        nomeImagem: String,
        x: Int,
        y: Int,
        sx: Float,
        sy: Float,
        deslocamentoImagemX: Int = 0,
        acao: () -> Unit
    ) {

        // SOMBRA DO CARTÃO

        val sombra = View(this)

        sombra.background = GradientDrawable().apply {

            setColor(Color.parseColor("#E8E7E7"))

            cornerRadius = 17f * sx
        }

        sombra.isClickable = false

        adicionarView(
            sombra,
            x + 13,
            y + 13,
            155,
            100,
            sx,
            sy
        )

        // CARTÃO AZUL

        val cartao = FrameLayout(this)

        cartao.background = GradientDrawable().apply {

            setColor(Color.parseColor("#96CBEB"))

            cornerRadius = 17f * sx
        }

        val parametrosCartao = FrameLayout.LayoutParams(
            (155 * sx).toInt().coerceAtLeast(1),
            (100 * sy).toInt().coerceAtLeast(1)
        )

        parametrosCartao.leftMargin = (x * sx).toInt()
        parametrosCartao.topMargin = (y * sy).toInt()

        tela.addView(cartao, parametrosCartao)

        // IMAGEM DO ALIMENTO

        val alimento = ImageView(this)

        val idImagem = resources.getIdentifier(
            nomeImagem,
            "drawable",
            packageName
        )

        if (idImagem != 0) {

            alimento.setImageResource(idImagem)

        } else {

            Log.e(
                "DescubraOCheiro",
                "Imagem não encontrada: $nomeImagem"
            )

            Toast.makeText(
                this,
                "Imagem não encontrada: $nomeImagem",
                Toast.LENGTH_LONG
            ).show()
        }

        alimento.scaleType = ImageView.ScaleType.FIT_CENTER

        alimento.contentDescription = nomeImagem

        // A IMAGEM NÃO BLOQUEIA O TOQUE NO CARTÃO

        alimento.isClickable = false
        alimento.isFocusable = false

        val parametrosImagem = FrameLayout.LayoutParams(
            (115 * sx).toInt().coerceAtLeast(1),
            (84 * sy).toInt().coerceAtLeast(1),
            Gravity.CENTER
        )

        cartao.addView(alimento, parametrosImagem)

        // MOVE APENAS A IMAGEM DENTRO DA CAIXINHA
        // NO BRIGADEIRO, O VALOR É 8

        alimento.translationX = deslocamentoImagemX * sx

        // =================================================
        // ÁREA TRANSPARENTE DE CLIQUE
        // FICA POR CIMA DA IMAGEM
        // =================================================

        val areaClique = View(this)

        areaClique.setBackgroundColor(Color.TRANSPARENT)

        areaClique.isClickable = true
        areaClique.isFocusable = true

        areaClique.contentDescription = "Selecionar $nomeImagem"

        cartao.addView(
            areaClique,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        areaClique.setOnClickListener {

            if (!jogoFinalizado && !popupAberto) {
                acao()
            }
        }
    }

    // =====================================================
    // CONFERIR RESPOSTA
    // =====================================================

    private fun mostrarResposta(
        correta: Boolean,
        sx: Float,
        sy: Float
    ) {

        if (jogoFinalizado || popupAberto) {
            return
        }

        if (correta) {

            jogoFinalizado = true

            mostrarPopupParabens(sx, sy)

        } else {

            Toast.makeText(
                this,
                "Tente novamente!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // =====================================================
    // POP-UP ANIMADO DE PARABÉNS
    // =====================================================

    private fun mostrarPopupParabens(
        sx: Float,
        sy: Float
    ) {

        if (popupAberto) {
            return
        }

        popupAberto = true

        // FUNDO ESCURECIDO

        val camadaEscura = FrameLayout(this)

        camadaEscura.setBackgroundColor(
            Color.parseColor("#99000000")
        )

        camadaEscura.isClickable = true
        camadaEscura.isFocusable = true

        tela.addView(
            camadaEscura,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // CAIXA DO POP-UP

        val popup = FrameLayout(this)

        popup.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(
                Color.parseColor("#FFFBE9"),
                Color.parseColor("#FFEFC7")
            )
        ).apply {

            cornerRadius = 28f * sx

            setStroke(
                (4 * sx).toInt().coerceAtLeast(1),
                Color.parseColor("#FFD76A")
            )
        }

        popup.elevation = 15f * sx

        val parametrosPopup = FrameLayout.LayoutParams(
            (540 * sx).toInt(),
            (270 * sy).toInt(),
            Gravity.CENTER
        )

        camadaEscura.addView(
            popup,
            parametrosPopup
        )

        // ENTRADA ANIMADA

        popup.scaleX = 0.5f
        popup.scaleY = 0.5f
        popup.alpha = 0f

        popup.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(450)
            .setInterpolator(
                AccelerateDecelerateInterpolator()
            )
            .start()

        // =================================================
        // ESTRELAS DECORATIVAS
        // =================================================

        val estrela1 = TextView(this).apply {

            text = "⭐"
            gravity = Gravity.CENTER

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                32f * sy
            )
        }

        adicionarViewNoPopup(
            popup,
            estrela1,
            30, 20, 45, 45,
            sx, sy
        )

        val estrela2 = TextView(this).apply {

            text = "✨"
            gravity = Gravity.CENTER

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                34f * sy
            )
        }

        adicionarViewNoPopup(
            popup,
            estrela2,
            460, 20, 50, 45,
            sx, sy
        )

        // =================================================
        // TÍTULO PARABÉNS
        // =================================================

        val titulo = TextView(this).apply {

            text = "Parabéns, explorador!"

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#D48A22")
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                31f * sy
            )

            typeface = Typeface.create(
                "sans-serif-rounded",
                Typeface.BOLD
            )
        }

        adicionarViewNoPopup(
            popup,
            titulo,
            35, 58, 470, 50,
            sx, sy
        )

        // =================================================
        // MENSAGEM DE PARABÉNS
        // =================================================

        val mensagem = TextView(this).apply {

            text = "O brigadeiro tem um\ncheirinho doce! 🍫"

            gravity = Gravity.CENTER

            setTextColor(
                Color.parseColor("#604423")
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                23f * sy
            )

            typeface = Typeface.create(
                "sans-serif",
                Typeface.BOLD
            )

            alpha = 0f
            translationY = 15f * sy
        }

        adicionarViewNoPopup(
            popup,
            mensagem,
            40, 111, 460, 63,
            sx, sy
        )

        // ANIMAÇÃO DA MENSAGEM

        mensagem.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(350)
            .setDuration(650)
            .start()

        // =================================================
        // BOTÃO VOLTAR
        // =================================================

        criarBotaoPopup(
            popup = popup,
            texto = "← Voltar",
            x = 48,
            y = 195,
            largura = 195,
            altura = 52,
            cor = "#83BDEB",
            sx = sx,
            sy = sy
        ) {

            finish()
        }

        // =================================================
        // BOTÃO PRÓXIMA FASE
        // =================================================

        criarBotaoPopup(
            popup = popup,
            texto = "Próxima fase →",
            x = 265,
            y = 195,
            largura = 225,
            altura = 52,
            cor = "#8ACF8B",
            sx = sx,
            sy = sy
        ) {

            abrirProximaFase()
        }
    }

    // =====================================================
    // BOTÕES DO POP-UP
    // =====================================================

    private fun criarBotaoPopup(
        popup: FrameLayout,
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
            19f * sy
        )

        botao.typeface = Typeface.create(
            "sans-serif",
            Typeface.BOLD
        )

        botao.background = GradientDrawable().apply {

            setColor(Color.parseColor(cor))

            cornerRadius = 18f * sx
        }

        botao.isClickable = true
        botao.isFocusable = true

        botao.setOnClickListener {
            acao()
        }

        adicionarViewNoPopup(
            popup,
            botao,
            x, y, largura, altura,
            sx, sy
        )
    }

    // =====================================================
    // ABRIR PRÓXIMA FASE
    // =====================================================

    private fun abrirProximaFase() {

        try {

            val classeProximaFase = Class.forName(
                "$packageName.DescubraOCheiro2Activity"
            )

            val intent = Intent(
                this,
                classeProximaFase
            )

            startActivity(intent)

            finish()

        } catch (erro: Exception) {

            Log.e(
                "DescubraOCheiro",
                "Não foi possível abrir a próxima fase",
                erro
            )

            Toast.makeText(
                this,
                "A próxima fase ainda não está disponível!",
                Toast.LENGTH_LONG
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

        val idImagem = resources.getIdentifier(
            nome,
            "drawable",
            packageName
        )

        if (idImagem != 0) {

            imagem.setImageResource(idImagem)

        } else {

            Log.e(
                "DescubraOCheiro",
                "Imagem não encontrada: $nome"
            )

            Toast.makeText(
                this,
                "Imagem não encontrada: $nome",
                Toast.LENGTH_LONG
            ).show()
        }

        imagem.scaleType =
            ImageView.ScaleType.FIT_CENTER

        imagem.isClickable = false
        imagem.isFocusable = false

        adicionarView(
            imagem,
            x, y, largura, altura,
            sx, sy
        )

        return imagem
    }

    // =====================================================
    // ADICIONAR VIEW NA TELA
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

        parametros.leftMargin = (x * sx).toInt()
        parametros.topMargin = (y * sy).toInt()

        tela.addView(
            view,
            parametros
        )
    }

    // =====================================================
    // ADICIONAR VIEW DENTRO DO POP-UP
    // =====================================================

    private fun adicionarViewNoPopup(
        popup: FrameLayout,
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

        popup.addView(
            view,
            parametros
        )
    }
}